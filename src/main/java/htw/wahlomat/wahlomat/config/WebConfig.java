package htw.wahlomat.wahlomat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * Web configuration for the Wahl-O-Mat backend.
 *
 * <p>This configuration customizes Spring MVC behavior,
 * specifically Cross-Origin Resource Sharing (CORS) settings.</p>
 *
 * <h2>CORS Configuration</h2>
 * <p>Allows requests from the Angular development frontend
 * running on {@code http://localhost:4200} to access backend API endpoints
 * under {@code /api/**}.</p>
 *
 * <p>Enabled HTTP methods:</p>
 * <ul>
 *   <li>GET</li>
 *   <li>POST</li>
 *   <li>PUT</li>
 *   <li>PATCH</li>
 *   <li>DELETE</li>
 *   <li>OPTIONS (required for CORS preflight requests)</li>
 * </ul>
 *
 * <p>Credentials (e.g., cookies or authorization headers) are allowed.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    /**
     * Configures CORS mappings for API endpoints.
     *
     * <p>All requests matching {@code /api/**} are allowed from
     * {@code http://localhost:4200} (Angular development server).</p>
     *
     * @param registry the {@link CorsRegistry} used to register CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:4200", // lokal
                        "https://wahlomat.f4.htw-berlin.de" // production
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

    }
}


