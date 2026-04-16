package com.obratech.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.obratech.security.CustomAuthenticationSuccessHandler;
import com.obratech.security.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {

        http
            .authenticationProvider(authenticationProvider)
            
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // públicas
                .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/img/**", "/styles/**").permitAll()

                // cliente
                .requestMatchers("/desboard", "/perfil-cliente/**", "/mis-proyectos/**")
                .hasAuthority("ROLE_CLIENT")

                // contratista
                .requestMatchers("/desboard-contratista", "/perfil-contratista/**", "/contratista-proyectos/**")
                .hasAuthority("ROLE_CONTRACTOR")

                // trabajador
                .requestMatchers("/desboard-trabajador", "/perfil-laboral/**")
                .hasAuthority("ROLE_WORKER")

                // admin
                .requestMatchers("/admin/**")
                .hasAuthority("ROLE_ADMIN")

                // cualquier otra
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .failureUrl("/login?error=true")
                .successHandler(authenticationSuccessHandler)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
