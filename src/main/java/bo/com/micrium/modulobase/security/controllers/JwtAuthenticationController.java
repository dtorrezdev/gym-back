package bo.com.micrium.modulobase.security.controllers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.Apps;
import bo.com.micrium.modulobase.commons.TiposComunes;
import bo.com.micrium.modulobase.commons.PrivilegioTipo;
import bo.com.micrium.modulobase.commons.UsuarioEstado;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Accion;
import com.micrium.bd.access.jpa.models.Formulario;
import com.micrium.bd.access.natives.model.dto.ParametroDto;
import com.micrium.bd.access.jpa.models.Rol;
import com.micrium.bd.access.jpa.models.RolAccion;
import com.micrium.bd.access.jpa.models.Usuario;
import com.micrium.bd.access.jpa.repositories.IAccionRepository;
import com.micrium.bd.access.jpa.repositories.IFormularioRepository;
import com.micrium.bd.access.jpa.repositories.IRolAccionRepository;
import bo.com.micrium.modulobase.common.exceptions.ExceptionResponse;
import bo.com.micrium.modulobase.controllers.template.GenericControler;
import bo.com.micrium.modulobase.controllers.dto.AccionResponse;
import bo.com.micrium.modulobase.security.controllers.dto.AutenticacionRequest;
import bo.com.micrium.modulobase.security.controllers.dto.AutenticacionResponse;
import bo.com.micrium.modulobase.security.services.AuthenticationLdapManager;
import bo.com.micrium.modulobase.security.utils.JwtTokenUtil;
import bo.com.micrium.modulobase.controllers.dto.FormularioResponse;
import bo.com.micrium.modulobase.controllers.dto.ModuloResponse;
import bo.com.micrium.modulobase.services.ParametroService;
import bo.com.micrium.exception.SerializeException;
//import bo.com.micrium.logger.AbstractLogger;
import bo.com.micrium.logger.LoggerMain;
//import bo.com.micrium.modulobase.services.LoggerWeb;
import bo.com.micrium.modulobase.commons.TipoAutenticacion;
import bo.com.micrium.serialize.Serializador;
import bo.com.micrium.cifrado.ConfigEncriptacion;
//import bo.com.micrium.exception.EncriptacionExcepcion;

/**
 *
 * @author alepaco.maton
 */
@RestController
@CrossOrigin
public class JwtAuthenticationController extends GenericControler {

    private static final Logger log = LogManager.getLogger(JwtAuthenticationController.class);

    public static final String METODO_VERSION = "/version";
    public static final String METODO_AUTENTICACION = "/autenticacion";

    @Autowired
    private transient AuthenticationLdapManager authenticationManager;

    @Autowired
    private transient IFormularioRepository formularioRepository;

    @Autowired
    private transient IRolAccionRepository rolAccionRepository;

    @Autowired
    private transient IAccionRepository accionRepository;

    @Autowired
    private transient ParametroService parametroService;

    @RequestMapping(value = METODO_VERSION, method = RequestMethod.GET)
    public ResponseEntity<?> version() {
        ParametroDto modoSistema = parametroService.getParametroByNombre(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name());

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Version", "Version 1.0");
        map.put("Modo del sistema", modoSistema.getValor());

        return ResponseEntity.ok(map.toString());
    }

