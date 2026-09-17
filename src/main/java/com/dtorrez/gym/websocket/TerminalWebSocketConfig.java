/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dtorrez.gym.websocket;

import com.dtorrez.gym.security.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;


/**
 *
 * @author maryl
 */
@Configuration
@EnableWebSocket
public class TerminalWebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TerminalWebSocketHandler(), "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenUtil))
                //.setAllowedOrigins("http://localhost:4200")
                ;
    }
    // ws://localhost:8085/modulobase/api/v1/ws/terminal
    // ws://localhost:8085/ws/terminal
}
