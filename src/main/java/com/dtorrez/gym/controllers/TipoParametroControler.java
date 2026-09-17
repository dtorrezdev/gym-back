/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.controllers;

import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;
import com.micrium.bd.access.jpa.models.TipoParametro;
import com.dtorrez.gym.controllers.dto.TipoParametroRequest;
import com.dtorrez.gym.controllers.dto.TipoParametroResponse;
import com.micrium.bd.access.jpa.repositories.ITipoParametroRepository;
import com.dtorrez.gym.validators.TipoParametroValidator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/tipos/parametros", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TipoParametroControler extends GenericControler implements ICrudControler<TipoParametroRequest, TipoParametroResponse, String> {

    private static final long serialVersionUID = 2546243156433770443L;

    @Autowired
    private transient ITipoParametroRepository repository;

    @Autowired
    private transient TipoParametroValidator validator;     //  37334 coverity

    @Override
    public Page<TipoParametroResponse> list(String token, String ipClient, String form, Pageable pageRequest) {
        ipClient = obtenerIp(ipClient);

        /*StringBuilder sb = new StringBuilder();
        sb.append("-------------------REQUEST----------------------\n").
                append("token ").append(token).append(", \n").
                append("form ").append(form).append(", \n").
                append("ipClient ").append(ipClient).append(", \n").
                append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                append("pageRequest ").append(pageRequest).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());*/
        LoggerMain.printRequest(Stream.of(
                        new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                        new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("form ", form),
                        new AbstractMap.SimpleEntry<>("pageRequest ", pageRequest)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        Page<TipoParametroResponse> out = repository.findAll(pageRequest).map(model -> ConvercionUtil.convertToObject(model, TipoParametroResponse.class));

        /*sb = new StringBuilder();
        sb.append("-------------------RESPONSE----------------------\n").
                append("token ").append(token).append(", \n").
                append("DATA ").append(out).append(", \n").
                append("CONTENT ").append(out.getContent()).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());*/
        LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out),
                        new AbstractMap.SimpleEntry<>("Content ", out.getContent())).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return out;
    }

    @Override
    public ResponseEntity<TipoParametroResponse> get(String token, String ipClient, 
            String form, String id) throws ApiException {

        try {
            ipClient = obtenerIp(ipClient);

            /*StringBuilder sb = new StringBuilder();
            sb.append("-------------------REQUEST----------------------\n").
                    append("token ").append(token).append(", \n").
                    append("form ").append(form).append(", \n").
                    append("ipClient ").append(ipClient).append(", \n").
                    append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                    append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                    append("id ").append(id).append(", \n").
                    append("------------------------------------------------\n");
            
            log.info(sb.toString());*/
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
                    
            Optional<TipoParametro> model = repository.findById(idDesencriptado);
            if (model.isPresent()) {
                ResponseEntity<TipoParametroResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model.get(), TipoParametroResponse.class));

                /*sb = new StringBuilder();
                sb.append("-------------------RESPONSE----------------------\n").
                        append("token ").append(token).append(", \n").
                        append("DATA ").append(out).append(", \n").
                        append("------------------------------------------------\n");

                log.info(sb.toString());*/
                LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                return out;
            }
        } catch (EncriptacionExcepcion e) {
            
            throw new ApiException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResponseEntity<TipoParametroResponse> create(String token, String ipClient, String form,
            TipoParametroRequest request, BindingResult result) throws URISyntaxException, ApiException {
        ipClient = obtenerIp(ipClient);

        /*StringBuilder sb = new StringBuilder();
        sb.append("-------------------REQUEST----------------------\n").
                append("token ").append(token).append(", \n").
                append("form ").append(form).append(", \n").
                append("ipClient ").append(ipClient).append(", \n").
                append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                append("request ").append(request).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());*/
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
            throw new ApiException(result, "Errores en la validacion");
        }

        TipoParametro model = repository.save(new TipoParametro(null, request.getNombre(), request.getDescripcion()));

        bitacoraService.guardarBitacora(token, ipClient, form, "Se adiciono:" + model);

        ResponseEntity<TipoParametroResponse> out = ResponseEntity.created(new URI("/usuarios/" + model.getId()))
                .body(ConvercionUtil.convertToObject(model, TipoParametroResponse.class));

        /*sb = new StringBuilder();
        sb.append("-------------------RESPONSE----------------------\n").
                append("token ").append(token).append(", \n").
                append("DATA ").append(out).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());*/
        LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return out;
    }

    @Override
    public ResponseEntity<TipoParametroResponse> update(String token, String ipClient, String form,
            TipoParametroRequest request, String id, BindingResult result) throws ApiException {
        HashMap<String, String> map = new HashMap();
        HashMap<String, String> mapNuevo = new HashMap();
        try {
            ipClient = obtenerIp(ipClient);

            /*StringBuilder sb = new StringBuilder();
            sb.append("-------------------REQUEST----------------------\n").
                    append("token ").append(token).append(", \n").
                    append("form ").append(form).append(", \n").
                    append("ipClient ").append(ipClient).append(", \n").
                    append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                    append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                    append("request ").append(request).append(", \n").
                    append("id ").append(id).append(", \n").
                    append("------------------------------------------------\n");

            log.info(sb.toString());*/
            LoggerMain.printRequest(Stream.of(
                        new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                        new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("form ", form),
                        new AbstractMap.SimpleEntry<>("request ", request),
                        new AbstractMap.SimpleEntry<>("id ", id)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            
            id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);
            validator.validate(request, idDesencriptado, result);

            if (result.hasErrors()) {
                map.put(TiposComunes.MENSAJE_ERROR, obtenerErrores(result));
                throw new ApiException(result, "Errores en la validacion");
            }

            TipoParametro model = repository.findById(idDesencriptado).get();

            map.put(TiposComunes.ModuloBase.ROL_TIPOS_PARAMETROS, ConvercionUtil.toJson(model));
            model.setNombre(request.getNombre());
            model.setDescripcion(request.getDescripcion());
            model = repository.save(model);

            mapNuevo.put(TiposComunes.ModuloBase.ROL_TIPOS_PARAMETROS, ConvercionUtil.toJson(model));

            bitacoraService.guardarBitacora(token, ipClient, form, "Se modifico:" + model);

            ResponseEntity<TipoParametroResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, TipoParametroResponse.class));

            /*sb = new StringBuilder();
            sb.append("-------------------RESPONSE----------------------\n").
                    append("token ").append(token).append(", \n").
                    append("DATA ").append(out).append(", \n").
                    append("------------------------------------------------\n");

            log.info(sb.toString());*/
            LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (EncriptacionExcepcion | ApiException e) {
            final String mensajeError = "Error al modificar un tipo_parametro, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.ROL_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_MODIFICAR, map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token, String ipClient, String form,
            String id) throws ApiException {
        HashMap<String, String> map = new HashMap();
        HashMap<String, String> mapNuevo = new HashMap();
        try {
            ipClient = obtenerIp(ipClient);

            /*StringBuilder sb = new StringBuilder();
            sb.append("-------------------REQUEST----------------------\n").
                    append("token ").append(token).append(", \n").
                    append("form ").append(form).append(", \n").
                    append("ipClient ").append(ipClient).append(", \n").
                    append("url ").append(httpServletRequest.getRequestURL()).append(", \n").
                    append("metodo ").append(httpServletRequest.getMethod()).append(", \n").
                    append("id ").append(id).append(", \n").
                    append("------------------------------------------------\n");

            log.info(sb.toString());*/
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
            Optional<TipoParametro> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                throw new NoHandlerFoundException("DELETE", "/{id}" + id, HttpHeaders.EMPTY);
            }

            map.put(TiposComunes.ModuloBase.ROL_TIPOS_PARAMETROS, ConvercionUtil.toJson(temp.get()));

            repository.delete(temp.get());

            bitacoraService.guardarBitacora(token, ipClient, form, "Se elimino:" + temp.get());

            ResponseEntity<Object> out = ResponseEntity.ok().build();

            /*sb = new StringBuilder();
            sb.append("-------------------RESPONSE----------------------\n").
                    append("token ").append(token).append(", \n").
                    append("DATA ").append(out).append(", \n").
                    append("------------------------------------------------\n");

            log.info(sb.toString());*/
            LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (EncriptacionExcepcion | NoHandlerFoundException e) {
            final String mensajeError = "Error al eliminar un " + TiposComunes.ModuloBase.ROL_TIPOS_PARAMETROS + ", " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.ROL_ELIMINAR,
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ROL_ELIMINAR, map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }

}