    @RequestMapping(value = METODO_AUTENTICACION, method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody String authenticationRequestString, BindingResult result) {
        log.info("Llego aqui");
        HashMap<String, String> map = new HashMap<String, String>();
        AutenticacionRequest authenticationRequest = null;
        //
        LoggerMain.info("***dtn java.class.path: " + System.getProperty("java.class.path"));
        try {
            // *** dtn cambios
            //authenticationRequest = new ObjectMapper().readValue(authenticationRequestString, AuteticacionRequest.class);
            authenticationRequest = Serializador.convertStringJsonToObject(authenticationRequestString, bo.com.micrium.modulobase.security.controllers.dto.AutenticacionRequest.class);
        } catch (SerializeException e) {
            LoggerMain.error("ERROR: ", e);
            return ResponseEntity.badRequest().body(new ExceptionResponse(
                    HttpStatus.BAD_REQUEST, "Error procesamiento", "No se ha mapeado el objeto correctamente."));
        }
        try {
            if (authenticationRequest.getContrasena() == null && authenticationRequest.getNombreUsuario() == null) {
                return ResponseEntity.badRequest().body(new ExceptionResponse(
                        HttpStatus.BAD_REQUEST, "Error procesamiento", "usuario o contraseña nulos"));
            }
            /*if (authenticationRequest.getContrasena() == null && authenticationRequest.getNombreUsuario() == null) {
                //return ResponseEntity.badRequest().body(new ExceptionResponse(HttpStatus.BAD_REQUEST, "Error procesamiento", "usuario o contraseña nulos"));
                return ResponseEntity.badRequest().body(new ExceptionResponse(HttpStatus.UNAUTHORIZED, "Error procesamiento", "usuario o contraseña nulos"));
            }*/

            ipClient = obtenerIp(ipClient);

            LoggerMain.printRequest(Stream.of(
                    new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                    new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                    new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                    new AbstractMap.SimpleEntry<>("form ", form),
                    new AbstractMap.SimpleEntry<>("request ", authenticationRequest)).
                    collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            Rol rol = null;

            try {
                rol = authenticationManager.validarCredenciales(authenticationRequest.getNombreUsuario(),
                        authenticationRequest.getContrasena());
            } catch (BadCredentialsException | InsufficientAuthenticationException | AccountStatusException e) {
                //LoggerMain.error(e.getMessage(), e);
                //LoggerMain.error("ABC", "AAA", AbstractLogger.getMsgError(e));
                map.put(TiposComunes.MENSAJE_ERROR, "Autenticacion fallida, " + e.getMessage());
                bitacoraService.guardarBitacora(null, ipClient, form, Acciones.AUTENTICACION, null, map);
                //return ResponseEntity.badRequest().body(new ExceptionResponse(HttpStatus.BAD_REQUEST, "Error procesamiento", e.getMessage()));
                return ResponseEntity.badRequest().body(new ExceptionResponse(HttpStatus.UNAUTHORIZED, "Error procesamiento", e.getMessage()));
            }

            final String token = jwtTokenUtil.generateToken(authenticationRequest.getNombreUsuario(), rol.getNombre());

            List<ModuloResponse> modulos = new ArrayList<>();
            List<Long> formularios = new ArrayList<>();
            Map<Long, Boolean> formVisible = new HashMap<>();

//            int validacionActiveDirectory = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY)).intValue();
            List<RolAccion> rolAcciones = rolAccionRepository.findAllByRolId(rol.getId());
            for (RolAccion rolAccion : rolAcciones) {
                /*if (1701 == rolAccion.getAccionId() && rol.getId() != Rol.SUPER_ADMINISTRADOR) {
                    continue;
                }*/

                //Accion accion = accionRepository.getOne(rolAccion.getAccionId());
                Accion accion = accionRepository.getReferenceById(rolAccion.getAccionId());

                Optional<Formulario> optionalForm = formularioRepository.findById(accion.getFormularioId());

                Formulario formulario = optionalForm.get();

//                if (!(validacionActiveDirectory == Parametro.DelSistema.LOCAL && formulario.getId() == Parametro.DelSistema.FORMULARIO_GRUPO)) {
                if (!formularios.stream().anyMatch(id -> id.equals(accion.getFormularioId()))) {
                    formularios.add(accion.getFormularioId());
                    formVisible.put(accion.getFormularioId(), accion.getNombre().contains("Navega"));
                    Optional<Formulario> optionalMod = formularioRepository.findById(formulario.getModuloId());

                    Formulario modulo = optionalMod.get();

                    List<AccionResponse> tempAcciones = new ArrayList<>();
                    tempAcciones.add(new AccionResponse(accion.getId(), accion.getNombre(), PrivilegioTipo.PERMISO_TIPO_ACCION));

                    FormularioResponse tempFormulario = new FormularioResponse(formulario.getId(),
                            formulario.getNombre(), formulario.getOrden(),
                            PrivilegioTipo.PERMISO_TIPO_FORMULARIO, formulario.getUrl(), formulario.getIcono(), tempAcciones);

                    Optional<ModuloResponse> optionalModuloRespo = modulos.stream().
                            filter(m -> m.getId() == formulario.getModuloId()).findFirst();

                    ModuloResponse moduloResponse;

                    if (optionalModuloRespo.isPresent()) {
                        moduloResponse = optionalModuloRespo.get();
                    } else {
                        moduloResponse = new ModuloResponse(modulo.getId(), modulo.getNombre(), modulo.getOrden(),
                                PrivilegioTipo.PERMISO_TIPO_MODULO, modulo.getUrl(), modulo.getIcono(), new ArrayList<>());

                        modulos.add(moduloResponse);
                    }
                    moduloResponse.getFormularios().add(tempFormulario);

                } else {
                    ModuloResponse moduloResponse = modulos.stream().
                            filter(m -> m.getId() == formulario.getModuloId()).findFirst().get();

                    Optional<FormularioResponse> optionalFormRes = moduloResponse.getFormularios().
                            stream().filter(f -> f.getId().equals(formulario.getId())).findFirst();
                    optionalFormRes.get().getAcciones().add(
                            new AccionResponse(accion.getId(), accion.getNombre(),
                                    PrivilegioTipo.PERMISO_TIPO_ACCION));
                    if (!formVisible.get(accion.getFormularioId())) {
                        formVisible.put(accion.getFormularioId(), accion.getNombre().contains("Navega"));
                    }
                }
//                }
            }

            Collections.sort(modulos);

            modulos.forEach(m
                    -> {
                m.sortFormularios();
                m.removeNotVisible(formVisible);
            }
            );

            map.put(TiposComunes.MENSAJE, "Autenticacion exitosa.");

            bitacoraService.guardarBitacora("Bearer " + token, ipClient, form,
                    Acciones.AUTENTICACION, null, map);

            Usuario usuario = usuarioRepository.findByNombreUsuarioAndEstadoIn(authenticationRequest.getNombreUsuario(), Arrays.asList(UsuarioEstado.HABILITADO));

            ParametroDto inactivityTime = parametroService.getParametroByNombre(Parametro.DelSistema.INACTIVITY_TIME.name());
            ParametroDto timeoutBackend = parametroService.getParametroByNombre(Parametro.DelSistema.TIME_OUT_BACKEND.name());
            ParametroDto urlNoTimeoutBackend = parametroService.getParametroByNombre(Parametro.DelSistema.URL_NO_TIME_OUT_BACKEND.name());
            ParametroDto tipoAD = parametroService.getParametroByNombre(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name());
            ParametroDto fraseSecreta = parametroService.getParametroByNombre(Parametro.DelSistema.FRASE_SECRETA_ENCRIPTAR.name());
            ConfigEncriptacion.phaseSecret = fraseSecreta.getValor();
            try {
                String tipoAuth = tipoAD.getValor().equals(TipoAutenticacion.HIBRIDO.getId())? "1": tipoAD.getValor();
                ResponseEntity<AutenticacionResponse> out = ResponseEntity.ok(new AutenticacionResponse(
                        usuario.getId(),
                        token, rol.getId(), rol.getNombre(),
                        modulos, usuario.getNombreCompleto(),
                        inactivityTime.getValor(), timeoutBackend.getValor(),
                        urlNoTimeoutBackend.getValor(), tipoAuth,
                        ConfigEncriptacion.btoa(fraseSecreta.getValor())));

                LoggerMain.printResponse(Stream.of(
                        new AbstractMap.SimpleEntry<>("token ", token),
                        new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                        new AbstractMap.SimpleEntry<>("trazabilidad ", authenticationRequest.getNombreUsuario()),
                        new AbstractMap.SimpleEntry<>("response ", out)).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                return out;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }

        } catch (AuthenticationException e) {
            final String mensajeError = "Error al cambiar la contraseña, " + e.getMessage();

            final Long logSistemaId = logWeb.error(authenticationRequest.getNombreUsuario(), Apps.TRAZABILIDAD_SISTEMA,
                    Acciones.AUTENTICACION, mensajeError, e);

            map.put(TiposComunes.MENSAJE_ERROR, mensajeError);

            bitacoraService.guardarBitacora(null, ipClient, form,
                    Acciones.AUTENTICACION, null, map, logSistemaId);

            throw e;
        }
    }
}
