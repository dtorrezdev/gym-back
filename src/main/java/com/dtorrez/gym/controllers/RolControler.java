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
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;
import java.util.Arrays;
import java.net.URI;
import java.util.List;
import java.util.Map;

import bo.com.micrium.modulobase.commons.*;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Rol;
import com.micrium.bd.access.jpa.models.RolTipoParametroPermiso;
import com.micrium.bd.access.jpa.models.TipoParametro;
import com.dtorrez.gym.controllers.dto.RolRequest;
import com.dtorrez.gym.controllers.dto.RolResponse;
import com.micrium.bd.access.jpa.repositories.IGrupoRepository;
import com.micrium.bd.access.jpa.repositories.IParametroRepository;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import com.micrium.bd.access.jpa.repositories.IRolTipoparametroRepository;
import com.micrium.bd.access.jpa.repositories.ITipoParametroRepository;
import com.dtorrez.gym.security.utils.JwtTokenUtil;
import com.dtorrez.gym.validators.RolValidator;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.exception.PageException;
import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.logger.LoggerMain;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/roles", produces = {MediaType.APPLICATION_JSON_VALUE})
public class RolControler extends GenericControler implements ICrudControler<RolRequest, RolResponse, String> {

    private static final long serialVersionUID = 9081953002167441022L;

    @Autowired
    private transient IRolRepository repository;

    @Autowired
    private transient IGrupoRepository grupoRepository;

    @Autowired
    private transient RolValidator validator;

    @Autowired
    private transient ITipoParametroRepository tipoParametroRepository;

    @Autowired
    private transient IRolTipoparametroRepository rolTipoparametroRepository;

    @Autowired
    private transient IParametroRepository parametroRepository;

