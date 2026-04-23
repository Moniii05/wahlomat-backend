package htw.wahlomat.wahlomat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import htw.wahlomat.wahlomat.dto.ErrorResponse;
import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Wahl-O-Mat backend.
 *
 * <p>This configuration defines:</p>
 * <ul>
 *   <li>Stateless security (no HTTP session) using JWT authentication</li>
 *   <li>Endpoint authorization rules for public, candidate-only and admin-only APIs</li>
 *   <li>Custom JSON error responses for {@code 401 Unauthorized} and {@code 403 Forbidden}</li>
 *   <li>Initialization of an admin user (always) and test candidates (only in {@code dev} profile)</li>
 * </ul>
 *
 * <h2>Development mode</h2>
 * <p>If {@code security.jwt.enabled=false}, all endpoints are permitted and no JWT filter is applied.</p>
 */
@Configuration
@EnableWebSecurity   // Für SecurityFilterChain
@EnableMethodSecurity // Für @PreAuthorize in Controllern
public class SecurityConfig {

    /**
     * Admin password injected from environment/property {@code ADMIN_PASSWORD}.
     * Used only during application startup to create an initial admin user if missing.
     */
    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;
    /**
     * Enables/disables JWT security.
     *
     * <p>When set to {@code false}, the application runs in a permissive development mode
     * where all endpoints are accessible without authentication.</p>
     */
    @Value("${security.jwt.enabled:true}")
    private boolean jwtEnabled;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /**
     * ObjectMapper used to serialize {@link ErrorResponse} for authentication/authorization errors.
     * Registers {@link JavaTimeModule} for Java time types.
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Creates the security configuration.
     *
     * @param jwtAuthenticationFilter JWT authentication filter applied before
     *                                {@link UsernamePasswordAuthenticationFilter} (production mode only)
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    /**
     * Provides the {@link PasswordEncoder} used to hash user passwords.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }


    /**
     * Configures the Spring Security filter chain.
     *
     * <p>Key settings:</p>
     * <ul>
     *   <li>CORS enabled and CSRF disabled (REST API)</li>
     *   <li>Stateless session management</li>
     *   <li>Custom handlers for authentication/authorization errors</li>
     *   <li>Authorization rules for all API endpoints</li>
     * </ul>
     *
     * <p>In production mode, the {@link JwtAuthenticationFilter} is registered.</p>
     *
     * @param http Spring Security HTTP configuration
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // URL-basierte Regeln
        HttpSecurity httpSec = http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // Session Management: STATELESS (keine Server-Sessions!)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //Exception Handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                );

        // IM DEV-MODUS: Alle Endpoints freigeben!
        if (!jwtEnabled) {
            httpSec.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()  // ALLES ohne Auth erlauben
            );
            System.out.println("JWT Filter DEAKTIVIERT (Development Mode) - ALLE Endpoints öffentlich!");
        } else {
            // Authorization Rules
            httpSec.authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() //OPTIONS-Requests IMMER erlauben (CORS Preflight)

                    // Swagger UI erlauben (nur dev mode)
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/auth/login", "/api/auth/register", "/auth/login", "/auth/register").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/auth/change-password", "/auth/change-password").hasAnyRole("CANDIDATE", "ADMIN")


                    // öffentlich für alle
                    .requestMatchers(HttpMethod.GET, "/api/candidate-profiles", "/candidate-profiles").permitAll() //nur Liste! für Wähler
                    .requestMatchers(HttpMethod.GET, "/api/candidate-profiles/*/questions", "/candidate-profiles/*/questions").permitAll() // Wähler müssen auch Kandidatenantworten sehen können
                    .requestMatchers(HttpMethod.GET, "/api/lists/**", "/lists/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/candidacies/list/**", "/candidacies/list/**").permitAll()  //für wähler
                    .requestMatchers(HttpMethod.GET, "/api/lookups/**", "/lookups/**").permitAll() // Lookups lesen (Faculties, Committees)
                    .requestMatchers(HttpMethod.GET, "/api/questions/**", "/questions/**").permitAll()

                    .requestMatchers(HttpMethod.POST, "/api/candidate-profiles/matching/**", "/candidate-profiles/matching/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/export/result").permitAll()

                    .requestMatchers(HttpMethod.POST, "/api/candidate-lists/matching/**", "/candidate-lists/matching/**").permitAll()
                    //ADMIN-ONLY
                    .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/users/**", "/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/questions/**", "/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/questions/**", "/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/questions/**", "/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/questions/**", "/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/lists/**", "/lists/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/lists/**", "/lists/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/lists/**", "/lists/**").hasRole("ADMIN")

                    //CANDIDATE-ONLY
                    .requestMatchers(HttpMethod.PATCH, "/api/candidate-profiles/*/answers/**", "/candidate-profiles/*/answers/**").hasRole("CANDIDATE")
                    .requestMatchers(HttpMethod.DELETE, "/api/candidate-profiles/*/answers/**", "/candidate-profiles/*/answers/**").hasRole("CANDIDATE")

                    .requestMatchers("/api/candidate/**", "/candidate/**").hasRole("CANDIDATE") //?weg

                    //CANDIDATE + ADMIN
                    .requestMatchers(HttpMethod.GET, "/api/candidate-profiles/*", "/candidate-profiles/*").hasAnyRole("CANDIDATE", "ADMIN") //nur Zugriff auf eigenes Profil
                    .requestMatchers(HttpMethod.PUT, "/api/candidate-profiles/*", "/candidate-profiles/*").hasAnyRole("CANDIDATE", "ADMIN")
                    .requestMatchers("/api/candidacies/user/**", "/candidacies/user/**").hasAnyRole("CANDIDATE", "ADMIN")

                    // Alle anderen Requests benötigen Authentication (Fallback)
                    .anyRequest().authenticated()
            );
            // JWT Filter nur in Production
            httpSec.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            System.out.println("JWT Filter AKTIV (Production Mode)");
        }

        return httpSec.build();
    }



    /**
     * Initializes an admin user on application startup if none exists.
     *
     * <p>Execution order is controlled using {@link Order}.</p>
     * <p>The admin is created with:</p>
     * <ul>
     *   <li>Email: {@code admin@htw-berlin.de}</li>
     *   <li>Role: {@link Role#ADMIN}</li>
     *   <li>Password: value from {@code ADMIN_PASSWORD}</li>
     * </ul>
     *
     * @param userRepository repository used to check/create the admin user
     * @param passwordEncoder encoder used to hash the initial password
     * @return a {@link CommandLineRunner} executed during startup
     */

