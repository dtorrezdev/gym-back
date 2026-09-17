package com.dtorrez.gym.controllers;

import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;
import com.micrium.bd.access.jpa.models.Accion;
import com.micrium.bd.access.jpa.models.RolAccion;
import com.dtorrez.gym.controllers.dto.AccionRequest;
import com.dtorrez.gym.controllers.dto.AccionResponse2;
import com.micrium.bd.access.jpa.repositories.IAccionRepository;
import com.micrium.bd.access.jpa.repositories.IRolAccionRepository;
import com.dtorrez.gym.validators.AccionValidator;

import bo.com.micrium.logger.LoggerMain;

@RestController
@CrossOrigin
@RequestMapping(value = "/acciones", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AccionControler extends GenericControler implements ICrudControler<AccionRequest, AccionResponse2, String> {

    private static final long serialVersionUID = -69171311878619585L;

    @Autowired
    private transient IAccionRepository repository;

    @Autowired
    private transient IRolAccionRepository rolAccionRepository;

    @Autowired
    private transient AccionValidator validator;    


    @Override
    public Page<AccionResponse2> list(String token, String ipClient, String form, Pageable pageRequest) throws Exception {

        try {
            validator.page(this.httpServletRequest.getParameter("size"), this.httpServletRequest.getParameter("page"), this.httpServletRequest.getParameter("sort"));

            //final String formularioId = this.httpServletRequest.getParameter("formularioId");
            //final String url = this.httpServletRequest.getParameter("url");
            //final String tipo = this.httpServletRequest.getParameter("tipo");
            final String formularioId = filterTextoQueryUpper(this.httpServletRequest.getParameter("formularioId"));
            final String nombre = filterTextoQueryUpper(this.httpServletRequest.getParameter("nombre"));
            final String url = filterTextoQueryUpper(this.httpServletRequest.getParameter("url"));
            final String metodo = filterTextoQueryUpper(this.httpServletRequest.getParameter("metodo"));

            if (!isBlanck(formularioId) && (formularioId.length() > 200)) {
                throw new Exception("La longitud del formularioId no debe ser mayor a 100.");
            }

            if (!isBlanck(url) && (url.length() > 255)) {
                throw new Exception("La longitud de la url no debe ser mayor a 255.");
            }

            if (!isBlanck(metodo) && (metodo.length() > 150)) {
                throw new Exception("La longitud del tipo no debe ser mayor a 50.");
            }

            ipClient = obtenerIp(ipClient);
            
            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("formularioId ", formularioId),
                    new AbstractMap.SimpleEntry<>("nombre ", nombre),
                    new AbstractMap.SimpleEntry<>("url ", url),
                    new AbstractMap.SimpleEntry<>("metodo ", metodo),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("pageRequest ", pageRequest)).
                    //collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing)));


            /*Page<AccionResponse2> out = repository.filter(
                    ((nombre == null || nombre.isEmpty()) ? -1 : 0), 
                    ((nombre == null || nombre.trim().isEmpty()) ? "" : "%" + nombre.trim().toUpperCase() + "%"),
                    ((url == null || url.isEmpty()) ? -1 : 0), 
                    ((url == null || url.trim().isEmpty()) ? "" : "%" + url.trim().toUpperCase() + "%"),
                    ((tipo == null || tipo.isEmpty()) ? -1 : 0), 
                    ((tipo == null || tipo.trim().isEmpty()) ? "" : "%" + tipo.trim().toUpperCase() + "%"),
                    pageRequest).map(model -> {
                        AccionResponse2 accionResponse = ConvercionUtil.convertToObject(model, AccionResponse2.class);
                        accionResponse.setModuloId(model.getModuloId().getId());
                        return accionResponse;
                    });*/
                        Page<AccionResponse2> out = repository.filter(
                        queryfilterTexto(formularioId), filterTextoQueryUpperLike(formularioId),
                        queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre),
                        queryfilterTexto(url), filterTextoQueryUpperLike(url),
                        queryfilterTexto(metodo), filterTextoQueryUpperLike(metodo),
                        //Rol.SUPER_ADMINISTRADOR, 
                        pageRequest)
                        .map(model -> {
                            AccionResponse2 convertToObject = ConvercionUtil.convertToObject(model, AccionResponse2.class);
                            return convertToObject;
                        });
                //.map(model
            
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out),
                    new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
            

        } catch (Exception e) {
            final String mensajeError = "Error al filtrar accion, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<AccionResponse2> get(String token, String ipClient, String form, String id) throws ApiException {        
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<AccionResponse2> create(String token, String ipClient, String form, AccionRequest request, BindingResult result) throws URISyntaxException, ApiException {
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
                throw new ApiException(result, "Errores en la validación");
            }

           // Accion model = repository.save(new Accion(null, request.getNombre(), request.getOrden(),
            //        request.getTipo(), request.getUrl(), request.getIcono(), moduloRepository.findById(request.getModuloId()).get()));
            Accion model = repository.save(new Accion(null, request.getFormularioId(), request.getNombre(),
             request.getUrl(), request.getMetodo()));

            map.put(TiposComunes.ModuloBase.FORMULARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_CREAR, null, map);

            ResponseEntity<AccionResponse2> out = ResponseEntity.created(new URI("/acciones/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, AccionResponse2.class));
  
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear una Accion. " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FORMULARIO_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_CREAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<AccionResponse2> update(String token, String ipClient, String form, AccionRequest request, String id, BindingResult result) throws ApiException {
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
                throw new ApiException(result, "Errores en la validación");
            }

            Optional<Accion> accionOptional = repository.findById(Long.parseLong(id));
            if (!accionOptional.isPresent()) {
                throw new ApiException("La accion con id " + id + " no existe.");
            }

            Accion accion = accionOptional.get();
            accion.setFormularioId(request.getFormularioId());
            accion.setNombre(request.getNombre());
            accion.setUrl(request.getUrl());
            accion.setMetodo(request.getMetodo());

            Accion model = repository.save(accion);

            map.put(TiposComunes.ModuloBase.FORMULARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_MODIFICAR, null, map);

            ResponseEntity<AccionResponse2> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, AccionResponse2.class));

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException e) {
            final String mensajeError = "Error al modificar el accion. " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FORMULARIO_MODIFICAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_MODIFICAR, null, map, logSistemaId);

            throw e;
        }
    }
    @Override
    public ResponseEntity<Map<String, String>> delete(String token, String ipClient, String form, String id) throws ApiException {
        HashMap<String, String> map = new HashMap<>();
        try {
            ipClient = obtenerIp(ipClient);
    
            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    
            Optional<Accion> accionOptional = repository.findById(Long.parseLong(id));
            if (!accionOptional.isPresent()) {
                throw new ApiException("El accion con id " + id + " no existe.");
            }
                    
                            // Verificar si hay roles relacionados con este accion
                List<RolAccion> roles = rolAccionRepository.findByAccionId(Long.parseLong(id));
                if (!roles.isEmpty()) {
                    // Crear el mensaje de error
                    String mensaje = "No se puede eliminar el accion porque tiene roles asociados.";
                    map.put(TiposComunes.MENSAJE_ERROR, mensaje);

                    LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("response ", mensaje)).
                            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                    // Devolver una respuesta con el mensaje de error
                    return ResponseEntity
                            .badRequest()
                            .body(map); // Devolvemos el mapa con el mensaje de error
                }

    
            // Eliminar el formulario si no tiene acciones relacionadas
            repository.deleteById(Long.parseLong(id));
    
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_ELIMINAR, null, map);
    
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            final String mensajeError = "Error al eliminar la accion. " + e.getMessage();
    
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FORMULARIO_ELIMINAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_ELIMINAR, null, map, logSistemaId);
    
            throw e;
        }
    }
    

}
