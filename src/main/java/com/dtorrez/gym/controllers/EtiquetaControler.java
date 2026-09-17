/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.AbstractMap;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
//import bo.com.micrium.modulobase.commons.ConvercionUtil;
import com.micrium.bd.access.jpa.models.Etiqueta;
import com.micrium.bd.access.jpa.repositories.IEtiquetaRepository;
import com.dtorrez.gym.controllers.dto.EtiquetaRequest;
import com.dtorrez.gym.controllers.dto.EtiquetaResponse;
// import bo.com.micrium.modulobase.security.utils.JWTTokenUtil;
import com.dtorrez.gym.validators.EtiquetaValidator;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;


/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = EtiquetaControler.RESOURCE, produces = {MediaType.APPLICATION_JSON_VALUE})
public class EtiquetaControler extends GenericControler implements ICrudControler<EtiquetaRequest, EtiquetaResponse, String> {

    public static final String RESOURCE = "/etiquetas";
    public static final String RESOURCE_BY_LLAVE = RESOURCE + "/by/llave";
    public static final String RESOURCE_BY_GRUPO = RESOURCE + "/by/grupo";

    private static final long serialVersionUID = -7116684103790631404L;

    @Autowired
    transient IEtiquetaRepository repository;

    @Autowired
    private transient EtiquetaValidator validator;

    private static final short ESTADO_ACTIVO = 1;

    @PostMapping("/by/llave")
    List<EtiquetaResponse> findAllByLlaveIn(@Valid @RequestBody List<String> request) {
        String ipClient = obtenerIp(null);
        LoggerMain.printRequest(Stream.of(
            new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
            new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()), 
            new AbstractMap.SimpleEntry<>("request ", request),
            new AbstractMap.SimpleEntry<>("ipClient ", ipClient)).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        
        List<EtiquetaResponse> out = (List<EtiquetaResponse>) this.repository.findAllByLlaveIn(request)
            .stream().map( model -> ConvercionUtil.convertToObject(model, EtiquetaResponse.class)
            ).collect(Collectors.toList());
        
        LoggerMain.printResponse(Stream.of(
            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
            new AbstractMap.SimpleEntry<>("response ", out)).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        return out;
    }

    @PostMapping("/by/grupo")
    List<EtiquetaResponse> findAllByGrupo(@Valid @RequestBody List<String> request) {
        String ipClient = obtenerIp(null);
        LoggerMain.printRequest(Stream.of(
            new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
            new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),                    
            new AbstractMap.SimpleEntry<>("request ", request),
            new AbstractMap.SimpleEntry<>("ipClient ", ipClient)).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        
        List<EtiquetaResponse> out = repository.findAllByGrupoIn(request)
            .stream().map(model -> ConvercionUtil.convertToObject(model, EtiquetaResponse.class)
            ).collect(Collectors.toList());

        LoggerMain.printResponse(Stream.of(
            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
            new AbstractMap.SimpleEntry<>("response ", out)).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        return out;
    }

    @PostMapping("/by/grupostoken")
    List<EtiquetaResponse> findAllByGrupo2(@Valid @RequestBody List<String> request) {
        String ipClient = obtenerIp(null);
        LoggerMain.printRequest(Stream.of(
            new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
            new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),                    
            new AbstractMap.SimpleEntry<>("request ", request),
            new AbstractMap.SimpleEntry<>("ipClient ", ipClient)).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        List<EtiquetaResponse> out = repository.findAllByGrupoIn(request)
            .stream().map(model -> ConvercionUtil.convertToObject(model, EtiquetaResponse.class)
            ).collect(Collectors.toList());
        
        LoggerMain.printResponse(Stream.of(
            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
            new AbstractMap.SimpleEntry<>("response ", out)).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        return out;
    }

    @Override
    public Page<EtiquetaResponse> list(String token, String ipClient, String form, Pageable pageRequest) throws Exception {

        try {
            
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);            

            final String llave = parametros.get("llave");
            final String grupo = parametros.get("grupo");
            final String valor = parametros.get("valor");
            
            ipClient = obtenerIp(ipClient);
            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<EtiquetaResponse> out = repository.filter(
                    (isBlanck(llave) ? -1 : 0), (isBlanck(llave) ? "" : "%" + llave.trim().toUpperCase() + "%"),
                    (isBlanck(valor) ? -1 : 0), (isBlanck(valor) ? "" : "%" + valor.trim().toUpperCase() + "%"),
                    (isBlanck(grupo) ? -1 : 0), (isBlanck(grupo) ? "" : "%" + grupo.trim().toUpperCase() + "%"),
                    pageRequest)
                    .map(model -> ConvercionUtil.convertToObject(model, EtiquetaResponse.class));
            
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, null);
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out),
                new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            return out;            
        } catch (Exception e) {
            final String mensajeError = "Error al filtrar etiquetas, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);
            throw e;
        }
    }

