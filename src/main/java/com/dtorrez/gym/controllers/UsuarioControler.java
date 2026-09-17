/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.controllers;

import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.AbstractMap;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.net.URI;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;

import bo.com.micrium.modulobase.commons.TipoAutenticacion;
import bo.com.micrium.modulobase.commons.UsuarioEstado;

import com.dtorrez.gym.security.services.ActiveDirectoryService;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.common.exceptions.LdapContextException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Usuario;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import com.micrium.bd.access.jpa.repositories.IUsuarioRepository;
import com.dtorrez.gym.controllers.dto.UsuarioRequest;
import com.dtorrez.gym.controllers.dto.UsuarioResponse;
import com.dtorrez.gym.controllers.dto.PageResponse;
import com.dtorrez.gym.security.utils.JwtTokenUtil;
import com.dtorrez.gym.services.ParametroService;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;
import com.dtorrez.gym.services.LoggerWeb;
import com.dtorrez.gym.validators.UsuarioValidator;


/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/usuarios", produces = {MediaType.APPLICATION_JSON_VALUE})
public class UsuarioControler extends GenericControler implements ICrudControler<UsuarioRequest, UsuarioResponse, String> {
    
    private static final long serialVersionUID = 1981251085693864444L;

    private static final Logger log = LogManager.getLogger(UsuarioControler.class);
    
    @Autowired
    protected transient IUsuarioRepository repository;
    
    @Autowired
    protected transient UsuarioValidator validator;
    
    @Autowired
    private transient IRolRepository rolRepository;
    
    @Autowired
    private transient ParametroService parametroService;    
    
