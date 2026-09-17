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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;
import com.micrium.bd.access.jpa.models.Accion;
import com.micrium.bd.access.jpa.models.Formulario;
import com.dtorrez.gym.controllers.dto.FormularioRequest2;
import com.dtorrez.gym.controllers.dto.FormularioResponse2;
import com.micrium.bd.access.jpa.repositories.IFormularioRepository;
import com.micrium.bd.access.jpa.repositories.IAccionRepository;
import com.dtorrez.gym.validators.FormularioValidator;

import bo.com.micrium.logger.LoggerMain;

@RestController
@CrossOrigin
@RequestMapping(value = "/formularios", produces = {MediaType.APPLICATION_JSON_VALUE})
public class FormularioControler extends GenericControler implements ICrudControler<FormularioRequest2, FormularioResponse2, String> {

    private static final long serialVersionUID = -69171311878619585L;

    @Autowired
    private transient IFormularioRepository repository;

    @Autowired
    private transient IAccionRepository accionRepository;

    @Autowired
    private transient FormularioValidator validator;

    @Override
    public Page<FormularioResponse2> list(String token, String ipClient, String form, Pageable pageRequest) throws Exception {

        try {
            validator.page(this.httpServletRequest.getParameter("size"), this.httpServletRequest.getParameter("page"), this.httpServletRequest.getParameter("sort"));

            //final String nombre = this.httpServletRequest.getParameter("nombre");
            //final Stringe url = this.httpServletRequest.getParameter("url");
            //final String tipo = this.httpServletRequest.getParameter("tipo");
            final String nombre = filterTextoQueryUpper(this.httpServletRequest.getParameter("nombre"));
            final String orden = filterTextoQueryUpper(this.httpServletRequest.getParameter("orden"));
            final String moduloId = filterTextoQueryUpper(this.httpServletRequest.getParameter("moduloId"));
            final String url = filterTextoQueryUpper(this.httpServletRequest.getParameter("url"));
            final String icono = filterTextoQueryUpper(this.httpServletRequest.getParameter("icono"));

            if (!isBlanck(nombre) && (nombre.length() > 200)) {
                throw new Exception("La longitud del nombre no debe ser mayor a 100.");
            }

            if (!isBlanck(url) && (url.length() > 255)) {
                throw new Exception("La longitud de la url no debe ser mayor a 255.");
            }

            if (!isBlanck(icono) && (icono.length() > 150)) {
                throw new Exception("La longitud del tipo no debe ser mayor a 50.");
            }

            ipClient = obtenerIp(ipClient);
        
            LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("nombre ", nombre),
                new AbstractMap.SimpleEntry<>("orden ", orden),
                new AbstractMap.SimpleEntry<>("moduloId ", moduloId),
                new AbstractMap.SimpleEntry<>("url ", url),
                new AbstractMap.SimpleEntry<>("icono ", icono),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("pageRequest ", pageRequest)).
                //collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing))
            );

            /*Page<FormularioResponse2> out = repository.filter(
                ((nombre == null || nombre.isEmpty()) ? -1 : 0), 
                ((nombre == null || nombre.trim().isEmpty()) ? "" : "%" + nombre.trim().toUpperCase() + "%"),
                ((url == null || url.isEmpty()) ? -1 : 0), 
                ((url == null || url.trim().isEmpty()) ? "" : "%" + url.trim().toUpperCase() + "%"),
                ((tipo == null || tipo.isEmpty()) ? -1 : 0), 
                ((tipo == null || tipo.trim().isEmpty()) ? "" : "%" + tipo.trim().toUpperCase() + "%"),
                pageRequest).map(model -> {
                    FormularioResponse2 formularioResponse = ConvercionUtil.convertToObject(model, FormularioResponse2.class);
                    formularioResponse.setModuloId(model.getModuloId().getId());
                    return formularioResponse;
                });*/
            Page<FormularioResponse2> out = repository.filter(
                queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre),
                queryfilterTexto(orden), filterTextoQueryUpperLike(orden),
                queryfilterTexto(moduloId), filterTextoQueryUpperLike(moduloId),
                queryfilterTexto(url), filterTextoQueryUpperLike(url),
                queryfilterTexto(icono), filterTextoQueryUpperLike(icono),
                //Rol.SUPER_ADMINISTRADOR, 
                pageRequest)
                .map(model -> {
                    FormularioResponse2 convertToObject = ConvercionUtil.convertToObject(
                        model, FormularioResponse2.class);
                    return convertToObject;
                });
                            
            LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("response ", out),
                new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            return out;            

        } catch (Exception e) {
            final String mensajeError = "Error al filtrar formulario, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<FormularioResponse2> get(String token, String ipClient, String form, String id) throws ApiException {        
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<FormularioResponse2> create(String token, String ipClient, String form, FormularioRequest2 request, BindingResult result) throws URISyntaxException, ApiException {
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

           // Formulario model = repository.save(new Formulario(null, request.getNombre(), request.getOrden(),
            //        request.getTipo(), request.getUrl(), request.getIcono(), moduloRepository.findById(request.getModuloId()).get()));
            Formulario model = repository.save(new Formulario(null, request.getNombre(), request.getOrden(),
            request.getModuloId(), request.getUrl(), request.getIcono()));

            map.put(TiposComunes.ModuloBase.FORMULARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_CREAR, null, map);

            ResponseEntity<FormularioResponse2> out = ResponseEntity.created(new URI("/formularios/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, FormularioResponse2.class));
  
            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un formulario. " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FORMULARIO_CREAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_CREAR, null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<FormularioResponse2> update(String token, String ipClient, String form, FormularioRequest2 request, String id, BindingResult result) throws ApiException {
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

            Optional<Formulario> formularioOptional = repository.findById(Long.parseLong(id));
            if (!formularioOptional.isPresent()) {
                throw new ApiException("El formulario con id " + id + " no existe.");
            }

            Formulario formulario = formularioOptional.get();
            formulario.setNombre(request.getNombre());
            formulario.setOrden(request.getOrden());
            formulario.setModuloId(request.getModuloId());
            formulario.setUrl(request.getUrl());
            formulario.setIcono(request.getIcono());
            //formulario.setModulo(moduloRepository.findById(request.getModuloId()).get());

            Formulario model = repository.save(formulario);

            map.put(TiposComunes.ModuloBase.FORMULARIO, ConvercionUtil.toJson(model));
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_MODIFICAR, null, map);

            ResponseEntity<FormularioResponse2> out = ResponseEntity.ok().body(ConvercionUtil.convertToObject(model, FormularioResponse2.class));

            LoggerMain.printResponse(Stream.of(
                    new AbstractMap.SimpleEntry<>("token ", token),
                    new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException e) {
            final String mensajeError = "Error al modificar el formulario. " + e.getMessage();

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
    
            Optional<Formulario> formularioOptional = repository.findById(Long.parseLong(id));
            if (!formularioOptional.isPresent()) {
                throw new ApiException("El formulario con id " + id + " no existe.");
            }
    
            // Verificar si hay acciones relacionadas con este formulario
            List<Accion> acciones = accionRepository.findByFormularioId(Long.parseLong(id));
            if (!acciones.isEmpty()) {
                // Crear el mensaje de error
                String mensaje = "No se puede eliminar el formulario porque tiene acciones asociadas.";
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
            final String mensajeError = "Error al eliminar el formulario. " + e.getMessage();
    
            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FORMULARIO_ELIMINAR, mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FORMULARIO_ELIMINAR, null, map, logSistemaId);
    
            throw e;
        }
    }
    
}
