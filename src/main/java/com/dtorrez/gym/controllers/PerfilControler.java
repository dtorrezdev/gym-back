/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.controllers;

import bo.com.micrium.logger.LoggerMain;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.TiposComunes;
import bo.com.micrium.modulobase.commons.UsuarioEstado;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.micrium.bd.access.jpa.models.Usuario;
import com.dtorrez.gym.controllers.dto.CambioContrasenaRequest;
import com.dtorrez.gym.controllers.dto.CambioContrasenaRequestLogin;
import com.micrium.bd.access.jpa.repositories.IUsuarioRepository;
import com.dtorrez.gym.security.utils.JwtTokenUtil;
import com.dtorrez.gym.validators.UsuarioValidator;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/perfiles", produces = {MediaType.APPLICATION_JSON_VALUE})
public class PerfilControler extends GenericControler {

    public static final String RESOURCE_CAMBIOLOGIN = "/perfiles" + "/contrasena/cambioLogin";

    private static final long serialVersionUID = -6548357999333579666L;

    @Autowired
    protected transient IUsuarioRepository repository;

    @Autowired
    protected transient UsuarioValidator validator;

    //@Autowired
    //protected transient ILogSistemaService logSistemaService;   //  37310 coverity
    
    //@Autowired
    //private transient BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/contrasena/cambioLogin")
    public ResponseEntity<?> cambiarContrasenaLogin(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody CambioContrasenaRequestLogin request,
            BindingResult result) throws ApiException {
        ipClient = obtenerIp(ipClient);

        Usuario model = repository.findByNombreUsuarioAndEstadoIn(request.getUserName(),
                Arrays.asList(UsuarioEstado.HABILITADO, UsuarioEstado.BLOQUEADO));

        validator.validate(model, request, result);

        if (result.hasErrors()) {
            throw new ApiException(result, "Errores en la validacion");
        }

       // model.setContrasena(this.passwordEncoder.encode(request.getContrasenaNueva()));

        //model = repository.save(model);

        bitacoraService.guardarBitacoraSinToken(request.getUserName(), ipClient, form, "Se cambio la contraseña:" + model);

        ResponseEntity<Object> out = ResponseEntity.ok().build();

        return out;
    }

    @PostMapping("/contrasena/cambio")
    public ResponseEntity<?> cambiarContrasena(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody CambioContrasenaRequest request,
            BindingResult result) throws ApiException {
        HashMap<String, String> map = new HashMap();

        try {
            ipClient = obtenerIp(ipClient);

            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("request ", request)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            Usuario model = repository.findByNombreUsuarioAndEstadoIn(
                    this.jwtTokenUtil.getUsernameFromToken(token),
                    Arrays.asList(UsuarioEstado.HABILITADO, UsuarioEstado.BLOQUEADO));

            validator.validate(model, request, result);

            if (result.hasErrors()) {
                map.put(TiposComunes.MENSAJE_ERROR, obtenerErrores(result));

                bitacoraService.guardarBitacora(token, ipClient, form,
                        Acciones.PERFIL_CAMBIAR_CONTRASENA, new HashMap(), map);

                throw new ApiException(result, "Errores en la validacion");
            }

           // model.setContrasena(this.passwordEncoder.encode(request.getContrasenaNueva()));

           // repository.save(model);

            map.put(TiposComunes.MENSAJE, "Cambio de contraseña exitoso.");

            bitacoraService.guardarBitacora(token, ipClient, form,
                    Acciones.PERFIL_CAMBIAR_CONTRASENA, new HashMap(), map);

            ResponseEntity<Object> out = ResponseEntity.ok().build();

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (Exception e) {
            final String mensajeError = "Error al cambiar la contraseña, " + e.getMessage();
            
            logWeb.error(obtenerNombreUsuario(),Apps.TRAZABILIDAD_SISTEMA, Acciones.PERFIL_CAMBIAR_CONTRASENA,
                    mensajeError, e);

            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);

            bitacoraService.guardarBitacora(token, ipClient, form,
                    Acciones.PERFIL_CAMBIAR_CONTRASENA, new HashMap(), map);

            throw e;
        }
    }

}
