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
import java.util.HashMap;

import org.springframework.web.servlet.NoHandlerFoundException;
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
import com.micrium.bd.access.jpa.repositories.IBitacoraRespository;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.dto.BitacoraResponse;
import com.dtorrez.gym.security.utils.JwtTokenUtil;
import bo.com.micrium.modulobase.commons.BaseDate;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/bitacoras", produces = {MediaType.APPLICATION_JSON_VALUE})

public class BitacoraControler extends GenericControler {

    private static final long serialVersionUID = 5418078144608698561L;

    @Autowired
    private transient IBitacoraRespository respository;

    @GetMapping
    Page<BitacoraResponse> list(
            @RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Param(value = "fechaIniStr") String fechaIniStr,
            @Param(value = "fechaFinStr") String fechaFinStr,
            @Param(value = "fecha") String fecha,
            @Param(value = "accion") String accion,
            @Param(value = "direccionIp") String direccionIp,
            @Param(value = "formulario") String formulario,
            @Param(value = "usuario") String usuario,
            Pageable pageRequest) throws NoHandlerFoundException, EncriptacionExcepcion {
        try {
            ipClient = obtenerIp(ipClient);
            String fi = ConfigEncriptacion.decrypt(fechaIniStr);
            String ff = ConfigEncriptacion.decrypt(fechaFinStr);
            LoggerMain.info("***dtn accion " + accion);
            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("fechaIniStr ", fi),
                    new AbstractMap.SimpleEntry<>("fechaFinStr ", ff),
                    new AbstractMap.SimpleEntry<>("fecha ", fecha),
                    new AbstractMap.SimpleEntry<>("accion ", accion),
                    new AbstractMap.SimpleEntry<>("direccionIp ", direccionIp),
                    new AbstractMap.SimpleEntry<>("formulario ", formulario),
                    new AbstractMap.SimpleEntry<>("usuario ", usuario),
                    new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            LoggerMain.info("***dtn accion 1 " + accion);
            Page<BitacoraResponse> out = respository.filter(queryfilterTexto(fi), fi, queryfilterTexto(ff), ff,
                    queryfilterTexto(fecha), filterTextoQueryUpperLike(fecha),
                    queryfilterTexto(accion), filterTextoQueryUpperLike(accion),
                    queryfilterTexto(direccionIp), filterTextoQueryUpperLike(direccionIp),
                    queryfilterTexto(formulario), filterTextoQueryUpperLike(formulario),
                    queryfilterTexto(usuario), filterTextoQueryUpperLike(usuario),
                    pageRequest).map(b -> {
                        BitacoraResponse bitacoraResponse = ConvercionUtil.convertToObject(b, BitacoraResponse.class);
                        String aux = isBlanck(bitacoraResponse.getFecha()) ? "" : BaseDate.convertLongToStringDefault(Long.valueOf(bitacoraResponse.getFecha()), BaseDate.FORMAT_DMY_HMS_SLASH);
                        bitacoraResponse.setFecha(aux);
                        return bitacoraResponse;
                    });

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR);

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out),
                    new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (EncriptacionExcepcion ex) {
            final String mensajeError = "Error al filtrar bitacora, " + ex.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, ex);
            HashMap<String, String> map = new HashMap<>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw ex;
        }
    }

}
