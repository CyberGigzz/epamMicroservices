package com.gym.crm.config;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.security.JwtRequestFilter;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder;   
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity 
public class SecurityConfig {

    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(TraineeDAO traineeDAO, TrainerDAO trainerDAO, JwtRequestFilter jwtRequestFilter) {
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            return traineeDAO.findByUsername(username)
                    .<UserDetails>map(trainee -> User.builder()
                            .username(trainee.getUsername())
                            .password(trainee.getPassword())
                            .roles("TRAINEE") 
                            .build())
                    .orElseGet(() ->
                        trainerDAO.findByUsername(username)
                                .<UserDetails>map(trainer -> User.builder()
                                        .username(trainer.getUsername())
                                        .password(trainer.getPassword())
                                        .roles("TRAINER") 
                                        .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                    );
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                                 "/",
                                 "/api/auth/**", 
                                 "/api/trainees/register",
                                 "/api/trainers/register",
                                 "/swagger-ui.html",
                                 "/swagger-ui/**",
                                 "/v3/api-docs/**",
                                 "/h2-console/**",
                                 "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .httpBasic(AbstractHttpConfigurer::disable);

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000", 
            "http://localhost:4200"  
        ));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Transaction-Id"));
        
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}