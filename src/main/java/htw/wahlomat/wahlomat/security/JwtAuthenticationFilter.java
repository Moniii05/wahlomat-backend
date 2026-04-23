package htw.wahlomat.wahlomat.security;

import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter executed once per HTTP request.
 *
 * <p>This filter is responsible for:
 * <ol>
 *   <li>Skipping authentication for public endpoints (e.g. login/register, public GETs) and in dev mode.</li>
 *   <li>Extracting the JWT from the {@code Authorization: Bearer <token>} header.</li>
 *   <li>Validating the token and reading claims (email, role).</li>
 *   <li>Loading the {@link User} from the database.</li>
 *   <li>Creating a Spring Security {@link Authentication} and storing it in the {@link SecurityContextHolder}.</li>
 * </ol>
 *
 * <p>Error handling:
 * <ul>
 *   <li>If the token is expired or invalid, request attributes are set (e.g. {@code jwtError}) so that the
 *       configured {@code AuthenticationEntryPoint} can return a proper JSON error response.</li>
 *   <li>The filter always continues the chain; access decisions happen later in the Security pipeline.</li>
 * </ul>
 */
    @Component
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

        private final JwtUtil jwtUtil;
        private final UserRepository userRepository;

    /**
     * Feature flag for development mode.
     * <p>If set to {@code false}, the filter will be skipped completely.
     */
        @Value("${security.jwt.enabled:true}") //vorläufig für devPhase, später anpassen
        private boolean jwtEnabled;

        @Autowired
        private Environment environment;  //löschen später(?): Spring Environment für Profile-Prüfung

    /**
     * Creates a new filter instance.
     *
     * @param jwtUtil utility for parsing and validating JWTs
     * @param userRepository repository for loading users by email
     */
        public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
            this.jwtUtil = jwtUtil;
            this.userRepository = userRepository;
        }


    /**
     * Determines whether this filter should be applied to the current request.
     *
     * <p>Skips filtering for:
     * <ul>
     *   <li>dev mode (JWT disabled)</li>
     *   <li>public auth endpoints (login/register)</li>
     *   <li>public GET resources (questions, lists, lookups, candidate list, candidate answers)</li>
     *   <li>public result export endpoint</li>
     * </ul>
     *
     * @param request incoming HTTP request
     * @return {@code true} if the filter should NOT run, otherwise {@code false}
     * @throws ServletException if an error occurs while deciding filter applicability
     */
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
            String path = request.getRequestURI();
            String method = request.getMethod();

            logger.info("shouldNotFilter check - Path: {}, Method: {}", path, method);

            // DEV MODE
            if (!jwtEnabled) {
                logger.debug("JWT Filter is DISABLED (dev mode)");
                return true;
            }

            // Auth ist öffentlich
            if (path.contains("/auth/login") || path.contains("/auth/register")) {
                logger.info("Skipping filter for auth endpoint");
                return true;
            }

            // Nur GET-Requests prüfen
            if (method.equals("GET")) {
                // Questions - öffentlich
                if (path.contains("/questions")) {
                    logger.info("Skipping filter for public questions");
                    return true;
                }

                // Lists - öffentlich
                if (path.contains("/lists")) {
                    logger.info("Skipping filter for public lists");
                    return true;
                }

                // Lookups - öffentlich
                if (path.contains("/lookups")) {
                    logger.info("Skipping filter for public lookups");
                    return true;
                }

                // NUR die Liste ist öffentlich, nicht einzelne Profile!
                if (path.endsWith("/candidate-profiles")) {
                    logger.info("Skipping filter for public candidate list");
                    return true;
                }

                // Antworten der Kandidaten - öffentlich
                if (path.contains("/candidate-profiles/") && path.endsWith("/questions")) {
                    logger.info("Skipping filter for public candidate answers");
                    return true;
                }
            }

            // Ergebnislisten - öffentlich
            if (method.equals("POST")) {
                if (path.contains("/export/result")) {
                    logger.info("Skipping filter for public result exports");
                    return true;
                }
            }

            // Alle anderen müssen durch Filter
            logger.info("Filter will run for this request");
            return false;  // ← Filter LÄUFT
        }


    /**
     * Performs JWT authentication if a valid {@code Authorization} header is present.
     *
     * <p>Flow:
     * <ol>
     *   <li>Read {@code Authorization} header.</li>
     *   <li>If missing/invalid format: continue filter chain without authentication.</li>
     *   <li>Extract token, parse email, load user, validate token.</li>
     *   <li>Create {@link UsernamePasswordAuthenticationToken} with role authority and store in security context.</li>
     * </ol>
     *
     * <p>If parsing/validation fails, request attributes are set:
     * <ul>
     *   <li>{@code jwtError}: "expired" | "invalid" | "general"</li>
     *   <li>{@code jwtErrorMessage}: human readable message</li>
     * </ul>
     *
     * @param request incoming HTTP request
     * @param response outgoing HTTP response
     * @param filterChain filter chain to continue processing
     * @throws ServletException if servlet/filter processing fails
     * @throws IOException if reading request or writing response fails
     */
        @Override
        protected void doFilterInternal(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain) throws ServletException, IOException {

            logger.info("=== JWT FILTER RUNNING ===");
            logger.info("Path: {}", request.getRequestURI());

            // 1. Authorization Header holen
            String authHeader = request.getHeader("Authorization");
            logger.info("Authorization Header: {}", authHeader != null ? "Present" : "NULL");

            // Wenn kein Header oder Header nicht mit "Bearer " startet → weiter ohne Auth
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("No valid Authorization header found");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                // 2. Token extrahieren (ohne "Bearer " Präfix)
                String token = authHeader.substring(7);

                // 3. Email aus Token extrahieren
                String email = jwtUtil.extractEmail(token);
                logger.info("Email from token: {}", email);

                // 4. Nur wenn noch keine Authentication im Context ist
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                logger.info("Current authentication in context: {}", currentAuth);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.info("Attempting to load user: {}", email);

                    // 5. User aus DB laden
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

                    logger.info("User loaded: {} with ID: {}", user.getEmail(), user.getUserId());

                    // 6. Token validieren
                    boolean isValid = jwtUtil.validateToken(token, email);
                    logger.info("Token validation result: {}", isValid);

                    if (jwtUtil.validateToken(token, email)) {
                        String role = jwtUtil.extractRole(token);
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(user, null, List.of(authority));

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        logger.info("User authenticated: {} with role: {}", email, role);
                    } else {
                        logger.warn("Token validation failed");
                        // Token ist ungültig, aber lass Spring Security es behandeln
                    }
                }

            } catch (ExpiredJwtException e) {
                logger.error("JWT Token expired: {}", e.getMessage());
                // Setze Attribute für customAuthenticationEntryPoint
                request.setAttribute("jwtError", "expired");
                request.setAttribute("jwtErrorMessage", "Token ist abgelaufen");

            } catch (MalformedJwtException | SignatureException e) {
                logger.error("Invalid JWT Token: {}", e.getMessage());
                // Setze Attribute für customAuthenticationEntryPoint
                request.setAttribute("jwtError", "invalid");
                request.setAttribute("jwtErrorMessage", "Ungültiger Token");

            } catch (Exception e) {
                logger.error("JWT Authentication error: {}", e.getMessage());
                request.setAttribute("jwtError", "general");
                request.setAttribute("jwtErrorMessage", e.getMessage());
            }

            filterChain.doFilter(request, response);
        }
}
