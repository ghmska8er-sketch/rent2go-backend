package app.web.rtgtechnologies.rent2go.shared.infrastructure.config;

import app.web.rtgtechnologies.rent2go.iam.infrastructure.authorization.BearerAuthorizationRequestFilter;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.authorization.ForbiddenAccessDeniedHandler;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.authorization.UnauthorizedRequestHandlerEntryPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider tokenProvider;
    private final AuthenticationEntryPoint unauthorizedRequestHandler;
    private final ForbiddenAccessDeniedHandler forbiddenAccessDeniedHandler;

    public WebSecurityConfiguration(@Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService,
                                    app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider tokenProvider,
                                    UnauthorizedRequestHandlerEntryPoint authenticationEntryPoint,
                                    ForbiddenAccessDeniedHandler forbiddenAccessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
        this.forbiddenAccessDeniedHandler = forbiddenAccessDeniedHandler;
    }

    @Bean
    public BearerAuthorizationRequestFilter authorizationRequestFilter() {
        return new BearerAuthorizationRequestFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(configurer -> configurer.configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of("*"));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            cors.setAllowedHeaders(List.of("*"));
            return cors;
        }));

        http.csrf(csrfConfigurer -> csrfConfigurer.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                    .authenticationEntryPoint(unauthorizedRequestHandler)
                    .accessDeniedHandler(forbiddenAccessDeniedHandler))
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/api/v1/auth/password/request",
                        "/api/v1/auth/password/reset",
                        "/api/v1/auth/verify",
                        "/api/v1/payments/calculate",
                        "/api/v1/payments/webhook").permitAll()
                    .requestMatchers(HttpMethod.GET,
                        "/api/v1/vehicles/me").authenticated()
                    .requestMatchers(HttpMethod.GET,
                        "/api/v1/vehicles",
                        "/api/v1/vehicles/*",
                        "/api/v1/vehicles/*/images",
                        "/api/v1/availability/vehicle/*/check",
                        "/api/v1/features",
                        "/api/v1/features/*",
                        "/api/v1/features/name/*",
                        "/api/v1/payments/promocodes/*").permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico").permitAll()
                    .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