    @Bean
    @Order(1)
    public CommandLineRunner loadAdminOnInit(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@htw-berlin.de";

            if (!userRepository.existsByEmailAndRole(adminEmail, Role.ADMIN)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                System.out.println("\n═══════════════════════════════════════════");
                System.out.println("  ADMIN ERSTELLT");
                System.out.println("   Email:    " + adminEmail);
                System.out.println("   Passwort: " + adminPassword);
                System.out.println("═══════════════════════════════════════════\n");
            } else {
                System.out.println("Admin existiert bereits: " + adminEmail);
            }
        };
    }
    /**
     * Initializes test candidate users on application startup (dev profile only).
     *
     * <p>This runner only executes if {@code spring.profiles.active=dev}.</p>
     *
     * @param userRepository repository used to check/create test users
     * @param passwordEncoder encoder used to hash passwords
     * @param activeProfile current active profile (defaults to {@code dev} if not set)
     * @return a {@link CommandLineRunner} executed during startup in dev profile
     */
    @Bean
    @Order(2)
    public CommandLineRunner loadTestCandidates(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${spring.profiles.active:dev}") String activeProfile) {
        return args -> {
            if (!activeProfile.equals("dev")) {
                return;
            }

            System.out.println("\n═══════════════════════════════════════════");
            System.out.println("  TEST-CANDIDATES");
            System.out.println("═══════════════════════════════════════════");

            if (userRepository.findByEmail("anna@stud.htw-berlin.de").isEmpty()) {
                User anna = new User();
                anna.setEmail("anna@stud.htw-berlin.de");
                anna.setPassword(passwordEncoder.encode("test123"));
                anna.setRole(Role.CANDIDATE);
                userRepository.save(anna);
                System.out.println("anna@stud.htw-berlin.de / test123");
            }

            if (userRepository.findByEmail("bob@stud.htw-berlin.de").isEmpty()) {
                User bob = new User();
                bob.setEmail("bob@stud.htw-berlin.de");
                bob.setPassword(passwordEncoder.encode("test123"));
                bob.setRole(Role.CANDIDATE);
                userRepository.save(bob);
                System.out.println("bob@stud.htw-berlin.de / test123");
            }

            System.out.println("═══════════════════════════════════════════\n");
        };
    }


    /**
     * Creates an {@link AuthenticationEntryPoint} that returns a JSON {@link ErrorResponse}
     * for authentication errors (HTTP 401).
     *
     * <p>The message is derived from:</p>
     * <ul>
     *   <li>Missing/invalid {@code Authorization} header</li>
     *   <li>JWT-related request attributes (e.g., {@code jwtError})</li>
     *   <li>Fallback to {@link #determineAuthErrorMessage(HttpServletRequest, AuthenticationException)}</li>
     * </ul>
     *
     * @return custom authentication entry point
     */
    private AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            String message = determineAuthErrorMessage(request, authException);

            String jwtError = (String) request.getAttribute("jwtError");
            String jwtErrorMessage = (String) request.getAttribute("jwtErrorMessage");

            if ("expired".equals(jwtError)) {
                message = "Token ist abgelaufen. Bitte melde dich erneut an.";
            } else if ("invalid".equals(jwtError)) {
                message = "Ungültiger Token. Bitte melde dich erneut an.";
            } else if (jwtError != null) {
                message = (String) request.getAttribute("jwtErrorMessage");
            } else {
                message = determineAuthErrorMessage(request, authException);
            }

            ErrorResponse errorResponse = ErrorResponse.of(
                    401,
                    "Unauthorized",
                    message,
                    request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }

    /**
     * Creates an {@link AccessDeniedHandler} that returns a JSON {@link ErrorResponse}
     * for authorization errors (HTTP 403).
     *
     * @return custom access denied handler
     */
    private AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");

            ErrorResponse errorResponse = ErrorResponse.of(
                    403,
                    "Forbidden",
                    "Insufficient permissions for this resource",
                    request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }
    /**
     * Determines a user-facing error message for authentication failures.
     *
     * @param request current HTTP request
     * @param authException authentication exception
     * @return error message string describing the authentication problem
     */
    private String determineAuthErrorMessage(HttpServletRequest request,
                                             AuthenticationException authException) {
        if (request.getHeader("Authorization") == null) {
            return "Authorization header is missing";
        }

        String authHeader = request.getHeader("Authorization");
        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid authorization header format";
        }

        if (authException.getMessage().contains("expired")) {
            return "Token has expired";
        }

        if (authException.getMessage().contains("JWT")) {
            return "Invalid or malformed token";
        }

        return "Authentication required";
    }


}

