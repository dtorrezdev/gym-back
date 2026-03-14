package bo.com.micrium.modulobase.controllers;

import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.exception.PageException;
import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.common.exceptions.ApiException;
import bo.com.micrium.modulobase.commons.*;
import bo.com.micrium.modulobase.controllers.dto.ClienteRequest;
import bo.com.micrium.modulobase.controllers.dto.ClienteResponse;
import bo.com.micrium.modulobase.controllers.template.GenericControler;
import bo.com.micrium.modulobase.controllers.template.ICrudControler;
import bo.com.micrium.modulobase.validators.ClienteValidator;
import com.micrium.bd.access.jpa.models.Cliente;
import com.micrium.bd.access.jpa.repositories.IClienteRepository;
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
@RequestMapping(value = "/cliente", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ClienteController extends GenericControler
 implements ICrudControler<ClienteRequest, ClienteResponse, String> {

    @Autowired
    private transient IClienteRepository repository;

    @Autowired
    private transient ClienteValidator validator;


    @Override
    public Page<ClienteResponse> list(String token, String ipClient, String form, Pageable pageRequest) throws ApiException {

        try {

            Map<String, String> parametros = getParametersMap(httpServletRequest);
            validator.page(parametros);
            validator.validateListar(parametros);
            final String id = filterTextoQueryUpper(parametros.get("id"));
            final String ci = filterTextoQueryUpper(parametros.get("ci"));
            final String nombre = filterTextoQueryUpper(parametros.get("nombre"));
            final String genero = filterTextoQueryUpper(parametros.get("genero"));
            final String telefono = filterTextoQueryUpper(parametros.get("telefono"));
            final String direccion = filterTextoQueryUpper(parametros.get("direccion"));

            ipClient = obtenerIp(ipClient);
            LoggerMain.printRequest(Stream.of(
                            new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                            new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("form ", form),
                            new AbstractMap.SimpleEntry<>("id ", filterTexto(id)),
                            new AbstractMap.SimpleEntry<>("ci ", filterTexto(ci)),
                            new AbstractMap.SimpleEntry<>("nombre ", filterTexto(nombre)),
                            new AbstractMap.SimpleEntry<>("genero ", filterTexto(genero)),
                            new AbstractMap.SimpleEntry<>("telefono ", filterTexto(telefono)),
                            new AbstractMap.SimpleEntry<>("direccion ", filterTexto(direccion)),
                            new AbstractMap.SimpleEntry<>("request ", pageRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            Page<ClienteResponse> out = repository.filter(
                queryfilterTexto(id), filterTextoQueryUpperLike(id),
                queryfilterTexto(ci), filterTextoQueryUpperLike(ci),
                queryfilterTexto(nombre), filterTextoQueryUpperLike(nombre),
                queryfilterTexto(genero), filterTextoQueryUpperLike(genero),
                queryfilterTexto(telefono), filterTextoQueryUpperLike(telefono),
                queryfilterTexto(direccion), filterTextoQueryUpperLike(direccion),
                pageRequest)
                    .map(model -> {
                        return ConvercionUtil.convertToObject(model, ClienteResponse.class);
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
            final String mensajeError = "Error al filtrar clientes, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, Acciones.FILTRAR, mensajeError, e);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, Acciones.FILTRAR, null, map, logSistemaId);

            throw new ApiException( mensajeError, e);
        }
    }

    @Override
    public ResponseEntity<ClienteResponse> get(String token, String ipClient, String form, String id) throws ApiException {
        throw new ApiException("GET not support method /{id}" + id);
    }

    @Override
    public ResponseEntity<ClienteResponse> create(String token,
                                                  String ipClient,
                                                  String form,
                                                  ClienteRequest request,
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
            Cliente newCliente = new Cliente();
            newCliente.setCi(request.getCi());
            newCliente.setNombre(request.getNombre());
            newCliente.setTelefono(request.getTelefono());
            newCliente.setDireccion(request.getDireccion());
            newCliente.setGenero(request.getGenero());
            newCliente.setDateCreated(new Date(System.currentTimeMillis()));
            Cliente model = repository.save(newCliente);
            map.put("cliente", ConvercionUtil.toJson(model));

            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_CLIENTE", null, map);

            ResponseEntity<ClienteResponse> out = ResponseEntity.created(
                            new URI("/cliente/" + model.getId()))
                    .body(ConvercionUtil.convertToObject(model, ClienteResponse.class));

            LoggerMain.printResponse(Stream.of(
                            new AbstractMap.SimpleEntry<>("token ", token),
                            new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                            new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                            new AbstractMap.SimpleEntry<>("response ", out)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return out;

        } catch (ApiException | URISyntaxException e) {
            final String mensajeError = "Error al crear un cliente, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CREAR_CLIENTE", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CREAR_CLIENTE", null, map, logSistemaId);

            throw e;
        }
    }

    @Override
    public ResponseEntity<ClienteResponse> update(String token,
                                                  String ipClient,
                                                  String form,
                                                  ClienteRequest request,
                                                  String id,
                                                  BindingResult result) throws ApiException {
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> mapNuevo = new HashMap<String, String>();

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

            Cliente model = repository.findById(idDesencriptado).get();

            map.put("cliente", ConvercionUtil.toJson(model));
            model.setCi(request.getCi());
            model.setNombre(request.getNombre());
            model.setGenero(request.getGenero());
            model.setTelefono(request.getTelefono());
            model.setDireccion(request.getDireccion());
            model.setDateUpdated(new Date(System.currentTimeMillis()));
            repository.save(model);

            mapNuevo.put("cliente", ConvercionUtil.toJson(model));

            bitacoraService.guardarBitacora(token, ipClient, form, "CLIENTE_MODIFICAR", map, mapNuevo);

            ResponseEntity<ClienteResponse> out = ResponseEntity.ok().
                    body(ConvercionUtil.convertToObject(model, ClienteResponse.class));

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
            final String mensajeError = "Error al modificar un cliente, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CLIENTE_MODIFICAR", mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CLIENTE_MODIFICAR", map, mapNuevo, logSistemaId);

            throw new ApiException(mensajeError);
        }
    }

    @Override
    public ResponseEntity<?> delete(String token,
                                    String ipClient,
                                    String form,
                                    String id) throws ApiException {
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
                            new AbstractMap.SimpleEntry<>("id ", id)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            id = limpiarCaracterEspecialEncriptacion(id);
            Long idDesencriptado = ConfigEncriptacion.desencryptIdToConvertLong(id);

            Optional<Cliente> temp = repository.findById(idDesencriptado);

            if (!temp.isPresent()) {
                throw new ApiException("DELETE /cliente/{id}" + id + ", No Exist");
            }

            Cliente model = temp.get();
            model.setDateUpdated(new Date(System.currentTimeMillis()));

            map.put("Cliente", ConvercionUtil.toJson(model));
            repository.delete(model);
            bitacoraService.guardarBitacora(token, ipClient, form, "CLIENTE_ELIMINAR", map, mapNuevo);

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
            final String mensajeError = "Error al eliminar un cliente, " + e.getMessage();

            final Long logSistemaId = logWeb.error(obtenerNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA, "CLIENTE_ELIMINAR",
                    mensajeError, e);
            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);
            bitacoraService.guardarBitacora(token, ipClient, form, "CLIENTE_ELIMINAR", map, mapNuevo, logSistemaId);

            throw new ApiException(e.getMessage(), e);
        }
    }
}
