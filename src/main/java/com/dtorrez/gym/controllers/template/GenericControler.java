package com.dtorrez.gym.controllers.template;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.dtorrez.gym.common.exceptions.ApiException;
import com.micrium.bd.access.jpa.repositories.IUsuarioRepository;
import com.dtorrez.gym.security.utils.JwtTokenUtil;
import com.dtorrez.gym.services.BitacoraService;
import bo.com.micrium.modulobase.commons.GlobalValidator;
import com.dtorrez.gym.services.LoggerWeb;

/**
 *
 * @author alepaco.maton
 */
public class GenericControler extends GlobalValidator {

    @Autowired
    protected transient BitacoraService bitacoraService;

    //@Autowired
    //protected transient ILogSistemaService logSistemaService;
    
    @Autowired
    protected transient LoggerWeb logWeb;

    @Autowired
    protected transient JwtTokenUtil jwtTokenUtil;

    @Autowired
    protected transient HttpServletRequest httpServletRequest;

    @Autowired
    protected transient IUsuarioRepository usuarioRepository;

    protected String obtenerErrores(BindingResult result) {
        StringBuilder sb = new StringBuilder("[");

        result.getAllErrors().forEach((error) -> {
            sb.append(error.getDefaultMessage()).append(" - ");
        });

        return sb.append("]").toString();
    }

    protected String obtenerIp(String ipClient) {
        if (ipClient == null || ipClient.isEmpty() || ipClient.equals("127.0.0.1") || ipClient.equals("localhost")) {
            return httpServletRequest.getRemoteAddr();
        }

        return ipClient;
    }
    
    public Map<String, String> getParametersMap(HttpServletRequest request) {
        Map<String, String> parametersMap = new HashMap<>();

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            parametersMap.put(paramName, paramValue);
        }
        return parametersMap;
    }
    
    public boolean isBlanck(String dato) {
        return dato == null || dato.trim().isEmpty();
    }

    /**
     * Limpia caracter especial '%' por '/'
     * @param dato
     * @return
     */
    public String limpiarCaracterEspecialEncriptacion(String dato) {
        return dato.replace(".", "/");
    }

    public static boolean isInteger(String s) {
        try {
            Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    @PostMapping("/bitacoras/eventual")
    private ResponseEntity<?> guardarBitacoraEventual(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form, @RequestBody String request) throws ApiException {
        bitacoraService.guardarBitacora(token, ipClient, form, request, null, null, null);
        return ResponseEntity.ok().build();
    }

    protected String obtenerRol() {
        return jwtTokenUtil.getRolNombreFromToken(this.httpServletRequest.getHeader(JwtTokenUtil.KEY_TOKEN));
    }

    protected String obtenerNombreUsuario() {
        return jwtTokenUtil.getUsernameFromToken(this.httpServletRequest.getHeader(JwtTokenUtil.KEY_TOKEN));
    }

    protected String obtenerNombreCompletoUsuario() {
        String nombre = jwtTokenUtil.getUsernameFromToken(this.httpServletRequest.getHeader(JwtTokenUtil.KEY_TOKEN));
        //   return userService.findBynombreUsuario(nombre).getNombreCompleto();
        return usuarioRepository.findBynombreUsuario(nombre).getNombreCompleto();
    }
}
