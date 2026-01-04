package ru.castroy10.springjwt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@RequiredArgsConstructor
public class UserConfig {

    /**
     * Provides the PasswordEncoder bean.
     * Uses BCrypt for password hashing.
     *
     * @return the BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides the UserDetailsService bean with in-memory users.
     * Creates two users: one with role USER and one with role ADMIN.
     *
     * @return the UserDetailsService instance
     */
    @Bean
    public UserDetailsService userDetailsService() {
        final UserDetails user = User.builder()
                                     .username("user@example.ru")
                                     .password(passwordEncoder().encode("12345"))
                                     .roles("USER")
                                     .build();

        final UserDetails admin = User.builder()
                                      .username("admin@example.ru")
                                      .password(passwordEncoder().encode("12345"))
                                      .roles("ADMIN")
                                      .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}
