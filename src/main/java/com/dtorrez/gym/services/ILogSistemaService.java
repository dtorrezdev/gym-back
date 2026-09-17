package com.dtorrez.gym.services;

/**
 *
 * @author alepaco.com
 */
public interface ILogSistemaService {

    Long debug(String trazabilidad,String componente, String proceso, String detalle, boolean persistente);

    Long debug(String trazabilidad,String componente, String proceso, String detalle, Exception ex, boolean persistente);

    Long info(String trazabilidad, String componente, String proceso, String detalle, boolean persistente);

    Long info(String trazabilidad, String componente, String proceso, String detalle, Exception ex, boolean persistente);

    Long warn(String trazabilidad, String componente, String proceso, String detalle, boolean persistente);

    Long warn(String trazabilidad,String componente, String proceso, String detalle, Exception ex, boolean persistente);

    Long error(String trazabilidad, String componente, String proceso, String detalle, boolean persistente);

    Long error(String trazabilidad,String componente, String proceso, String detalle, Exception ex, boolean persistente);

    Long debug(String trazabilidad,String componente, String proceso, String detalle);

    Long debug(String trazabilidad, String componente, String proceso, String detalle, Exception ex);

    Long info(String trazabilidad, String componente, String proceso, String detalle);

    Long info(String trazabilidad, String componente, String proceso, String detalle, Exception ex);

    Long warn(String trazabilidad, String componente, String proceso, String detalle);

    Long warn(String trazabilidad, String componente, String proceso, String detalle, Exception ex);

    Long error(String trazabilidad, String componente, String proceso, String detalle);

    Long error(String trazabilidad, String componente, String proceso, String detalle, Exception ex);
}