    @Autowired
    private transient BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public Page<UsuarioResponse> list(String token, String ipClient, String form, Pageable pageRequest) throws Exception {
        
        try {            
            
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);
            final String id = parametros.get("id");
            final String nombreCompleto = parametros.get("nombreCompleto");
            final String nombreUsuario = parametros.get("nombreUsuario");
            final String rolId = parametros.get("rolId");
            final String rolNombre = parametros.get("rolNombre");
            final String tipo = parametros.get("tipo");
            final String estado = parametros.get("estado");
            
            
            ipClient = obtenerIp(ipClient);
            
            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("id ", filterTexto(id)),
                new AbstractMap.SimpleEntry<>("nombreCompleto ", filterTexto(nombreCompleto)),
                new AbstractMap.SimpleEntry<>("nombreUsuario ", filterTexto(nombreUsuario)),
                new AbstractMap.SimpleEntry<>("rolId ", filterTexto(rolId)),
                new AbstractMap.SimpleEntry<>("rolNombre ", filterTexto(rolNombre)),
                new AbstractMap.SimpleEntry<>("tipo ", filterTexto(tipo)),
                new AbstractMap.SimpleEntry<>("estado ", filterTexto(estado)),
                new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            String tipoTemporal = "";
            if (tipo != null && !tipo.isEmpty()) {
                // OAC LDAP
                /*if (UsuarioTipo.USUARIO_NORMAL_VALOR.equals(tipo)) {
                    tipoTemporal = String.valueOf(UsuarioTipo.USUARIO_NORMAL);
                } else if (UsuarioTipo.USUARIO_ACTIVE_DIRECTORY_VALOR.equals(tipo)) {
                    tipoTemporal = String.valueOf(UsuarioTipo.USUARIO_ACTIVE_DIRECTORY);
                }*/
                if (TipoAutenticacion.LOCAL.getId().equals(tipo)) {
                    tipoTemporal = String.valueOf(TipoAutenticacion.LOCAL.getId());
                } else if (TipoAutenticacion.LDAP.getId().equals(tipo)) {
                    tipoTemporal = String.valueOf(TipoAutenticacion.LDAP.getId());
                }
            }
            
            String estadoTemporal = "";
            if (estado != null && !estado.isEmpty()) {
                if (UsuarioEstado.HABILITADO_VALOR.equals(estado)) {
                    estadoTemporal = String.valueOf(UsuarioEstado.HABILITADO);
                } else if (UsuarioEstado.INHABILITADO_VALOR.equals(estado)) {
                    estadoTemporal = String.valueOf(UsuarioEstado.INHABILITADO);
                } else if (UsuarioEstado.BLOQUEADO_VALOR.equals(estado)) {
                    estadoTemporal = String.valueOf(UsuarioEstado.BLOQUEADO);
                }
            }
            
            Page<UsuarioResponse> out = repository.filter(
                    ((id == null || id.trim().isEmpty()) ? -1 : 0),
                    ((id == null || id.trim().isEmpty()) ? "" : id.trim()),
                    ((nombreUsuario == null || nombreUsuario.isEmpty()) ? -1 : 0),
                    ((nombreUsuario == null || nombreUsuario.trim().isEmpty()) ? "" : "%" + nombreUsuario.trim().toUpperCase() + "%"),
                    ((nombreCompleto == null || nombreCompleto.isEmpty()) ? -1 : 0),
                    ((nombreCompleto == null || nombreCompleto.trim().isEmpty()) ? "" : "%" + nombreCompleto.trim().toUpperCase() + "%"),
                    ((rolId == null || rolId.isEmpty()) ? -1 : 0),
                    ((rolId == null || rolId.trim().isEmpty()) ? "" : rolNombre.trim().toUpperCase()),
                    ((rolNombre == null || rolNombre.isEmpty()) ? -1 : 0),
                    ((rolNombre == null || rolNombre.trim().isEmpty()) ? "" : rolNombre.trim().toUpperCase()),
                    ((tipo == null || tipo.isEmpty()) ? -1 : 0),
                    tipoTemporal,
                    ((estado == null || estado.isEmpty()) ? -1 : 0),
                    estadoTemporal,
                    //UsuarioTipo.SUPER_USUARIO, // OAC LDAP
                    TipoAutenticacion.SUPER.getId(),
                    pageRequest)
                    //.map(model -> ConvercionUtil.convertToObject(model, UsuarioResponse.class));
                    .map(model -> {
                        UsuarioResponse usuarioResponse = ConvercionUtil.convertToObject(model, UsuarioResponse.class);
                        usuarioResponse.setRolId(model.getRolId().getId());
                        usuarioResponse.setRolNombre(model.getRolId().getNombre());
                        usuarioResponse.setEstado(model.getEstado() == UsuarioEstado.HABILITADO ? UsuarioEstado.HABILITADO_VALOR
                                : model.getEstado() == UsuarioEstado.INHABILITADO ? UsuarioEstado.INHABILITADO_VALOR
                                : model.getEstado() == UsuarioEstado.BLOQUEADO ? UsuarioEstado.BLOQUEADO_VALOR : "");
                        return usuarioResponse;
                    });
            
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, null);
            
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", new PageResponse<>(out)),
                new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            return out;                                        
        } catch (Exception e) {
            final String mensajeError = "Error al filtrar usuarios, " + e.getMessage();
            
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);
            
            throw e;
        }
    }
    
    @Override
    public ResponseEntity<UsuarioResponse> get(String token, String ipClient, String form, String id) throws ApiException {
        /*
        ipClient = obtenerIp(ipClient);
        
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------REQUEST----------------------\n").
                append("token ").append(token).append(", \n").
                append("form ").append(form).append(", \n").
                append("ipClient ").append(ipClient).append(", \n").
                append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                append("id ").append(id).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());

        Optional<Usuario> model = repository.findById(id);
        if (model.isPresent()) {
            ResponseEntity<UsuarioResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model.get(), UsuarioResponse.class));

            sb = new StringBuilder();
            sb.append("-------------------RESPONSE----------------------\n").
                    append("token ").append(token).append(", \n").
                    append("DATA ").append(out).append(", \n").
                    append("------------------------------------------------\n");

            log.info(sb.toString());

            return out;
        }*/
        
        throw new ApiException("GET not supported method /{id}" + id);
    }
    
    @Override
    public ResponseEntity<UsuarioResponse> create(String token, String ipClient, String form,
            UsuarioRequest request, BindingResult result) throws URISyntaxException, ApiException {
        HashMap<String, String> map = new HashMap<String, String>();
        
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
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            validator.validate(request, null, result);
            
            if (result.hasErrors()) {
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                //bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_CREAR, null, map);
                
                throw new ApiException(result, "Errores en la validacion");
            }
            
            LoggerWeb.info("***** OAC TipoAutenticacio: " + request.getTipo());
            String name = request.getNombreUsuario().trim();
            //  OAC LDAP
            //if (request.getTipo() == UsuarioTipo.USUARIO_ACTIVE_DIRECTORY) {           
            if (request.getTipo().equals(TipoAutenticacion.LDAP.getId())) {
                try {
                    name = new ActiveDirectoryService(parametroService).getNombreCompleto(request.getNombreUsuario().trim());
                } catch (LdapContextException ex) {
                    //logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_CREAR, "Error de conexion ldap " + ex.getMessage(), ex);
                    result.rejectValue("nombreUsuario", "field.nombreUsuario", "Fallo con la conexión con active directory.");
                    map.put(TiposComunes.ERROR, obtenerErrores(result));
                    //bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_CREAR, null, map);
                    throw new ApiException(result, "Fallo con la conexión con active directory.");
                }
            }
            
            int dias = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.BLOQUEO_USUARIOS_DIAS.name())).intValue();
            Date fechaActualizacion = sumarDiasAFecha(new Date(), dias);

            //  OAC LDAP
            /*Usuario model = repository.save(new Usuario(null, request.getNombreUsuario(), name,
            (String) parametroService.getParamVal(ParametroID.CONTRASENA_POR_DEFECTO),
            (request.getTipo() == UsuarioTipo.USUARIO_ACTIVE_DIRECTORY ? UsuarioTipo.USUARIO_ACTIVE_DIRECTORY : UsuarioTipo.USUARIO_NORMAL),
            (short) 0, null, rolRepository.findById(request.getRolId()).get(), UsuarioEstado.HABILITADO,
            fechaActualizacion));*/
            String pass = (request.getTipo().equals(TipoAutenticacion.LDAP.getId())) ? (String) parametroService.getParamVal(Parametro.DelSistema.CONTRASENA_POR_DEFECTO.name()) : request.getPass();
            //passwordEncoder
            //LoggerWeb.debug("***** OAC pass:" + pass);
            pass = (pass == null || pass.isEmpty()) ? null : passwordEncoder.encode(ConfigEncriptacion.atob(pass));
            Usuario usuario = new Usuario(null, request.getNombreUsuario(), name,
                    pass,
                    request.getTipo(), (short) 0, null,
                    rolRepository.findById(request.getRolId()).get(), UsuarioEstado.HABILITADO,
                    fechaActualizacion);
            LoggerWeb.debug("***** OAC usuario:" + usuario);
            Usuario model = repository.save(usuario);
            LoggerWeb.debug("***** OAC DESUES SAVE:" + usuario);
            
            map.put(TiposComunes.ModuloBase.USUARIO, bo.com.micrium.modulobase.commons.ConvercionUtil.toJson(model));
            
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_CREAR, null, map);
            
            ResponseEntity<UsuarioResponse> out = ResponseEntity.created(new URI("/usuarios/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, UsuarioResponse.class));
            
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            return out;
        } catch (EncriptacionExcepcion | ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un usuario, " + e.getMessage();
            
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_CREAR, null, map, logSistemaId);
            
            throw new ApiException(result, mensajeError, e);
        }
    }
    
    @Override
    public ResponseEntity<UsuarioResponse> update(String token, String ipClient, String form,
            UsuarioRequest request, String id, BindingResult result) throws ApiException {
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> mapNuevo = new HashMap<String, String>();
        
        try {
            ipClient = obtenerIp(ipClient);
            
            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("id ", id),
                new AbstractMap.SimpleEntry<>("request ", request)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);
            validator.validate(request, idDesencriptado, result);
            if (result.hasErrors()) {
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                //bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_MODIFICAR, null, map);
                throw new ApiException(result, "Errores en la validacion");
            }
            Usuario model = repository.findById(idDesencriptado).get();
            //  OAC LDAP
            //if (model.getTipo() == UsuarioTipo.USUARIO_ACTIVE_DIRECTORY) {
            if (request.getTipo().equals(TipoAutenticacion.LDAP.getId())) {
                try {
                    model.setNombreCompleto(new ActiveDirectoryService(parametroService).getNombreCompleto(request.getNombreUsuario()));
                } catch (LdapContextException ex) {
                    //logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_MODIFICAR, "Error de conexion ldap " + ex.getMessage(), ex);
                    result.rejectValue("nombreUsuario", "field.nombreUsuario", "Fallo con la conexión con active directory.");
                    map.put(TiposComunes.ERROR, obtenerErrores(result));
                    //bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_CREAR, null, map);
                    throw new ApiException(result, "Fallo con la conexión con active directory.");
                }
            }
            
            map.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));

            // model.setNombreUsuario(request.getNombreUsuario());
            String pass = (request.getTipo().equals(TipoAutenticacion.LDAP.getId())) ? (String) parametroService.getParamVal(Parametro.DelSistema.CONTRASENA_POR_DEFECTO.name()) : request.getPass();
            model.setRolId(rolRepository.findById(request.getRolId()).get());
            if (pass != null) {
                model.setContrasena(passwordEncoder.encode(ConfigEncriptacion.atob(pass)));
            }
            
            model = repository.save(model);
            mapNuevo.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_MODIFICAR, map, mapNuevo);
            ResponseEntity<UsuarioResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, UsuarioResponse.class));
            
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            
            return out;
        } catch (EncriptacionExcepcion | ApiException e) {
            final String mensajeError = "Error al actualizar un usuario, " + e.getMessage();
            
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_MODIFICAR, map, null, logSistemaId);
            
            throw new ApiException(result, mensajeError, e);
        }
    }

    /*
     * ya no se eliminara usuario
     * 
     */
    @Deprecated
    @Override
    public ResponseEntity<?> delete(String token, String ipClient, String form,
            String id) throws ApiException {
        /*
        ipClient = obtenerIp(ipClient);
         StringBuilder sb = new StringBuilder();
        sb.append("-------------------REQUEST----------------------\n").
                append("token ").append(token).append(", \n").
                append("form ").append(form).append(", \n").
                append("ipClient ").append(ipClient).append(", \n").
                append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                append("id ").append(id).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());

        Optional<Usuario> temp = repository.findById(id);

        if (!temp.isPresent()) {
            throw new NoHandlerFoundException("DELETE", "/{id}" + id, HttpHeaders.EMPTY);
        }

        Usuario model = temp.get();

        if (model.getEstado() == UsuarioEstado.INHABILITADO) {
            return ResponseEntity.noContent().build();
        }

        model.setEstado(UsuarioEstado.INHABILITADO);

        repository.save(model);

        bitacoraService.guardarBitacora(token, ipClient, form, "Se elimino:" + model);

        ResponseEntity<Object> out = ResponseEntity.ok().build();

        sb = new StringBuilder();
        sb.append("-------------------RESPONSE----------------------\n").
                append("token ").append(token).append(", \n").
                append("DATA ").append(out).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());*/
        throw new ApiException("DELETE not support method /{id}" + id);
    }
    
    @GetMapping("/contrasena/reseteo/{id}")
    public ResponseEntity<?> resetearContrasena(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @PathVariable Long id) throws RuntimeException {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> mapNuevo = new HashMap<>();
        
        try {
            ipClient = obtenerIp(ipClient);
            
            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("id ", id)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            Optional<Usuario> optional = repository.findById(id);
            
            if (!optional.isPresent()) {
                map.put(TiposComunes.MENSAJE_ERROR, "Identificador invalido.");
                bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_RESETEAR_CONTRASENA, map, null);
                
                throw new RuntimeException("Identificador de usuario invalido.");
            }
            
            Usuario model = optional.get();
            
            map.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            model.setContrasena((String) parametroService.getParamVal(Parametro.DelSistema.CONTRASENA_POR_DEFECTO.name()));
            model = repository.save(model);
            
            mapNuevo.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_RESETEAR_CONTRASENA, map, mapNuevo);
            ResponseEntity<Object> out = ResponseEntity.ok().build();
            
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            return out;
        } catch (RuntimeException e) {
            final String mensajeError = "Error al resetear la contraseña del usuario id: " + id + ", " + e.getMessage();
            
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_RESETEAR_CONTRASENA, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_RESETEAR_CONTRASENA, map, mapNuevo, logSistemaId);
            
            throw e;
        }
    }
    
    @PutMapping("/inhabilitar")
    ResponseEntity<?> deshabilitar(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @RequestBody String id) {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> mapNuevo = new HashMap<>();
        
        try {
            ipClient = obtenerIp(ipClient);
            
            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("id ", id)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            Long idusuario = Long.valueOf(id);
            Optional<Usuario> optional = repository.findById(idusuario);
            
            Usuario model = optional.get();
            map.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            model.setEstado(UsuarioEstado.INHABILITADO);
            model = repository.save(model);
            
            mapNuevo.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_INHABILITAR, map, mapNuevo);
            ResponseEntity<Object> out = ResponseEntity.ok().build();
            
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            
            return out;
        } catch (NumberFormatException e) {
            final String mensajeError = "Error al deshabilitar un usuario id: " + id + ", " + e.getMessage();
            
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_INHABILITAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_INHABILITAR, map, mapNuevo, logSistemaId);
            
            throw e;
        }
    }
    
    @PutMapping("/habilitar")
    ResponseEntity<?> habilitar(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @RequestBody String id) {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> mapNuevo = new HashMap<>();
        
        try {
            ipClient = obtenerIp(ipClient);
            
            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("id ", id)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            
            Long idusuario = Long.valueOf(id);
            Optional<Usuario> optional = repository.findById(idusuario);
            
            Usuario model = optional.get();
            
            map.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            model.setEstado(UsuarioEstado.HABILITADO);
            model = repository.save(model);
            
            mapNuevo.put(TiposComunes.ModuloBase.USUARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_HABILITAR, map, mapNuevo);
            ResponseEntity<Object> out = ResponseEntity.ok().build();
            
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            
            return out;
        } catch (NumberFormatException e) {
            final String mensajeError = "Error al habilitar un usuario id: " + id + ", " + e.getMessage();
            
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_HABILITAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_HABILITAR, map, mapNuevo, logSistemaId);
            
            throw e;
        }
    }
    
}
