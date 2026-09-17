package com.dtorrez.gym.security.config;

import java.io.Serializable;
//import java.util.Arrays;

//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter; @deprecado
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
//import org.springframework.core.Ordered;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import com.dtorrez.gym.security.controllers.JwtAuthenticationController;
//import bo.com.micrium.modulobase.security.filters.JwtAuthenticationEntryPoint;
import com.dtorrez.gym.security.filters.JwtRequestFilter;
import com.dtorrez.gym.controllers.EtiquetaControler;

/**
 *
 * @author alepaco.maton
 */
@Configuration
@EnableWebSecurity
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebSecurityConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    //@Autowired
    //private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;


    // Definición de AuthenticationManager como bean
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    // @Bean
    // @Override
    // public AuthenticationManager authenticationManagerBean() {
    //     return new AuthenticationLdapManager();
    // }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())            
            .authorizeHttpRequests(authRequest -> authRequest
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(EtiquetaControler.RESOURCE_BY_LLAVE).permitAll()
            .requestMatchers(EtiquetaControler.RESOURCE_BY_GRUPO).permitAll()
            // .requestMatchers(PerfilControler.RESOURCE_CAMBIOLOGIN).permitAll().
            .requestMatchers(JwtAuthenticationController.METODO_AUTENTICACION).permitAll()
            .requestMatchers(JwtAuthenticationController.METODO_VERSION).permitAll()            
            .anyRequest().authenticated())
            .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //.authenticationProvider(authProvider)
            //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            //.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedPage("/access-denied"))
            //.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}

    /*@Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity.csrf().disable()
                    .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests().
                    antMatchers(HttpMethod.OPTIONS, "/**").permitAll().
                    antMatchers(EtiquetaControler.RESOURCE_BY_LLAVE).permitAll().
                    antMatchers(EtiquetaControler.RESOURCE_BY_GRUPO).permitAll().
                    antMatchers(PerfilControler.RESOURCE_CAMBIOLOGIN).permitAll().
                    antMatchers(JwtAuthenticationController.METODO_AUTENTICACION).permitAll().
                    antMatchers(JwtAuthenticationController.METODO_VERSION).permitAll().
                    anyRequest().authenticated();
            //coverity  
            httpSecurity.authorizeRequests().
                    antMatchers(HttpMethod.OPTIONS, "/**").permitAll().
                    antMatchers(EtiquetaControler.RESOURCE_BY_LLAVE).permitAll().
                    antMatchers(EtiquetaControler.RESOURCE_BY_GRUPO).permitAll().
                    antMatchers(PerfilControler.RESOURCE_CAMBIOLOGIN).permitAll().
                    antMatchers(JwtAuthenticationController.METODO_AUTENTICACION).permitAll().
                    antMatchers(JwtAuthenticationController.METODO_VERSION).permitAll().anyRequest().authenticated();

            httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        }*/
  

}
