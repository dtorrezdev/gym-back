package com.dtorrez.gym.controllers;

import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.exception.PageException;
import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.logger.LoggerMain;
import com.dtorrez.gym.common.exceptions.ApiException;
import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import com.dtorrez.gym.controllers.dto.StaffRequest;
import com.dtorrez.gym.controllers.dto.StaffResponse;
import com.dtorrez.gym.controllers.template.GenericControler;
import com.dtorrez.gym.controllers.template.ICrudControler;
import com.dtorrez.gym.validators.StaffValidator;
import com.micrium.bd.access.jpa.models.Staff;
import com.micrium.bd.access.jpa.repositories.IStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/staff", produces = {MediaType.APPLICATION_JSON_VALUE})
public class StaffController extends GenericControler
    implements ICrudControler<StaffRequest, StaffResponse, String> {

    @Autowired
    private transient IStaffRepository repository;

    @Autowired
    private transient StaffValidator validator;

    @Override
    public Page<StaffResponse> list(String token,
                                    String ipClient,
                                    String form,
                                    Pageable pageRequest) throws ApiException {
        try {
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);
            final String nombre = filterTextoQueryUpper(parametros.get("nombre"));
            final String especialidad = filterTextoQueryUpper(parametros.get("especialidad"));
            final String estado = filterTextoQueryUpper(parametros.get("estado"));


            parametros.forEach((key,value) -> {
                LoggerMain.info("key={},  value={}", key,value);
            });
            ipClient = obtenerIp(ipClient);
            LoggerMain.printRequest(Stream.of(
                            new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                            new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("form ", form),
                            new AbstractMap.SimpleEntry<>("nombre ", filterTexto(nombre)),
                            new AbstractMap.SimpleEntry<>("especialidad ", filterTexto(especialidad)),
                            new AbstractMap.SimpleEntry<>("estado ", filterTexto(estado)),
                            new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<StaffResponse> out = repository.filter(queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre), pageRequest)
                    .map(model -> {
                        StaffResponse staffResponse = ConvercionUtil.convertToObject(model, StaffResponse.class);
                        staffResponse.setEstado(model.getEstado() == 1 ? "Activo" : "Inactivo");
                        return staffResponse;
                    });

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out),
                            new AbstractMap.SimpleEntry<>("content ", out.getContent())).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            return out;
        }catch ( ValidateException | PageException e) {
            final String mensajeError = "Error al filtrar Staff, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw new ApiException(mensajeError, e);
        }
    }

    @Override
    public ResponseEntity<StaffResponse> get(String token,
                                             String ipClient,
                                             String form,
                                             String id) throws ApiException {
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<StaffResponse> create(String token,
                                                String ipClient,
                                                String form,
                                                StaffRequest request,
                                                BindingResult result) throws URISyntaxException, ApiException {
        Map<String, String> map = new HashMap<String, String>();
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
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                throw new ApiException(result, "Errores en la validacion");
            }

            Staff newStaff = new Staff();
            newStaff.setNombre(request.getNombre());
            newStaff.setEspecialidad(request.getEspecialidad());
//            newStaff.setHorario(request.getHorario());
            newStaff.setEstado((short) 1);
            newStaff.setDateCreated(new Date(System.currentTimeMillis()));
            newStaff = repository.save(newStaff);
            map.put("staff", ConvercionUtil.toJson(newStaff));
            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_STAFF", null, map);

            ResponseEntity<StaffResponse> out = ResponseEntity.created(
                            new URI("/staff/" + newStaff.getId()))
                    .body(ConvercionUtil.convertToObject(newStaff, StaffResponse.class));

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un staff, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CREAR_STAFF", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_STAFF", null, map, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<StaffResponse> update(String token,
                                                String ipClient,
                                                String form,
                                                StaffRequest request,
                                                String id,
                                                BindingResult result) throws ApiException {
        HashMap<String, String> map = new HashMap<String, String>(),
                                mapNuevo = new HashMap<String, String>();
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
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);

            validator.validate(request, idDesencriptado, result);

            if (result.hasErrors()) {
                map.put(TiposComunes.ERROR, obtenerErrores(result));
                throw new ApiException(result, "Errores en la validacion");
            }

            Staff updateStaff = repository.findById(idDesencriptado).get();
            map.put("staff", ConvercionUtil.toJson(updateStaff));

            updateStaff.setNombre(request.getNombre());
            updateStaff.setEspecialidad(request.getEspecialidad());
//            updateStaff.setHorario(request.getHorario());
            //updateStaff.setEstado(request.getEstado());
            updateStaff.setEstado(request.getEstado() == "Activo" ?  (short)1 : (short)0);
            updateStaff.setDateUpdated(new Date(System.currentTimeMillis()));
            repository.save(updateStaff);

            mapNuevo.put("staff", ConvercionUtil.toJson(updateStaff));

            bitacoraService.guardarBitacora(token, ipClient, form, "STAFF_MODIFICAR", map, mapNuevo);

            ResponseEntity<StaffResponse> out = ResponseEntity.ok().
                    body(ConvercionUtil.convertToObject(updateStaff, StaffResponse.class));

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            return out;
        } catch (ApiException | EncriptacionExcepcion e) {
        //} catch (ApiException e) {
            final String mensajeError = "Error al modificar un staff, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "STAFF_MODIFICAR", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "STAFF_MODIFICAR", map, mapNuevo, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token,
                                    String ipClient,
                                    String form,
                                    String id) throws ApiException {
        HashMap<String, String> map = new HashMap<>(),
                                mapNuevo = new HashMap<>();

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

            id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);

            Optional<Staff> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                throw new ApiException("DELETE /staff/{id}" + id + ", No Exist");
            }

            Staff updateStaff = temp.get();
            map.put("staff", ConvercionUtil.toJson(updateStaff));
            updateStaff.setEstado((short)0);
            updateStaff.setDateUpdated(new Date(System.currentTimeMillis()));

            repository.save(updateStaff);
            mapNuevo.put("staff", ConvercionUtil.toJson(updateStaff));

            bitacoraService.guardarBitacora(token, ipClient, form, "STAFF_ELIMINAR", map, mapNuevo);

            ResponseEntity<Object> out = ResponseEntity.ok().build();

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | EncriptacionExcepcion e) {
        //} catch (ApiException e) {
            final String mensajeError = "Error al eliminar un staff, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "STAFF_ELIMINAR",
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "STAFF_ELIMINAR", map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }
}
