package bo.com.micrium.modulobase.controllers;

import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.exception.PageException;
import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.common.exceptions.ApiException;
import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TiposComunes;
import bo.com.micrium.modulobase.controllers.dto.ClienteResponse;
import bo.com.micrium.modulobase.controllers.dto.ServicioRequest;
import bo.com.micrium.modulobase.controllers.dto.ServicioResponse;
import bo.com.micrium.modulobase.controllers.template.GenericControler;
import bo.com.micrium.modulobase.controllers.template.ICrudControler;
import bo.com.micrium.modulobase.validators.ServicioValidator;
import com.micrium.bd.access.jpa.models.Cliente;
import com.micrium.bd.access.jpa.models.Servicio;
import com.micrium.bd.access.jpa.models.Staff;
import com.micrium.bd.access.jpa.repositories.IServicioRepository;
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
@RequestMapping(value = "/servicio", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ServicioController
        extends GenericControler
        implements ICrudControler<ServicioRequest, ServicioResponse, String> {

    @Autowired
    private ServicioValidator validator;

    @Autowired
    private IServicioRepository repository;

    @Autowired
    private IStaffRepository repositoryStaff;

    @Override
    public Page<ServicioResponse> list(String token,
                                       String ipClient,
                                       String form,
                                       Pageable pageRequest) throws ApiException {
        try {
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);

            final String nombre = filterTextoQueryUpper(parametros.get("nombre"));
            final String descripcion = filterTextoQueryUpper(parametros.get("descripcion"));
            final String tipoServicio = filterTextoQueryUpper(parametros.get("tipo_servicio"));
            final String horario = filterTextoQueryUpper(parametros.get("horario"));
            final String cupos = filterTextoQueryUpper(parametros.get("cupos"));
            final String staffId = filterTextoQueryUpper(parametros.get("staff_id"));

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
                            new AbstractMap.SimpleEntry<>("descripcion ", filterTexto(descripcion)),
                            new AbstractMap.SimpleEntry<>("tipo_servicio ", filterTexto(tipoServicio)),
                            new AbstractMap.SimpleEntry<>("horario ", filterTexto(horario)),
                            new AbstractMap.SimpleEntry<>("cupos ", filterTexto(cupos)),
                            new AbstractMap.SimpleEntry<>("staffId ", filterTexto(staffId)),
                            new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<ServicioResponse> out = repository.filter(queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre), pageRequest)
                    .map(model -> {
                        ServicioResponse servicioResponse = ConvercionUtil.convertToObject(model, ServicioResponse.class);
                        servicioResponse.setStaffId(
                                Objects.nonNull(model.getStaffId()) ? model.getStaffId().getId() : null
                        );
                        return servicioResponse;

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

        } catch (ValidateException | PageException e) {
            final String mensajeError = "Error al filtrar servicio, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw new ApiException( mensajeError );
        }
    }

    @Override
    public ResponseEntity<ServicioResponse> get(String token,
                                                String ipClient,
                                                String form,
                                                String id) throws ApiException {
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<ServicioResponse> create(String token,
                                                   String ipClient,
                                                   String form,
                                                   ServicioRequest request,
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

            Staff staff = null;
            if (Objects.nonNull(request.getStaffId())) {
                staff = repositoryStaff.findById(request.getStaffId()).get();
            }

            Servicio newServicio = new Servicio();
            newServicio.setNombre(request.getNombre());
            newServicio.setDescripcion(request.getDescripcion());
            newServicio.setTipoServicio(request.getTipoServicio());
            newServicio.setHorario(request.getHorario());
            newServicio.setCupos(request.getCupos());
            newServicio.setEstado(request.getEstado());
            newServicio.setStaffId(staff);
            newServicio.setDateCreated(new Date(System.currentTimeMillis()));
            newServicio = repository.save(newServicio);

            map.put("servicio", ConvercionUtil.toJson(newServicio));

            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_SERVICIO", null, map);

            ServicioResponse response = ConvercionUtil.convertToObject(newServicio, ServicioResponse.class);
            response.setStaffId(request.getStaffId());
            ResponseEntity<ServicioResponse> out = ResponseEntity.created(
                            new URI("/servicio/" + newServicio.getId()))
                    .body(response);

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un servicio, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CREAR_SERVICIO", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_SERVICIO", null, map, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<ServicioResponse> update(String token,
                                                   String ipClient,
                                                   String form,
                                                   ServicioRequest request,
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

            Staff staff = null;
            if(Objects.nonNull(request.getStaffId())) {
                staff = repositoryStaff.findById(request.getStaffId()).get();
            }

            Servicio updateServicio = repository.findById(idDesencriptado).get();
            map.put("servicio", ConvercionUtil.toJson(updateServicio));

            updateServicio.setNombre(request.getNombre());
            updateServicio.setDescripcion(request.getDescripcion());
            updateServicio.setTipoServicio(request.getTipoServicio());
            updateServicio.setHorario(request.getHorario());
            updateServicio.setCupos(request.getCupos());
            updateServicio.setEstado(request.getEstado());
            updateServicio.setStaffId(staff);
            updateServicio.setDateUpdated(new Date(System.currentTimeMillis()));
            updateServicio = repository.save(updateServicio);

            mapNuevo.put("servicio", ConvercionUtil.toJson(updateServicio));

            bitacoraService.guardarBitacora(token, ipClient, form, "MODIFICAR_SERVICIO", map, mapNuevo);
            ServicioResponse response = ConvercionUtil.convertToObject(updateServicio, ServicioResponse.class);
            response.setStaffId(request.getStaffId());
            ResponseEntity<ServicioResponse> out = ResponseEntity.ok().
                    body(response);

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
            final String mensajeError = "Error al modificar un servicio, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "MODIFICAR_SERVICIO", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "MODIFICAR_SERVICIO", map, mapNuevo, logSistemaId);

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

            Optional<Servicio> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                throw new ApiException("DELETE /servicio/{id}" + id + ", No Exist");
            }

            Servicio model = temp.get();
            model.setEstado((short) 0);
            model.setDateUpdated(new Date(System.currentTimeMillis()));

            map.put("servicio", ConvercionUtil.toJson(model));
            repository.save(model);
            bitacoraService.guardarBitacora(token, ipClient, form, "ELIMINAR_SERVICIO", map, mapNuevo);

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
            final String mensajeError = "Error al eliminar un servicio, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "ELIMINAR_SERVICIO",
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "ELIMINAR_SERVICIO", map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }
}