    @Override
    public ResponseEntity<EtiquetaResponse> get(String token, String ipClient, String form,
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

        Optional<Etiqueta> model = repository.findById(id);
        if (model.isPresent()) {
            ResponseEntity<EtiquetaResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertir(model.get()));

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
    public ResponseEntity<EtiquetaResponse> create(String token, String ipClient, String form,
            EtiquetaRequest request, BindingResult result) throws URISyntaxException, ApiException {

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
                map.put(TiposComunes.MENSAJE_ERROR, obtenerErrores(result));
                bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ETIQUETA_CREAR, null, map);
                throw new ApiException(result, "Errores en la validacion");
            }

            Etiqueta model = repository.save(new Etiqueta(null, request.getLlave(), request.getValor(), request.getGrupo(), ESTADO_ACTIVO));
            map.put(TiposComunes.ModuloBase.ROL, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ETIQUETA_CREAR, null, map);

            ResponseEntity<EtiquetaResponse> out = ResponseEntity.created(new URI("/etiquetas/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, EtiquetaResponse.class));
           
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear una etiqueta, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.ETIQUETA_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ETIQUETA_CREAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<EtiquetaResponse> update(String token, String ipClient, String form,
            EtiquetaRequest request, String id, BindingResult result) throws ApiException {

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
            Long idDesenciptado = ConfigEncriptacion.desencryptIdToConvertLong(id);
            validator.validate(request, idDesenciptado, result);

            if (result.hasErrors()) {
                map.put(TiposComunes.MENSAJE_ERROR, obtenerErrores(result));
                bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ETIQUETA_MODIFICAR, null, map);
                throw new ApiException(result, "Errores en la validacion");
            }

            Etiqueta model = repository.findById(idDesenciptado).get();
            map.put(TiposComunes.ModuloBase.ETIQUETA, ConvercionUtil.toJson(model));

            model.setValor(request.getValor());
            model = repository.save(model);
            mapNuevo.put(TiposComunes.ModuloBase.ETIQUETA, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ETIQUETA_MODIFICAR, map, mapNuevo);

            ResponseEntity<EtiquetaResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, EtiquetaResponse.class));
            
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            return out;
        } catch (ApiException | EncriptacionExcepcion e) {
            final String mensajeError = "Error al modificar una etiqueta, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.ETIQUETA_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.ETIQUETA_MODIFICAR, map, mapNuevo, logSistemaId);
            
            throw new ApiException(result, e.getMessage(), e);
        }
        
    }

    @Override
    public ResponseEntity<?> delete(String token, String ipClient, String form, String id) throws ApiException {
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

        Optional<Etiqueta> temp = repository.findById(id);

        if (!temp.isPresent()) {
            throw new NoHandlerFoundException("DELETE", "/{id}" + id, HttpHeaders.EMPTY);
        }

        Etiqueta model = temp.get();

        repository.delete(model);

        bitacoraService.guardarBitacora(token, ipClient, form, "Se elimino:" + model);

        ResponseEntity<Object> out = ResponseEntity.ok().build();

        sb = new StringBuilder();
        sb.append("-------------------RESPONSE----------------------\n").
                append("token ").append(token).append(", \n").
                append("DATA ").append(out).append(", \n").
                append("------------------------------------------------\n");

        log.info(sb.toString());

        return out;*/

        throw new ApiException("DELETE not support method " + "/{id}" + id);
    }

}
