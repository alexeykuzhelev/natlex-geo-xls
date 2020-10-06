package com.natlex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // Создание двух пользователей
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser("user").password(encoder.encode("userpw")).roles("USER")
                .and()
                .withUser("admin").password(encoder.encode("adminpw")).roles("USER", "ADMIN");
    }

    // Secure the endpoints with HTTP Basic authentication
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.headers().httpStrictTransportSecurity()
                .maxAgeInSeconds(0)
                .includeSubDomains(true);

        http.httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/sections").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.GET, "/api/sections/**").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/api/sections").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/sections/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/sections/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/jobs/**").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/api/jobs/**").hasRole("ADMIN")
                .and()
                .csrf().disable();
    }
}
