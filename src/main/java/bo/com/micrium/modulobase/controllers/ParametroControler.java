/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.com.micrium.modulobase.controllers;

import bo.com.micrium.modulobase.commons.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

//import bo.com.micrium.modulobase.commons.ConvercionUtil;

import com.micrium.bd.access.exceptions.DaoException;
import com.micrium.bd.access.jpa.models.Parametro;
import com.micrium.bd.access.jpa.models.RolTipoParametroPermiso;
import com.micrium.bd.access.jpa.models.TipoParametro;
import com.micrium.bd.access.jpa.repositories.IParametroRepository;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import com.micrium.bd.access.jpa.repositories.IRolTipoparametroRepository;
import com.micrium.bd.access.jpa.repositories.ITipoParametroRepository;
import bo.com.micrium.modulobase.controllers.dto.ParametroRequest;
import bo.com.micrium.modulobase.controllers.dto.ParametroResponse;
import bo.com.micrium.modulobase.services.ParametroService;
import bo.com.micrium.modulobase.common.exceptions.ApiException;
import bo.com.micrium.modulobase.controllers.template.GenericControler;
import bo.com.micrium.modulobase.controllers.template.ICrudControler;
import bo.com.micrium.modulobase.validators.ParametroValidator;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;


/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/parametros", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ParametroControler extends GenericControler implements ICrudControler<ParametroRequest, ParametroResponse, String> {

    private static final long serialVersionUID = 647183600973224741L;

    @Autowired
    private transient IParametroRepository repository;

    @Autowired
    private transient ITipoParametroRepository tipoParametroRepository;

    @Autowired
    private transient IRolRepository rolRepository;

    @Autowired
    private transient IRolTipoparametroRepository rolTipoparametroRepository;

    @Autowired
    private transient ParametroValidator validator;

    @Autowired
    private transient ParametroService parametroService;

    @Override
    public Page<ParametroResponse> list(String token, String ipClient, String form, Pageable pageRequest) throws Exception {
        try {
                        
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);

            final String nombre = parametros.get("nombre");
            final String valor = parametros.get("valor");
            final String descripcion = parametros.get("descripcion");
            final String idtipoparametro = parametros.get("idtipoparametro");

            ipClient = obtenerIp(ipClient);

            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            Page<ParametroResponse> out = null;
            if (idtipoparametro == null || idtipoparametro.isEmpty()) {
                List<RolTipoParametroPermiso> tiposparametrosIds = rolTipoparametroRepository.
                        findAllByRolId(rolRepository.findByNombreAndEstado(obtenerRol(), RolEstado.HABILITADO).getId()).
                        stream().filter(p -> p.getTipoPermiso() != PermisoTipo.PERMISO_NINGUNO).collect(Collectors.toList());

                /*out = repository.findAllByTipoParametroIdIn(tiposparametrosIds.stream().map(t -> t.getTipoParametroId()).collect(Collectors.toList()), pageRequest).
                        map(model -> ConvercionUtil.convertir(model,
                        tipoParametroRepository.findById(model.getTipoParametroId()).get(),
                        tiposparametrosIds.stream().filter(
                                o -> o.getTipoParametroId() == model.getTipoParametroId()).findFirst().get().getTipoPermiso() == PermisoTipo.PERMISO_LECTURA_ESCRITURA));*/
                out = repository.findAllByTipoParametroIdIn(tiposparametrosIds.stream().map(t -> t.getTipoParametroId()).collect(Collectors.toList()), pageRequest).
                        map(model -> {
                            TipoParametro tipoParametro = tipoParametroRepository.findById(model.getTipoParametroId()).get();
                            ParametroResponse parametroResponse = ConvercionUtil.convertToObject(model, ParametroResponse.class);
                            parametroResponse.setTipoParametroNombre(tipoParametro.getNombre());
                            parametroResponse.setEditable(tiposparametrosIds.stream().filter(
                                    o -> o.getTipoParametroId().equals(model.getTipoParametroId())).findFirst().get().getTipoPermiso() == PermisoTipo.PERMISO_LECTURA_ESCRITURA);
                            return parametroResponse;
                        });
            } else {
                Optional<TipoParametro> tiposparametro = tipoParametroRepository.findById(Long.valueOf(idtipoparametro.trim()));
                TipoParametro tipoParametroValue = tiposparametro.get();

                /*out = repository.filter(Long.valueOf(idtipoparametro.trim()), ((nombre == null || nombre.isEmpty()) ? -1 : 0), ((nombre == null || nombre.trim().isEmpty()) ? "" : "%" + nombre.trim().toUpperCase() + "%"),
                        ((valor == null || valor.isEmpty()) ? -1 : 0), ((valor == null || valor.trim().isEmpty()) ? "" : "%" + valor.trim().toUpperCase() + "%"),
                        ((descripcion == null || descripcion.isEmpty()) ? -1 : 0), ((descripcion == null || descripcion.trim().isEmpty()) ? "" : "%" + descripcion.trim().toUpperCase() + "%"),
                        pageRequest)
                        .map(model -> ConvercionUtil.convertir(model, tipoParametroValue, true));*/
                out = repository.filter(Long.valueOf(idtipoparametro.trim()), ((nombre == null || nombre.isEmpty()) ? -1 : 0), ((nombre == null || nombre.trim().isEmpty()) ? "" : "%" + nombre.trim().toUpperCase() + "%"),
                        ((valor == null || valor.isEmpty()) ? -1 : 0), ((valor == null || valor.trim().isEmpty()) ? "" : "%" + valor.trim().toUpperCase() + "%"),
                        ((descripcion == null || descripcion.isEmpty()) ? -1 : 0), ((descripcion == null || descripcion.trim().isEmpty()) ? "" : "%" + descripcion.trim().toUpperCase() + "%"),
                        pageRequest)
                        .map(model -> {
                            ParametroResponse parametroResponse = ConvercionUtil.convertToObject(model, ParametroResponse.class);
                            parametroResponse.setTipoParametroNombre(tipoParametroValue.getNombre());
                            parametroResponse.setEditable(false);
                            return parametroResponse;
                        });
            }
    
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, null);
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out),
                    new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;            

        } catch (Exception e) {
            final String mensajeError = "Error al obtener Parametro, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);
            throw e;
        }
    }

    /**
     * Para el caso del parametro, la busqueda sera por el nombre y no el ID, es
     * decir, que el validr ID sera el nombre del parametro que tambien debe ser
     * unico.
     *
     * @param token
     * @param ipClient
     * @param form
     * @param id
     * @return
     * @throws NoHandlerFoundException
     */
    @Override
    public ResponseEntity<ParametroResponse> get(String token, String ipClient, String form, String id) throws ApiException {
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

            String desencriptadoId = ConfigEncriptacion.decrypt(id);
            LoggerMain.info("desencriptadoId " + desencriptadoId);

            //Optional<Parametro> model = repository.findById(id);
            //Optional<Parametro> model = repository.findById(descrp);
            Parametro model = repository.findByNombre(desencriptadoId);
            if (model != null) {
                TipoParametro tipoParametro = tipoParametroRepository.findById(model.getTipoParametroId()).get();
                ParametroResponse parametroResponse = ConvercionUtil.convertToObject(model, ParametroResponse.class);
                LoggerMain.info("OAC - parametroResponse: " + parametroResponse);
                parametroResponse.setTipoParametroNombre(tipoParametro.getNombre());
                parametroResponse.setEditable(false);
                LoggerMain.info("OAC - DESPUES parametroResponse: " + parametroResponse);
                ResponseEntity<ParametroResponse> out = ResponseEntity.ok().body(parametroResponse);
                
                LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                return out;
            }
        } catch (EncriptacionExcepcion e) {
            final String mensajeError = "Error al obtener Parametro, " + e.getMessage();
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);
            throw new ApiException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResponseEntity<ParametroResponse> create(String token, String ipClient, String form,
            ParametroRequest request, BindingResult result) throws URISyntaxException, ApiException {

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
                //bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PARAMETRO_CREAR, null, map);
                throw new ApiException(result, "Errores en la validacion");
            }

            Parametro model = repository.save(new Parametro(null, request.getNombre(),
                    request.getTipo(), request.getValor(), request.getDescripcion(),
                    request.getTipoParametroId()));

            map.put(TiposComunes.ModuloBase.PARAMETRO, ConvercionUtil.toJson(model));
            //bitacoraService.guardarBitacora(token, ipClient, form, "Se adiciono:" + model);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PARAMETRO_CREAR, null, map);            

            /*ResponseEntity<ParametroResponse> out = ResponseEntity.created(new URI("/parametros/" + model.getId()))
                .body(ConvercionUtil.convertir(model, tipoParametroRepository.findById(model.getTipoParametroId()).get(), false));*/
            TipoParametro tipoParametro = tipoParametroRepository.findById(model.getTipoParametroId()).get();
            ParametroResponse parametroResponse = ConvercionUtil.convertToObject(model, ParametroResponse.class);
            parametroResponse.setTipoParametroNombre(tipoParametro.getNombre());
            parametroResponse.setEditable(false);
            ResponseEntity<ParametroResponse> out = ResponseEntity.ok().body(parametroResponse);

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
        } catch (ApiException e) {
            final String mensajeError = "Error al crear un parametro, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.PARAMETRO_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PARAMETRO_CREAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<ParametroResponse> update(String token, String ipClient, String form,
            ParametroRequest request, String id, BindingResult result) throws ApiException {
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
                //bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PARAMETRO_MODIFICAR, null, map);
                throw new ApiException(result, "Errores en la validacion");
            }

            Parametro model = repository.findById(idDesencriptado).get();
            //Long modelId = model.getId();
            if (model.getNombre().equals(com.micrium.bd.access.enuns.Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name())) {
                int valor = Integer.parseInt(request.getValor().trim());
                //if (request.getValor().trim().equals(ParametroID.LDAP) || request.getValor().trim().equals(ParametroID.HIBRIDO)) {
                if ((valor == TipoAutenticacion.LDAP.getId()) || (valor == TipoAutenticacion.HIBRIDO.getId())) {
                    //etiquetaRepository.updateByEstado("grupo",false);
                }
            }

            if (ParametroTipo.TIPO_PASSWORD == request.getTipo()) {
                LoggerMain.info("request: " + request.toString());
                if (request.getValor().equals("")) {
                    request.setValor(model.getValor());
                } else {
                    request.setValor(ConfigEncriptacion.encrypt(request.getValor()));
                }
                LoggerMain.info("request: " + request.toString());
            }
            map.put(TiposComunes.ModuloBase.PARAMETRO, model.toString());

            model.setNombre(request.getNombre());
            model.setTipo(request.getTipo());
            model.setValor(request.getValor());
            model.setDescripcion(request.getDescripcion());
            model.setTipoParametroId(request.getTipoParametroId());

            model = repository.save(model);

            mapNuevo.put(TiposComunes.ModuloBase.PARAMETRO, model.toString());

            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PARAMETRO_MODIFICAR, map, mapNuevo);
            
            parametroService.updateParameter(request.getNombre(), request.getValor());

            /*ResponseEntity<ParametroResponse> out = ResponseEntity.ok().body(ConvercionUtil.convertir(model,
            tipoParametroRepository.findById(model.getTipoParametroId()).get(), false));*/
            TipoParametro tipoParametro = tipoParametroRepository.findById(model.getTipoParametroId()).get();
            ParametroResponse parametroResponse = ConvercionUtil.convertToObject(model, ParametroResponse.class);
            parametroResponse.setTipoParametroNombre(tipoParametro.getNombre());
            parametroResponse.setEditable(false);
            ResponseEntity<ParametroResponse> out = ResponseEntity.ok().body(parametroResponse);
            
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (DaoException | EncriptacionExcepcion | ApiException e) {
            final String mensajeError = "Error al modificar un parametro, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.PARAMETRO_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PARAMETRO_MODIFICAR, map, mapNuevo, logSistemaId);

            throw new ApiException(result, mensajeError, e);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token, String ipClient, String form, String id) throws ApiException {
        throw new ApiException("DELETE not support method " + "/{id} " + id);
    }

}
