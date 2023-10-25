package com.tripyle.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfiguration(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/", "/user/authentication-code/send",
                        "/user/auth/name", "/user/auth/username", "/user/password/change",
                        "/hashtag/*", "/profile/mbti",
                        "/tripyler/list", "/review/list").permitAll()
                .antMatchers("/report/list", "/report/{reportId}",
                        "/block/list").hasRole("ADMIN")
                .antMatchers("/user/signup/kakao",
                        "/profile/*", "/profile/**",
                        "/chat/*", "/chat/**",
                        "/tripyler/*", "/tripyler/**",
                        "/review/*", "/review/**",
                        "/my-collections/*", "/report/*", "/block/*").authenticated()
                .antMatchers(HttpMethod.OPTIONS, "/**", "/**/*").permitAll()
                .and()
                .exceptionHandling().accessDeniedHandler(new AccessDeniedHandlerImpl())
                .and()
                .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPointImpl())
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CorsFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring().antMatchers("/v2/api-docs", "/swagger-resources/**", "/swagger*/**",
                "/webjars/**");
    }
}