    @Override
    public Page<RolResponse> list(String token, String ipClient, String form, Pageable pageRequest) throws ApiException {
        
        try {
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);    
            validator.validateListar(parametros);        
        
            final String nombre = filterTextoQueryUpper(parametros.get("nombre"));
            final String descripcion = filterTextoQueryUpper(parametros.get("descripcion"));

            parametros.forEach((key,value) -> {
                LoggerMain.info("key={},  value={}", key,value);                
            });
            
            ipClient = obtenerIp(ipClient);

            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("nombre ", filterTexto(nombre)),
                new AbstractMap.SimpleEntry<>("descripcion ", filterTexto(descripcion)),
                new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<RolResponse> out = repository.filter(
                queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre),
                queryfilterTexto(descripcion), filterTextoQueryUpperLike(descripcion),
                Rol.SUPER_ADMINISTRADOR, pageRequest
            ).map(model -> {
                RolResponse convertToObject = ConvercionUtil.convertToObject(model, RolResponse.class);
                return convertToObject;
            });

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, null);
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out),
                new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            return out;            
        } catch (PageException | ValidateException  e) {
            final String mensajeError = "Error al filtrar roles, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw new ApiException( mensajeError, e);
        }

    }

    @Override
    public ResponseEntity<RolResponse> get(String token, String ipClient,
            String form, String id) throws ApiException {
        /*ipClient = obtenerIp(ipClient);

        LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("id ", (id == null) ? "" : id)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        Optional<Rol> model = repository.findById(id);
        if (model.isPresent()) {
            ResponseEntity<RolResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertir(model.get()));

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        }*/

        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<RolResponse> create(String token, String ipClient, String form,
            RolRequest request, BindingResult result) throws URISyntaxException, ApiException {
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
                throw new ApiException(result, "Errores en la validacion");
            }

            Rol model = repository.save(new Rol(null, request.getNombre(), request.getDescripcion(), RolEstado.HABILITADO));
            map.put(TiposComunes.ModuloBase.ROL, ConvercionUtil.toJson(model));
            List<RolTipoParametroPermiso> temp = new ArrayList<>();

            for (TipoParametro tipoParametro : tipoParametroRepository.findAll()) {
                RolTipoParametroPermiso modelRTP = new RolTipoParametroPermiso(null, model.getId(), tipoParametro.getId(), PermisoTipo.PERMISO_NINGUNO);
                modelRTP = rolTipoparametroRepository.save(modelRTP);
                temp.add(modelRTP);
            }

            map.put(TiposComunes.ModuloBase.ROL_TIPOS_PARAMETROS, ConvercionUtil.toJson(temp));

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_CREAR, null, map);

            ResponseEntity<RolResponse> out = ResponseEntity.created(
                    new URI("/roles/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, RolResponse.class));
            //.body(ConvercionUtil.convertir(model));

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un rol, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.ROL_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_CREAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<RolResponse> update(String token, String ipClient, String form,
            RolRequest request, String id, BindingResult result) throws ApiException {
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
                throw new ApiException(result, "Errores en la validacion");
            }

            Rol model = repository.findById(idDesencriptado).get();

            map.put(TiposComunes.ModuloBase.ROL, ConvercionUtil.toJson(model));
            model.setNombre(request.getNombre());
            model.setDescripcion(request.getDescripcion());
            model = repository.save(model);

            mapNuevo.put(TiposComunes.ModuloBase.ROL, ConvercionUtil.toJson(model));

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_MODIFICAR, map, mapNuevo);
            
            ResponseEntity<RolResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, RolResponse.class));

            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            return out;
        } catch (ApiException | EncriptacionExcepcion e) {
            final String mensajeError = "Error al modificar un rol, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.USUARIO_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.USUARIO_MODIFICAR, map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token, String ipClient, String form,
            String id) throws ApiException {
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
            
            id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);

            Optional<Rol> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                throw new NoHandlerFoundException("DELETE", "/{id}" + id, HttpHeaders.EMPTY);
            }

            Rol model = temp.get();

            map.put(TiposComunes.ModuloBase.ROL, ConvercionUtil.toJson(model));

            if (model.getEstado() == RolEstado.INHABILITADO) {
                return ResponseEntity.noContent().build();
            }

            long numeroUsuariosConRol = usuarioRepository.
                    countByEstadoInAndRolIdId(Arrays.asList(
                            UsuarioEstado.HABILITADO, UsuarioEstado.BLOQUEADO), idDesencriptado);

            if (numeroUsuariosConRol > 0) {
                throw new RuntimeException("No puede ser eliminado porque esta en uso por almenos un usuario");
            }

            long numeroGruposConRol = grupoRepository.countByEstadoAndRolId(GrupoEstado.HABILITADO, model);

            if (numeroGruposConRol > 0) {
                throw new RuntimeException("No puede ser eliminado porque esta en uso por almenos un grupo");
            }

            model.setEstado(RolEstado.INHABILITADO);
            repository.save(model);
            mapNuevo.put(TiposComunes.ModuloBase.ROL, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_ELIMINAR, map, mapNuevo);

            ResponseEntity<Object> out = ResponseEntity.ok().build();

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (EncriptacionExcepcion | RuntimeException | NoHandlerFoundException e) {
            final String mensajeError = "Error al eliminar un rol, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.ROL_ELIMINAR,
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_ELIMINAR, map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }

    @PostMapping("/cargamasivo")
    public ResponseEntity<?> upload(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody RolRequest[] request, BindingResult result) throws NoHandlerFoundException, ApiException, Exception {
        HashMap<String, String> map = new HashMap<>();
        try {
            ipClient = obtenerIp(ipClient);
            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("request ", request)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            validator.validatePlus(request, result);
            if (result.hasErrors()) {
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                throw new ApiException(result, "Errores en la validacion");
            }            

            List<Rol> roles = ConvercionUtil.convertToListObject(request, Rol.class);
            Integer batchSize = Integer.valueOf(parametroRepository.findByNombre(
                Parametro.DelSistema.BATCH_SIZE.name()).getValor());
            for (int i = 0; i < roles.size(); i += batchSize) {
                List<Rol> batch = roles.subList(i, Math.min(i + batchSize, roles.size()));
                repository.saveAll(batch);
            }

            bitacoraService.guardarBitacora(token, ipClient, form, "ROL_CARGA_MASIVA " + request.length, null, map);
            ResponseEntity<Object> out = ResponseEntity.ok().build();
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", "Registro completo")).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            return out;
        } catch (ApiException e) {
            final String mensajeError = "Error al cargar registros de roles, " + e.getMessage();
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "ROL_CARGA_MASIVA", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "ROL_CARGA_MASIVA", null, map, logSistemaId);
            throw e;
        }
    }
}
