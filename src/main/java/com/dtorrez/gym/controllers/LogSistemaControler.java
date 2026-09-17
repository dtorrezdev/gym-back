/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.controllers;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micrium.bd.access.jpa.repositories.ILogSistemaRepository;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.dto.LogSistemaResponse;
import com.dtorrez.gym.security.utils.JwtTokenUtil;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.BaseDate;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import java.util.HashMap;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/logs/sistema", produces = {MediaType.APPLICATION_JSON_VALUE})
public class LogSistemaControler extends GenericControler {

    private static final long serialVersionUID = 5418078144608698561L;

    @Autowired
    private transient ILogSistemaRepository repository;

    @GetMapping
    Page<LogSistemaResponse> list(
            @RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Param(value = "fechaIniStr") String fechaIniStr,
            @Param(value = "fechaFinStr") String fechaFinStr,
            @Param(value = "id") String id,
            @Param(value = "fechaRegistro") String fechaRegistro,
            @Param(value = "app") String app,
            @Param(value = "proceso") String proceso,
            @Param(value = "detalle") String detalle,
            @Param(value = "nivel") String nivel,
            @Param(value = "trazabilidad") String trazabilidad,
            Pageable pageRequest) throws NoHandlerFoundException, EncriptacionExcepcion {

        try {
            ipClient = obtenerIp(ipClient);
            String fi = ConfigEncriptacion.decrypt(fechaIniStr);
            String ff = ConfigEncriptacion.decrypt(fechaFinStr);

            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("fechaIniStr ", fi),
                    new AbstractMap.SimpleEntry<>("fechaFinStr ", ff),
                    new AbstractMap.SimpleEntry<>("id ", String.valueOf(id)),
                    new AbstractMap.SimpleEntry<>("fechaRegistro ", String.valueOf(fechaRegistro)),
                    new AbstractMap.SimpleEntry<>("app ", String.valueOf(app)),
                    new AbstractMap.SimpleEntry<>("proceso ", String.valueOf(proceso)),
                    new AbstractMap.SimpleEntry<>("detalle ", String.valueOf(detalle)),
                    new AbstractMap.SimpleEntry<>("nivel ", String.valueOf(nivel)),
                    new AbstractMap.SimpleEntry<>("trazabilidad_log ", String.valueOf(trazabilidad)),
                    new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            Page<LogSistemaResponse> out = repository.
                    filtrar(queryfilterTexto(fi), fi, queryfilterTexto(ff), ff,
                            queryfilterTexto(id), filterTextoQueryUpper(id),
                            queryfilterTexto(fechaRegistro), filterTextoQueryUpperLike(fechaRegistro),
                            queryfilterTexto(app), filterTextoQueryUpperLike(app),
                            queryfilterTexto(proceso), filterTextoQueryUpperLike(proceso),
                            queryfilterTexto(detalle), filterTextoQueryUpperLike(detalle),
                            queryfilterTexto(nivel), filterTextoQueryUpperLike(nivel),
                            queryfilterTexto(trazabilidad), filterTextoQueryUpperLike(trazabilidad),
                            pageRequest).
                    map(b -> {
                        LogSistemaResponse logSistemaResponse = ConvercionUtil.convertToObject(b, LogSistemaResponse.class);
                        String aux = isBlanck(logSistemaResponse.getFechaRegistro()) ? "" : BaseDate.convertLongToStringDefault(Long.valueOf(logSistemaResponse.getFechaRegistro()), BaseDate.FORMAT_DMY_HMS_SLASH);
                        logSistemaResponse.setFechaRegistro(aux);
                        return logSistemaResponse;
                    });

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR);

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out),
                    new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            
            logWeb.info(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, "*** info Se ha listado todo");
            logWeb.debug(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, "*** debug Se ha listado todo");
            logWeb.warn(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, "*** warn Se ha listado todo");
            
            return out;
        } catch (EncriptacionExcepcion ex) {
            final String mensajeError = "Error al filtrar log sistema, " + ex.getMessage();

            //final Long logSistemaId = logSistemaService.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, ex);
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, ex);
            HashMap<String, String> map = new HashMap();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw ex;
        }
    }

}
