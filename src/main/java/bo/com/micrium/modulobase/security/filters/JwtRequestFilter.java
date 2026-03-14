package bo.com.micrium.modulobase.security.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;

import bo.com.micrium.modulobase.commons.RolEstado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bo.com.micrium.modulobase.security.controllers.JwtAuthenticationController;
import bo.com.micrium.modulobase.security.services.JwtUserDetailsService;
import bo.com.micrium.modulobase.security.services.RateLimiterService;

import com.micrium.bd.access.jpa.repositories.IRolAccionRepository;
import com.micrium.bd.access.jpa.repositories.IAccionRepository;
import bo.com.micrium.modulobase.controllers.EtiquetaControler;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import bo.com.micrium.modulobase.security.utils.JwtTokenUtil;
import io.github.bucket4j.Bucket;

import com.micrium.bd.access.jpa.models.RolAccion;
import com.micrium.bd.access.jpa.models.Accion;
import com.micrium.bd.access.jpa.models.Rol;
import bo.com.micrium.logger.LoggerMain;

/**
 *
 * @author alepaco.maton
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter implements Serializable {

    private final Logger log = LogManager.getLogger(JwtRequestFilter.class);

    private final long serialVersionUID = 1L;

    @Autowired
    private transient IRolRepository rolRepository;

    @Autowired
    private transient IRolAccionRepository rolAccionRepository;

    @Autowired
    private transient IAccionRepository accionRepository;

    @Autowired
    private transient JwtUserDetailsService jwtUserDetailsService;
    
    @Autowired
    private transient JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RateLimiterService rateLimiterService;

    private void peticionesOptionsCors(HttpServletRequest request, HttpServletResponse response) {
        //HttpServletRequest req = (HttpServletRequest) request;

        //response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        //response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
        response.setHeader("Access-Control-Max-Age", "1500");

        //response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Cache-Control", "no-cache, must-understand, no-store, max-age=604800, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-XSS-Protection", "1; mode=block");
        //response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");
        response.setHeader("X-Content-Type-Options", "nosniff");

        //response.setHeader("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' 'unsafe-eval' https://fonts.googleapis.com https://fonts.googleapis.com; object-src 'self'; img-src 'self' data:; form-action 'self'; font-src 'self' https:; default-src 'self' http:;");
        response.setHeader("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' 'unsafe-eval'; object-src 'self'; img-src 'self' data:; form-action 'self'; font-src 'self' https:; default-src 'self' http:;");

        Enumeration<String> headersEnum = ((HttpServletRequest) request).getHeaders("Access-Control-Request-Headers");
        StringBuilder headers = new StringBuilder();
        String delim = "";
        while (headersEnum.hasMoreElements()) {
            headers.append(delim).append(headersEnum.nextElement());
            delim = ", ";
        }
        response.setHeader("Access-Control-Allow-Headers", headers.toString());
    }

    private void tokenInvalido(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoggerMain.info("+++ UR1: " + request.getRequestURI());
        LoggerMain.info("+++ UR2: " + request.getRequestURI());

        HttpServletResponse httpResp = (HttpServletResponse) response;
        //response.setHeader("Access-Control-Allow-Origin", "*");
        //httpResp.setHeader("Access-Control-Allow-Origin", "https://10.19.11.141:8443,http://localhost:8081/Gateway/api/v1");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
        httpResp.setHeader("Access-Control-Max-Age", "1500");

        //response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Cache-Control", "no-cache, must-understand, no-store, max-age=604800, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-XSS-Protection", "1; mode=block");
        //response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");
        response.setHeader("X-Content-Type-Options", "nosniff");

        //response.setHeader("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' 'unsafe-eval' https://fonts.googleapis.com https://fonts.googleapis.com; object-src 'self'; img-src 'self' data:; form-action 'self'; font-src 'self' https:; default-src 'self' http:;");
        response.setHeader("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' 'unsafe-eval'; object-src 'self'; img-src 'self' data:; form-action 'self'; font-src 'self' https:; default-src 'self' http:;");

        Enumeration<String> headersEnum = ((HttpServletRequest) request).getHeaders("Access-Control-Request-Headers");
        StringBuilder headers = new StringBuilder();
        String delim = "";
        while (headersEnum.hasMoreElements()) {
            headers.append(delim).append(headersEnum.nextElement());
            delim = ", ";
        }
        httpResp.setHeader("Access-Control-Allow-Headers", headers.toString());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter res = response.getWriter();
        res.append("Unauthorized");
        res.close();
    }

    private void sinPermiso(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpServletResponse httpResp = (HttpServletResponse) response;
        //response.setHeader("Access-Control-Allow-Origin", "*");
        // httpResp.setHeader("Access-Control-Allow-Origin", "https://10.19.11.141:8443,http://localhost:8081/Gateway/api/v1");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        //response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
        response.setHeader("Cache-Control", "no-cache, must-understand, no-store, max-age=604800, must-revalidate, private");
        httpResp.setHeader("Access-Control-Max-Age", "1500");

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-XSS-Protection", "1; mode=block");
        //response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");
        response.setHeader("X-Content-Type-Options", "nosniff");

        //response.setHeader("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' 'unsafe-eval' https://fonts.googleapis.com https://fonts.googleapis.com; object-src 'self'; img-src 'self' data:; form-action 'self'; font-src 'self' https:; default-src 'self' http:;");
        response.setHeader("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' 'unsafe-eval'; object-src 'self'; img-src 'self' data:; form-action 'self'; font-src 'self' https:; default-src 'self' http:;");

        Enumeration<String> headersEnum = ((HttpServletRequest) request).getHeaders("Access-Control-Request-Headers");
        StringBuilder headers = new StringBuilder();
        String delim = "";
        while (headersEnum.hasMoreElements()) {
            headers.append(delim).append(headersEnum.nextElement());
            delim = ", ";
        }
        httpResp.setHeader("Access-Control-Allow-Headers", headers.toString());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        PrintWriter res = response.getWriter();
        res.append("Forbidden");
        res.close();
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // log.info("***dtn doFilterInternal URL " + request.getRequestURL());
        if (request.getMethod().equals("OPTIONS")) {
            peticionesOptionsCors(request, response);
            return;
        }

        Bucket bucket = rateLimiterService.resolveBucket("S/N"); // peticones sin token
               
        // printAllHeaders(request);
        if (!request.getRequestURI().equals(request.getContextPath() + JwtAuthenticationController.METODO_AUTENTICACION)
                && !request.getRequestURI().equals(request.getContextPath() + JwtAuthenticationController.METODO_VERSION)
                && !request.getRequestURI().equals(request.getContextPath() + EtiquetaControler.RESOURCE_BY_LLAVE)
                //&& !request.getRequestURI().equals(request.getContextPath() + "/notificacion")
                && !request.getRequestURI().equals(request.getContextPath() + EtiquetaControler.RESOURCE_BY_GRUPO)) {
            String requestTokenHeader = request.getHeader(JwtTokenUtil.KEY_TOKEN);


            String rolNombre = null;

            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                tokenInvalido(request, response);
                return;
            }
            
            // Resolver el Bucket basado en el usuario
            bucket = rateLimiterService.resolveBucket(jwtTokenUtil.getUsernameFromToken(requestTokenHeader));

            // Aplicar control de tasa
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Demasiadas solicitudes, vuelva intentar mas tarde...");
                return;
            }

            try {
                rolNombre = jwtTokenUtil.getRolNombreFromToken(requestTokenHeader);
                //log.info("ROL> " + rolNombre);
                //log.info("USER> " + jwtTokenUtil.getUsernameFromToken(requestTokenHeader));
                if (jwtTokenUtil.isTokenExpired(requestTokenHeader)) {
                    log.warn("Token expirado ");
                    tokenInvalido(request, response);
                    return;
                }
            } catch (Exception e) {
                log.error("Token invalido, " + e.getMessage(), e);
                tokenInvalido(request, response);
                return;
            }
            Rol rol = rolRepository.findByNombreAndEstado(rolNombre, RolEstado.HABILITADO);

            if (rol == null) {
                tokenInvalido(request, response);
                return;
            }

            // se nesesita validar el token pero no asi si tiene permiso ya que es un permiso universal
            /* if (request.getRequestURI().equals(request.getContextPath() + ElasticSearchControler.RESOURCE_BY_BUSCAR_EN_CASO_INVESTIGACION)) {
                chain.doFilter(request, response);
                return;
            }*/
            boolean flag = true;

            exito:
            for (RolAccion rolAccion : rolAccionRepository.findAllByRolId(rol.getId())) {
                Accion accion = accionRepository.findById(rolAccion.getAccionId()).get();
                String[] urls = accion.getUrl().split(",");
                String[] metodos = accion.getMetodo().split(",");
                int size = urls.length; 
                // log.info("req.url=" + request.getRequestURI() + "  metodo=" + request.getMethod());
                for (int i = 0; i < size; i++) {
                    String uri = request.getContextPath() + urls[i];
                    // log.info("uri="+ uri + "  metodo="+ metodos[i]);
                    if (request.getRequestURI().startsWith(uri) && metodos[i].equals(request.getMethod().toUpperCase())) {
                        flag = false;
                        break exito;
                    }

                }
            }
            if (flag) {
                log.info("sin permiso");
                sinPermiso(request, response);
                return;
            }

            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(rolNombre);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
        // Aplicar control de tasa
        if (!bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Demasiadas solicitudes, vuelva intentar mas tarde...");
            return;
        }
        
        chain.doFilter(request, response);        
    }

    // private void printAllHeaders(HttpServletRequest request) {
    //     Enumeration<String> headerNames = request.getHeaderNames();

    //     if (headerNames != null) {
    //         while (headerNames.hasMoreElements()) {
    //             String headerName = headerNames.nextElement();
    //             String headerValue = request.getHeader(headerName);
    //             log.info(headerName + ": " + headerValue);
    //         }
    //     } else {
    //         log.info("No headers found.");
    //     }
    // }

}
