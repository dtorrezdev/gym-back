package bo.com.micrium.modulobase.controllers;

import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.ConversionException;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.exception.PageException;
import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.common.exceptions.ApiException;
import bo.com.micrium.modulobase.commons.*;
import bo.com.micrium.modulobase.controllers.dto.*;
import bo.com.micrium.modulobase.controllers.template.GenericControler;
import bo.com.micrium.modulobase.controllers.template.ICrudControler;
import bo.com.micrium.modulobase.security.utils.JwtTokenUtil;
import bo.com.micrium.modulobase.validators.MembresiaValidator;
import com.micrium.bd.access.jpa.models.*;
import com.micrium.bd.access.jpa.repositories.*;
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
@RequestMapping(value = "/membresia", produces = {MediaType.APPLICATION_JSON_VALUE})
public class MembresiaController
        extends GenericControler
        implements ICrudControler<MembresiaRequest, MembresiaResponse, String> {

    @Autowired
    private transient IMembresiaRepository repository;

    @Autowired
    private transient IMembresiaPlanRepository membresiaPlanRepository;

    @Autowired
    private transient IPlanRepository planRepository;

    @Autowired
    private transient IClienteRepository clienteRepository;

    @Autowired
    private transient IUsuarioRepository usuarioRepository;

    @Autowired
    private transient MembresiaValidator validator;


    @Override
    public Page<MembresiaResponse> list(
            String token,
            String ipClient,
            String form,
            Pageable pageRequest
    ) throws Exception {
        try {
            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);
            final String descripcion = filterTextoQueryUpper(parametros.get("descripcion"));
            final String tipoPago = filterTextoQueryUpper(parametros.get("tipoPago"));
            final String montoTotal = filterTextoQueryUpper(parametros.get("monto_total"));
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
                            new AbstractMap.SimpleEntry<>("descripcion ", filterTexto(descripcion)),
                            new AbstractMap.SimpleEntry<>("tipoPago ", filterTexto(tipoPago)),
                            new AbstractMap.SimpleEntry<>("monto_total ", filterTexto(montoTotal)),
                            new AbstractMap.SimpleEntry<>("estado ", filterTexto(estado)),
                            new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<MembresiaResponse> out = repository.filter(
                            queryfilterTexto(descripcion), filterTextoQueryUpperLike(descripcion),
                            pageRequest)
                    .map(model -> {
                        MembresiaResponse membresiaResponse = ConvercionUtil.convertToObject(model, MembresiaResponse.class);
                        membresiaResponse.setClienteId(model.getCliente().getId());
                        membresiaResponse.setCliente(model.getCliente().getNombre());
                        //membresiaResponse.setUsuarioId(model.getUsuario().getId());
                        membresiaResponse.setEstado(model.getEstado() == 1 ? "Activo" : "Inactivo");
                        membresiaResponse.setPlanes(
                                this.membresiaPlanRepository.findAllByMembresiaId(model.getId())
                                        .stream()
                                        .map( planMen -> {
                                            PlanMembresiaResponse respoMenPla = ConvercionUtil
                                                    .convertToObject(planMen, PlanMembresiaResponse.class);
                                            respoMenPla.setPlanId(planMen.getPlanId().getId());
                                            respoMenPla.setPlan(planMen.getPlanId().getNombre() + "-" + planMen.getPlanId().getDuracion());
                                            return  respoMenPla;
                                        }).toList()
                        );
                        return membresiaResponse;
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
            final String mensajeError = "Error al filtrar membresias, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw new ApiException( mensajeError, e);
        }
    }

    @Override
    public ResponseEntity<MembresiaResponse> get(
            String token,
            String ipClient,
            String form,
            String id
    ) throws ApiException {
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<MembresiaResponse> create(
            String token,
            String ipClient,
            String form,
            MembresiaRequest request,
            BindingResult result
    ) throws URISyntaxException, ApiException {

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

            Cliente cliente = null;
            if (Objects.nonNull(request.getClienteId())) {
                cliente = clienteRepository.findById(request.getClienteId()).get();
            }

            Usuario usuario = null;
            if (Objects.nonNull(request.getUsuarioId())) {
                usuario = usuarioRepository.findById(request.getUsuarioId()).get();
            }

            Membresia newMembresia = new Membresia();
            newMembresia.setDescripcion(request.getDescripcion());
            newMembresia.setTipoPago(request.getTipoPago());
            newMembresia.setMontoTotal(request.getMontoTotal());
            newMembresia.setEstado((short) 1);
            newMembresia.setCliente(cliente);
            newMembresia.setUsuario(usuario);
            newMembresia.setDateCreated(new Date(System.currentTimeMillis()));
            newMembresia = repository.save(newMembresia);

            map.put("membresia", ConvercionUtil.toJson(newMembresia));

            // Insersion de detalle de Membresia

            Long membresiaId = newMembresia.getId();
            this.membresiaPlanRepository.deleteByMembresiaId(membresiaId);

            List<MembresiaPlan> storedMembresiaPlan = new ArrayList<>();

            request.getPlanes().forEach(plan -> {
                MembresiaPlan newMembresiaPlan = new MembresiaPlan();
                try {
                    Plan planObject = null;
                    if (Objects.nonNull(plan.getPlanId())) {
                        planObject = planRepository.findById(plan.getPlanId()).get();
                    }


                    newMembresiaPlan.setPlanId(planObject);
                    Date fecha = null;
                    if(Objects.nonNull(plan.getFechaInicio())) {
                        fecha = BaseDate.convertStringToDate(plan.getFechaInicio(), BaseDate.FORMAT_YMD_HMS_GUION);
                    }
                    newMembresiaPlan.setFechaInicio (fecha);
                    fecha = null;
                    if(Objects.nonNull(plan.getFechaFin())) {
                        fecha = BaseDate.convertStringToDate(plan.getFechaFin(), BaseDate.FORMAT_YMD_HMS_GUION);
                    }
                    newMembresiaPlan.setFechaFin(fecha);
                } catch (ConversionException e) {
                    newMembresiaPlan.setFechaInicio(null);
                    newMembresiaPlan.setFechaFin(null);
                    LoggerMain.error(e.getMessage(), e);
                }
                newMembresiaPlan.setMonto(plan.getMonto());
                newMembresiaPlan.setMembresiaId(membresiaId);
                newMembresiaPlan = this.membresiaPlanRepository.save(newMembresiaPlan);
                storedMembresiaPlan.add(newMembresiaPlan);
            });
            // Insersion de detalle de Membresia //
            map.put("membresia_plan", ConvercionUtil.toJson(storedMembresiaPlan));

            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_MEMBRESIA", null, map);

            MembresiaResponse response = ConvercionUtil.convertToObject(newMembresia, MembresiaResponse.class);
            response.setClienteId(request.getClienteId());
            response.setUsuarioId(request.getUsuarioId());
            ResponseEntity<MembresiaResponse> out = ResponseEntity.created(
                            new URI("/membresia/" + newMembresia.getId()))
                    .body(response);

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear una membresia, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CREAR_MEMBRESIA", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_MEMBRESIA", null, map, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<MembresiaResponse> update(
            String token,
            String ipClient,
            String form,
            MembresiaRequest request,
            String id,
            BindingResult result
    ) throws ApiException {

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

            Cliente cliente = null;
            if (Objects.nonNull(request.getClienteId())) {
                cliente = clienteRepository.findById(request.getClienteId()).get();
            }

            Usuario usuario = null;
            if (Objects.nonNull(request.getUsuarioId())) {
                usuario = usuarioRepository.findById(request.getUsuarioId()).get();
            }

            Membresia updateMembresia = repository.findById(idDesencriptado).get();
            map.put("membresia", ConvercionUtil.toJson(updateMembresia));


            updateMembresia.setDescripcion(request.getDescripcion());
            updateMembresia.setTipoPago(request.getTipoPago());
            updateMembresia.setMontoTotal(request.getMontoTotal());
            updateMembresia.setEstado(request.getEstado() == "Activo" ?  (short)1 : (short)0);
            updateMembresia.setCliente(cliente);
            updateMembresia.setUsuario(usuario);
            updateMembresia.setDateUpdated(new Date(System.currentTimeMillis()));
            updateMembresia = repository.save(updateMembresia);

            mapNuevo.put("membresia", ConvercionUtil.toJson(updateMembresia));

            // Insersion de detalle de Membresia

            Long membresiaId = updateMembresia.getId();
            this.membresiaPlanRepository.deleteByMembresiaId(membresiaId);

            List<MembresiaPlan> storedMembresiaPlan = new ArrayList<>();

            request.getPlanes().forEach(plan -> {
                MembresiaPlan newMembresiaPlan = new MembresiaPlan();
                try {
                    Plan planObject = null;
                    if (Objects.nonNull(plan.getPlanId())) {
                        planObject = planRepository.findById(plan.getPlanId()).get();
                    }

                    newMembresiaPlan.setPlanId(planObject);
                    Date fecha = null;
                    if(Objects.nonNull(plan.getFechaInicio())) {
                        fecha = BaseDate.convertStringToDate(plan.getFechaInicio(), BaseDate.FORMAT_YMD_HMS_GUION);
                    }
                    newMembresiaPlan.setFechaInicio (fecha);
                    fecha = null;
                    if(Objects.nonNull(plan.getFechaFin())) {
                        fecha = BaseDate.convertStringToDate(plan.getFechaFin(), BaseDate.FORMAT_YMD_HMS_GUION);
                    }
                    newMembresiaPlan.setFechaFin(fecha);
                } catch (ConversionException e) {
                    newMembresiaPlan.setFechaInicio(null);
                    newMembresiaPlan.setFechaFin(null);
                    LoggerMain.error(e.getMessage(), e);
                }
                newMembresiaPlan.setMonto(plan.getMonto());
                newMembresiaPlan.setMembresiaId(membresiaId);
                newMembresiaPlan = this.membresiaPlanRepository.save(newMembresiaPlan);
                storedMembresiaPlan.add(newMembresiaPlan);
            });
            // Insersion de detalle de Membresia //
            map.put("membresia_plan", ConvercionUtil.toJson(storedMembresiaPlan));

            bitacoraService.guardarBitacora(token, ipClient, form, "MODIFICAR_MEMBRESIA", map, mapNuevo);
            MembresiaResponse response = ConvercionUtil.convertToObject(updateMembresia, MembresiaResponse.class);
            response.setClienteId(request.getClienteId());
            response.setUsuarioId(request.getUsuarioId());
            ResponseEntity<MembresiaResponse> out = ResponseEntity.ok().
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
            final String mensajeError = "Error al modificar una membresia, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "MODIFICAR_MEMBRESIA", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "MODIFICAR_MEMBRESIA", map, mapNuevo, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<?> delete(
            String token,
            String ipClient,
            String form,
            String id
    ) throws ApiException {

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

            //id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = Long.parseLong(id); //ConfigEncriptacion.desencryptIdToConvertLong(id);

            Optional<Membresia> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                throw new ApiException("DELETE /membresia/{id}" + id + ", No Exist");
            }

            Membresia model = temp.get();
            model.setEstado((short) 0);
            model.setDateUpdated(new Date(System.currentTimeMillis()));

            map.put("membresia", ConvercionUtil.toJson(model));
            repository.save(model);
            bitacoraService.guardarBitacora(token, ipClient, form, "ELIMINAR_MEMBRESIA", map, mapNuevo);

            ResponseEntity<Object> out = ResponseEntity.ok().build();

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;
        //} catch (ApiException | EncriptacionExcepcion e) {
        } catch (ApiException e) {
            final String mensajeError = "Error al eliminar una membresia, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "ELIMINAR_MEMBRESIA",
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "ELIMINAR_MEMBRESIA", map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }

    // Registrar Membresia Plan
    @PostMapping("/membresia_plan")
    ResponseEntity<?> guardarPlanServicio(
            @RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody MembresiaPlanRequest request
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
        Long membresiaId = request.getMembresiaId();
        this.membresiaPlanRepository.deleteByMembresiaId(membresiaId);

        List<MembresiaPlan> storedMembresiaPlan = new ArrayList<>();

        request.getPlanes().forEach(plan -> {
            MembresiaPlan newMembresiaPlan = new MembresiaPlan();
            try {
                Plan planObject = null;
                if (Objects.nonNull(plan.getPlanId())) {
                    planObject = planRepository.findById(plan.getPlanId()).get();
                }

                newMembresiaPlan.setPlanId(planObject);
                Date fecha = null;
                if(Objects.nonNull(plan.getFechaInicio())) {
                    fecha = BaseDate.convertStringToDate(plan.getFechaInicio(), BaseDate.FORMAT_YMD_HMS_GUION);
                }
                newMembresiaPlan.setFechaInicio (fecha);
                fecha = null;
                if(Objects.nonNull(plan.getFechaFin())) {
                    fecha = BaseDate.convertStringToDate(plan.getFechaFin(), BaseDate.FORMAT_YMD_HMS_GUION);
                }
                newMembresiaPlan.setFechaFin(fecha);
            } catch (ConversionException e) {
                newMembresiaPlan.setFechaInicio(null);
                newMembresiaPlan.setFechaFin(null);
                LoggerMain.error(e.getMessage(), e);
            }
            newMembresiaPlan.setMonto(plan.getMonto());
            newMembresiaPlan.setMembresiaId(membresiaId);
            newMembresiaPlan = this.membresiaPlanRepository.save(newMembresiaPlan);
            storedMembresiaPlan.add(newMembresiaPlan);
        });

        mapNuevo.put("membresia_plan", ConvercionUtil.toJson(storedMembresiaPlan));
        bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_MEMBRESIA_PLAN" + " del membresiaId " + request.getMembresiaId(), null, mapNuevo);

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
