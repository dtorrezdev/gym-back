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
import bo.com.micrium.modulobase.controllers.dto.*;
import bo.com.micrium.modulobase.controllers.template.GenericControler;
import bo.com.micrium.modulobase.controllers.template.ICrudControler;
import bo.com.micrium.modulobase.security.utils.JwtTokenUtil;
import bo.com.micrium.modulobase.validators.PlanValidador;
import com.micrium.bd.access.jpa.models.Cliente;
import com.micrium.bd.access.jpa.models.Plan;
import com.micrium.bd.access.jpa.models.PlanServicio;
import com.micrium.bd.access.jpa.repositories.IPlanRepository;
import com.micrium.bd.access.jpa.repositories.IPlanServicioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/plan", produces = {MediaType.APPLICATION_JSON_VALUE})
public class PlanController extends GenericControler
    implements ICrudControler<PlanRequest, PlanResponse, String> {

    @Autowired
    private transient IPlanRepository repository;

    @Autowired transient IPlanServicioRepository planServicioRepository;

    @Autowired
    private transient PlanValidador validator;

    @Override
    public Page<PlanResponse> list(String token,
                                   String ipClient,
                                   String form,
                                   Pageable pageRequest) throws ApiException {
        try {
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);
            final String nombre = filterTextoQueryUpper(parametros.get("nombre"));
            final String descripcion = filterTextoQueryUpper(parametros.get("descripcion"));
            final String duracion = filterTextoQueryUpper(parametros.get("duracion"));
            final String precio = filterTextoQueryUpper(parametros.get("precio"));

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
                            new AbstractMap.SimpleEntry<>("precio ", filterTexto(precio)),
                            new AbstractMap.SimpleEntry<>("duracion ", filterTexto(duracion)),
                            new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<PlanResponse> out = repository.filter(queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre), pageRequest)
                    .map(model -> {
                        PlanResponse planResponse = ConvercionUtil.convertToObject(model, PlanResponse.class);
                        planResponse.setEstado(model.getEstado() == 1 ? "Activo" : "Inactivo");
                        planResponse.setServicios(
                                planServicioRepository.findAllByPlanId(model.getId())
                                        .stream()
                                        .map(PlanServicio::getServicioId)
                                        .toList()
                        );
                        return planResponse;
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
            final String mensajeError = "Error al filtrar Planes, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw new ApiException( mensajeError, e);
        }
    }

    @Override
    public ResponseEntity<PlanResponse> get(String token,
                                            String ipClient,
                                            String form,
                                            String id) throws ApiException {
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<PlanResponse> create(String token,
                                               String ipClient,
                                               String form,
                                               PlanRequest request,
                                               BindingResult result) throws ApiException {
        HashMap<String, String> map = new HashMap<String, String>();
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

            Plan newPlan = new Plan();
            newPlan.setNombre(request.getNombre());
            newPlan.setDescripcion(request.getDescripcion());
            newPlan.setPrecio(request.getPrecio());
            newPlan.setDuracion(request.getDuracion());
            newPlan.setEstado((short) 1);
            newPlan.setDateCreated(new Date(System.currentTimeMillis()));
            newPlan = repository.save(newPlan);
            map.put("plan", ConvercionUtil.toJson(newPlan));

            Long planId = newPlan.getId();
            this.planServicioRepository.deleteByPlanId(planId);

            List<PlanServicio> storedPlanServicios = new ArrayList<>();

            request.getServicios().forEach(servicioId -> {
                PlanServicio newPlanServicio = new PlanServicio(null, planId, servicioId);
                newPlanServicio = this.planServicioRepository.save(newPlanServicio);
                storedPlanServicios.add(newPlanServicio);
            });

            map.put("plan_servicio", ConvercionUtil.toJson(storedPlanServicios));

            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_PLAN", null, map);

            ResponseEntity<PlanResponse> out = ResponseEntity.created(
                            new URI("/plan/" + newPlan.getId()))
                    .body(ConvercionUtil.convertToObject(newPlan, PlanResponse.class));

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;

        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un plan, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CREAR_PLAN", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_PLAN", null, map, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<PlanResponse> update(String token,
                                               String ipClient,
                                               String form,
                                               PlanRequest request,
                                               String id,
                                               BindingResult result) throws ApiException {
        Map<String, String> map = new HashMap<String, String>(),
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

            Plan updatePlan = repository.findById(idDesencriptado).get();
            map.put("plan", ConvercionUtil.toJson(updatePlan));

            updatePlan.setNombre(request.getNombre());
            updatePlan.setDescripcion(request.getDescripcion());
            updatePlan.setPrecio(request.getPrecio());
            updatePlan.setDuracion(request.getDuracion());
            updatePlan.setEstado(request.getEstado() == "Activo"? (short)1: (short) 0);
            updatePlan.setDateUpdated(new Date(System.currentTimeMillis()));
            repository.save(updatePlan);

            mapNuevo.put("plan", ConvercionUtil.toJson(updatePlan));

            Long planId = updatePlan.getId();
            this.planServicioRepository.deleteByPlanId(planId);

            List<PlanServicio> storedPlanServicios = new ArrayList<>();

            request.getServicios().forEach(servicioId -> {
                PlanServicio newPlanServicio = new PlanServicio(null, planId, servicioId);
                newPlanServicio = this.planServicioRepository.save(newPlanServicio);
                storedPlanServicios.add(newPlanServicio);
            });
            map.put("plan_servicio", ConvercionUtil.toJson(storedPlanServicios));

            bitacoraService.guardarBitacora(token, ipClient, form, "PLAN_MODIFICAR", map, mapNuevo);

            ResponseEntity<PlanResponse> out = ResponseEntity.ok().
                    body(ConvercionUtil.convertToObject(updatePlan, PlanResponse.class));

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            return out;
        } catch (ApiException | EncriptacionExcepcion e) {
        //} catch(Exception e) {
            final String mensajeError = "Error al modificar un plan, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "PLAN_MODIFICAR", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "PLAN_MODIFICAR", map, mapNuevo, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token,
                                    String ipClient,
                                    String form,
                                    String id) throws ApiException {
        Map<String, String> map = new HashMap<>(),
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

            //id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = Long.parseLong(id);//ConfigEncriptacion.desencryptIdToConvertLong(id);

            Optional<Plan> temp = repository.findById(idDesencriptado);
            if (!temp.isPresent()) {
                throw new ApiException("DELETE /plan/{id}" + id + ", No Exist");
            }
            Plan updatePlan = temp.get();
            map.put("plan", ConvercionUtil.toJson(updatePlan));

            updatePlan.setEstado((short)0);  // [0: Inactivo, 1: Activo]
            updatePlan.setDateUpdated(new Date(System.currentTimeMillis()));

            mapNuevo.put("plan", ConvercionUtil.toJson(updatePlan));
            repository.save(updatePlan);
            bitacoraService.guardarBitacora(token, ipClient, form, "PLAN_ELIMINAR", map, mapNuevo);

            ResponseEntity<Object> out = ResponseEntity.ok().build();

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        //} catch (ApiException | EncriptacionExcepcion e) {
        } catch (RuntimeException e) {
            final String mensajeError = "Error al eliminar un plan, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "PLAN_ELIMINAR",
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "PLAN_ELIMINAR", map, mapNuevo, logSistemaId);
            throw new ApiException(mensajeError);
        }
    }

    // Registrar Plan Servicio
    @PostMapping("/plan_servicio")
    ResponseEntity<?> guardarPlanServicio(
            @RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody PlanServicioRequest request
    ) throws URISyntaxException, ApiException {

        HashMap<String, String> mapNuevo = new HashMap<>();

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
        Long planId = request.getPlanId();
        this.planServicioRepository.deleteByPlanId(planId);

        List<PlanServicio> storedPlanServicios = new ArrayList<>();

        request.getServicioIds().forEach(servicioId -> {
            PlanServicio newPlanServicio = new PlanServicio(null, planId, servicioId);
            newPlanServicio = this.planServicioRepository.save(newPlanServicio);
            storedPlanServicios.add(newPlanServicio);
        });

        mapNuevo.put("plan_servicio", ConvercionUtil.toJson(storedPlanServicios));
        bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_PLAN_SERVICIO" + " del rol " + request.getPlanId(), null, mapNuevo);

        ResponseEntity<Object> out = ResponseEntity.ok().build();

        LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return out;
    }
}
