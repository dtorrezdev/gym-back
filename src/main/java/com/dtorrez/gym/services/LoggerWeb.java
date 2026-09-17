package com.dtorrez.gym.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.micrium.bd.access.jpa.repositories.ILogSistemaRepository;
import com.micrium.bd.access.jpa.models.LogSistema;
import bo.com.micrium.modulobase.commons.LogNivel;
import bo.com.micrium.logger.AbstractLogger;
import bo.com.micrium.logger.ILoggerPersist;
import bo.com.micrium.logger.LoggerMain;

/**
 *
 * @author maryl
 */
@Service
@Component
public class LoggerWeb extends AbstractLogger implements ILoggerPersist {

    @Autowired
    private ILogSistemaRepository repository;

    public LoggerWeb() {
        // Private constructor to prevent instantiation
    }

    @Override
    public Long debug(String trazabilidad, String componente, String proceso, String detalle) {

        LoggerMain.debug(trazabilidad, proceso, detalle);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.DEBUG, trazabilidad));
        
        return idLog.getId();
    }

    @Override
    public Long debug(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        LoggerMain.debug(trazabilidad, proceso, detalle, ex);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.DEBUG, trazabilidad));
        
        return idLog.getId();
    }

    @Override
    public Long info(String trazabilidad, String componente, String proceso, String detalle) {
        LoggerMain.info(trazabilidad, proceso, detalle);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.INFO, trazabilidad));
        
        return idLog.getId();
    }

    @Override
    public Long info(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        LoggerMain.info(trazabilidad, proceso, detalle, ex);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.INFO, trazabilidad));
        
        return idLog.getId();
    }

    @Override
    public Long warn(String trazabilidad, String componente, String proceso, String detalle) {
        LoggerMain.warn(trazabilidad, proceso, detalle);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.WARN, trazabilidad));
        
        return idLog.getId();
    }

    @Override
    public Long warn(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        LoggerMain.warn(trazabilidad, proceso, detalle, ex);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.WARN, trazabilidad));
        
        return idLog.getId();
    }

    @Override
    public Long error(String trazabilidad, String componente, String proceso, String detalle) {
        LoggerMain.error(trazabilidad, proceso, detalle);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.ERROR, trazabilidad));
                
        return idLog.getId();
    }

    @Override
    public Long error(String trazabilidad, String componente, String proceso, String detalle, Exception ex) {
        LoggerMain.error(trazabilidad, proceso, detalle, ex);
        LogSistema idLog = repository.save(new LogSistema(null, new Date(), componente, proceso, detalle, LogNivel.ERROR, trazabilidad));
        
        return idLog.getId();
    }
}
