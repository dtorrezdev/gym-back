package com.dtorrez.gym.services;

import bo.com.micrium.modulobase.commons.ConvercionUtil;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.micrium.bd.access.jpa.models.Bitacora;
import com.micrium.bd.access.jpa.repositories.IBitacoraRespository;
import com.dtorrez.gym.security.utils.JwtTokenUtil;

/**
 *
 * @author alepaco.com
 */
@Service
@Component
public class BitacoraService {

    @Autowired
    IBitacoraRespository respository;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    public void guardarBitacora(String token, String direccionIp, String form,
            String accion) {
        guardarBitacora(token, direccionIp, form, accion, null, null, null);
    }

    public void guardarBitacora(String token, String direccionIp, String form,
            String accion,
            Map valorAnterior,
            Map valorNuevo) {
        guardarBitacora(token, direccionIp,
                form, accion, valorAnterior, valorNuevo, null);
    }

    public void guardarBitacora(String token, String direccionIp, String form,
            String accion,
            Map valorAnterior,
            Map valorNuevo, Long logSistemaId) {
        guardarBitacoraSinToken(jwtTokenUtil.getUsernameFromToken(token),
                direccionIp, form, accion, valorAnterior, valorNuevo, logSistemaId);
    }

    public void guardarBitacoraSinToken(String userName, String direccionIp,
            String form, String accion) {
        respository.save(new Bitacora(null, accion, direccionIp,
                new Timestamp(Calendar.getInstance().getTimeInMillis()),
                form, userName, null, null, null));
    }

    public void guardarBitacoraSinToken(String userName, String direccionIp,
            String form, String accion,
            Long logSistemaId) {
        respository.save(new Bitacora(null, accion, direccionIp,
                new Timestamp(Calendar.getInstance().getTimeInMillis()),
                form, userName, null, null, logSistemaId));
    }

    public void guardarBitacoraSinToken(String userName, String direccionIp,
            String form, String accion, Object valorAnterior, Object valorNuevo) {
        //LoggerMain.debug("]*** dtn object " + valorAnterior + " valor Nuevo: " + valorNuevo);
        respository.save(new Bitacora(null, accion, direccionIp,
                new Timestamp(Calendar.getInstance().getTimeInMillis()),
                form, userName, ConvercionUtil.toJson(valorAnterior),
                ConvercionUtil.toJson(valorNuevo), null));
    }

    public void guardarBitacoraSinToken(String userName, String direccionIp,
            String form, String accion, Object valorAnterior, Object valorNuevo,
            Long logSistemaId) {
        //LoggerMain.debug("*** dtn object " + valorAnterior + " valor Nuevo: " + valorNuevo);
        respository.save(new Bitacora(null, accion, direccionIp,
                new Timestamp(Calendar.getInstance().getTimeInMillis()),
                form, userName, ConvercionUtil.toJson(valorAnterior),
                ConvercionUtil.toJson(valorNuevo), logSistemaId));
    }

}
