/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dtorrez.gym.websocket;

import com.dtorrez.gym.security.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 *
 * @author maryl
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LogManager.getLogger(JwtHandshakeInterceptor.class);
    
    private final JwtTokenUtil jwtTokenUtil;

    public JwtHandshakeInterceptor(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String token = httpServletRequest.getParameter("token"); // Puede venir en Query Params
            log.info("Conexion WebSOcket token: " + token);
            if (token != null && !jwtTokenUtil.isTokenExpired(token)) {
                attributes.put("user", jwtTokenUtil.getUsernameFromToken(token));
                log.info("Conexion WebSOcket token valido, user: " + jwtTokenUtil.getUsernameFromToken(token));
                return true;
            }
        }
        return false; // Bloquea la conexión WebSocket si el token no es válido
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
        // No es necesario implementar
    }
}
