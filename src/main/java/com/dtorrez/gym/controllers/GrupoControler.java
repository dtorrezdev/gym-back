/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.controllers;

import java.util.stream.Collectors;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;

import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;
import com.micrium.bd.access.jpa.models.Grupo;
import com.dtorrez.gym.controllers.dto.GrupoRequest;
import com.dtorrez.gym.controllers.dto.GrupoResponse;
import com.micrium.bd.access.jpa.repositories.IGrupoRepository;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import com.dtorrez.gym.validators.GrupoValidator;
import bo.com.micrium.modulobase.commons.GrupoEstado;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/grupos", produces = {MediaType.APPLICATION_JSON_VALUE})
public class GrupoControler extends GenericControler implements ICrudControler<GrupoRequest, GrupoResponse, String> {

    private static final long serialVersionUID = -69171311878619585L;

    @Autowired
    private transient IGrupoRepository repository;

    @Autowired
    private transient IRolRepository rolRepository;

    @Autowired
    private transient GrupoValidator validator;
    

    @Override
    public Page<GrupoResponse> list(String token, String ipClient, String form, Pageable pageRequest) throws Exception {

        try {
            validator.page(this.httpServletRequest.getParameter("size"), this.httpServletRequest.getParameter("page"), this.httpServletRequest.getParameter("sort"));

            final String nombre = this.httpServletRequest.getParameter("nombre");
            final String descripcion = this.httpServletRequest.getParameter("descripcion");
            final String rolNombre = this.httpServletRequest.getParameter("rolNombre");

            if (!isBlanck(nombre) && (nombre.length() > 100)) {
                throw new Exception("La longitud del nombre no debe ser mayor a 100.");
            }

            if (!isBlanck(descripcion) && (descripcion.length() > 255)) {
                throw new Exception("La longitud de descripcion no debe ser mayor a 255.");
            }

            if (!isBlanck(rolNombre) && (rolNombre.length() > 50)) {
                throw new Exception("La longitud del rolNombre no debe ser mayor a 50.");
            }

            ipClient = obtenerIp(ipClient);
        
            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("nombre ", nombre),
                    new AbstractMap.SimpleEntry<>("descripcion ", descripcion),
                    new AbstractMap.SimpleEntry<>("rolNombre ", rolNombre),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("pageRequest ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            Page<GrupoResponse> out = repository.filter(((nombre == null || nombre.isEmpty()) ? -1 : 0), ((nombre == null || nombre.trim().isEmpty()) ? "" : "%" + nombre.trim().toUpperCase() + "%"),
                    ((descripcion == null || descripcion.isEmpty()) ? -1 : 0), ((descripcion == null || descripcion.trim().isEmpty()) ? "" : "%" + descripcion.trim().toUpperCase() + "%"),
                    ((rolNombre == null || rolNombre.isEmpty()) ? -1 : 0), ((rolNombre == null || rolNombre.trim().isEmpty()) ? "" : "%" + rolNombre.trim().toUpperCase() + "%"),
                    pageRequest).map(model -> {
                        GrupoResponse grupoResponse = ConvercionUtil.convertToObject(model, GrupoResponse.class);
                        grupoResponse.setRolId(model.getRolId().getId());
                        grupoResponse.setRolNombre(model.getRolId().getNombre());
                        return grupoResponse;
                    });
            
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out),
                    new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;

        } catch (Exception e) {
            final String mensajeError = "Error al filtrar grupo, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);
            throw e;
        }
    }

    @Override
    public ResponseEntity<GrupoResponse> get(String token, String ipClient, String form,
            String id) throws ApiException {        
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<GrupoResponse> create(String token, String ipClient, String form,
            GrupoRequest request, BindingResult result) throws URISyntaxException, ApiException {
        HashMap<String, String> map = new HashMap<>();
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

            validator.validate(request, null, result);
            if (result.hasErrors()) {
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                throw new ApiException(result, "Errores en la validacion");
            }

            Grupo model = repository.save(new Grupo(null, request.getNombre(), request.getDescripcion(),
                    rolRepository.findById(request.getRolId()).get(), GrupoEstado.HABILITADO));

            map.put(TiposComunes.ModuloBase.GRUPO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.GRUPO_CREAR, null, map);

            ResponseEntity<GrupoResponse> out = ResponseEntity.created(new URI("/grupos/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, GrupoResponse.class));
  
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un grupo. " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.GRUPO_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.GRUPO_CREAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<GrupoResponse> update(String token, String ipClient, String form,
            GrupoRequest request, String id, BindingResult result) throws ApiException {
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
                    new AbstractMap.SimpleEntry<>("id ", id),
                    new AbstractMap.SimpleEntry<>("request ", request)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);
            validator.validate(request, idDesencriptado, result);

            if (result.hasErrors()) {
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                throw new ApiException(result, "Errores en la validacion");
            }

            Grupo model = repository.findById(idDesencriptado).get();
            map.put(TiposComunes.ModuloBase.GRUPO, ConvercionUtil.toJson(model));

            model.setNombre(request.getNombre());
            model.setDescripcion(request.getDescripcion());
            model.setRolId(rolRepository.findById(request.getRolId()).get());

            model = repository.save(model);
            mapNuevo.put(TiposComunes.ModuloBase.GRUPO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.GRUPO_MODIFICAR, map, mapNuevo);

            ResponseEntity<GrupoResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, GrupoResponse.class));

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (EncriptacionExcepcion | ApiException e) {
            final String mensajeError = "Error al actualizar un grupo, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.GRUPO_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.GRUPO_MODIFICAR, map, null, logSistemaId);

            throw new ApiException(result, mensajeError, e);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token, String ipClient, String form,
            String id) throws ApiException {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> mapNuevo = new HashMap<>();

        ipClient = obtenerIp(ipClient);
        try {
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
            Optional<Grupo> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                map.put(TiposComunes.ERROR, "El objeto buscado no se encuentra en la BD");
                throw new NoHandlerFoundException("DELETE", "/{id}" + id, HttpHeaders.EMPTY);
            }

            Grupo model = temp.get();
            if (model.getEstado().equals(GrupoEstado.INHABILITADO)) {
                return ResponseEntity.noContent().build();
            }
            map.put(TiposComunes.ModuloBase.GRUPO, ConvercionUtil.toJson(model));

            model.setEstado(GrupoEstado.INHABILITADO);
            repository.save(model);
            mapNuevo.put(TiposComunes.ModuloBase.GRUPO, ConvercionUtil.toJson(model));

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.GRUPO_ELIMINAR, map, mapNuevo);
            ResponseEntity<Object> out = ResponseEntity.ok().build();

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (EncriptacionExcepcion | RuntimeException | NoHandlerFoundException e) {
            final String mensajeError = "Error al eliminar un grupo, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.GRUPO_ELIMINAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.GRUPO_ELIMINAR, map, mapNuevo, logSistemaId);

            throw new ApiException(mensajeError, e);
        }
    }

}
