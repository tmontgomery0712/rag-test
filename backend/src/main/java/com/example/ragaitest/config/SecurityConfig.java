//package com.example.ragaitest.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // TELL SPRING SECURITY TO USE THE CORS CONFIGURATION
//                .cors()
//                .csrf(AbstractHttpConfigurer::disable) // Often disabled for stateless APIs
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/**").permitAll() // Adjust your authorization rules as needed
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}
