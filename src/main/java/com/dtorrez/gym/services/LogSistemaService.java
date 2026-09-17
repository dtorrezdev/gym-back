package com.dtorrez.gym.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import bo.com.micrium.modulobase.commons.LogNivel;
import com.micrium.bd.access.jpa.models.LogSistema;
import com.micrium.bd.access.jpa.repositories.ILogSistemaRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author alepaco.com
 */
@Service
@Component
public class LogSistemaService implements ILogSistemaService{

    private static final Logger log = LogManager.getLogger(LogSistemaService.class);
    
    @Autowired
    ILogSistemaRepository repository;
    
    /**
     * 
     * @param trazabilidad = usuario logueado
     * @param componente= DemOrques, DemPersonal
     * @param proceso = accion realizada
     * @param detalle
     * @return 
     */
    @Override
    public Long debug(String trazabilidad, String componente, String proceso, String detalle) {
        return debug(trazabilidad,componente, proceso, detalle, true);
    }

    @Override
    public Long debug(String trazabilidad,String componente, String proceso, String detalle, Exception ex) {
        return debug(trazabilidad,componente, proceso, detalle, ex, true);
    }

    @Override
    public Long info(String trazabilidad, String componente, String proceso, String detalle) {
        return info(trazabilidad, componente, proceso, detalle, true);
    }

    @Override
    public Long info(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        return info(trazabilidad, componente, proceso, detalle, ex, true);
    }

    @Override
    public Long warn(String trazabilidad, String componente, String proceso, String detalle) {
        return warn(trazabilidad, componente, proceso, detalle, true);
    }

    @Override
    public Long warn(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        return warn(trazabilidad, componente, proceso, detalle, ex, true);
    }

    @Override
    public Long error(String trazabilidad, String componente, String proceso, String detalle) {
        return error(trazabilidad,componente, proceso, detalle, true);
    }

    @Override
    public Long error(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        return error(trazabilidad, componente, proceso, detalle, ex, true);
    }

    @Override
    public Long debug(String trazabilidad,String componente, String proceso, String detalle, boolean persistente) {
        return debug(trazabilidad,componente, proceso, detalle, null, persistente);
    }

    /**
     * 
     * @param trazabilidad: usuario
     * @param componente: orquestador, DemPersonal, DemCargo,etc
     * @param proceso: accion realizada
     * @param detalle: 
     * @param ex: mensaje excepcion
     * @param persistente
     * @return 
     */
    @Override
    public Long debug(String trazabilidad,String componente, String proceso, String detalle, Exception ex, boolean persistente) {
        if (ex == null) {
            log.info(trazabilidad + " - " + proceso + " - " + detalle);
        } else {
            log.info(trazabilidad + " - " + proceso + " - " + detalle, ex);
        }

        if (persistente) {
            return repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.DEBUG, trazabilidad)).getId();
        } else {
            return null;
        }
    }

    @Override
    public Long info(String trazabilidad, String componente, String proceso, String detalle, boolean persistente) {
        return info(trazabilidad, componente, proceso, detalle, null, persistente);
    }

    @Override
    public Long info(String trazabilidad, String componente, String proceso, String detalle, Exception ex, boolean persistente) {
        if (ex == null) {
            log.info(trazabilidad + " - " + proceso + " - " + detalle);
        } else {
            log.info(trazabilidad + " - " + proceso + " - " + detalle, ex);
        }

        if (persistente) {
            return repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.INFO, trazabilidad)).getId();
        } else {
            return null;
        }
    }

    @Override
    public Long warn(String trazabilidad, String componente, String proceso, String detalle, boolean persistente) {
        return warn(trazabilidad,componente, proceso, detalle, null, persistente);
    }

    @Override
    public Long warn(String trazabilidad, String componente, String proceso, String detalle, Exception ex, boolean persistente) {
        if (ex == null) {
            log.warn(trazabilidad + " - " + proceso + " - " + detalle);
        } else {
            log.warn(trazabilidad + " - " + proceso + " - " + detalle, ex);
        }

        if (persistente) {
            return repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.WARN, trazabilidad)).getId();
        } else {
            return null;
        }
    }

    @Override
    public Long error(String trazabilidad,String componente, String proceso, String detalle, boolean persistente) {
        return error(trazabilidad, componente, proceso, detalle, null, persistente);
    }

    @Override
    public Long error(String trazabilidad,String componente, String proceso, String detalle, Exception ex, boolean persistente) {
        if (ex == null) {
            log.error(trazabilidad + " - " + proceso + " - " + detalle);
        } else {
            log.error(trazabilidad + " - " + proceso + " - " + detalle, ex);
        }

        if (persistente) {
            return repository.save(new LogSistema(null, new Date(),componente, proceso, detalle, LogNivel.WARN, trazabilidad)).getId();
        } else {
            return null;
        }
    }	
}
