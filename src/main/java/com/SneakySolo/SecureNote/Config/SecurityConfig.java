package com.SneakySolo.SecureNote.Config;

import com.SneakySolo.SecureNote.Filter.JWTAuthFilter;
import com.SneakySolo.SecureNote.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JWTAuthFilter jwtAuthFilter;

    // the reason we create a bean of this is because we do not have to later create a BCrypt object again and again
    // no more BCrypt bcrypt = new BCrypt(); instead just add private final PasswordEncoder passwordEncoder and use it freely

    // here we save this with the name PasswordEncoder, so anytime we add private final PasswordEncoder passwordEncoder, spring automatically injects it there
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    // this is the chain of filters that sits in front of your controllers and dictates any requests
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // disable csrf
                .authorizeHttpRequests(request -> request // special rules for some requests
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/notes/**").hasRole("ADMIN")
                        .requestMatchers("/api/notes/**").authenticated()
                        .anyRequest().authenticated()) // else case for those requests not mentioned before
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider()) // spring security has many providers for authentication
                // so we just tell them to use this particular one
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // we tell spring security to run our filter before it runs the mentioned filter
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        // DaoAuthenticationProvider is a class that handles username/password authentication using DB data
        // calls customUserDetailsService to get user
        // then matches the password using the encryption we used

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder()); // We tell that when checking passwords during login, use BCrypt
        return provider;

        // no checking code here because -
        // IMP - AuthProvider internally check that user from the db and the one trying to log in as the same or not
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        // for checking the login credentials which is token form, it delegates to AuthProvider
    }
}