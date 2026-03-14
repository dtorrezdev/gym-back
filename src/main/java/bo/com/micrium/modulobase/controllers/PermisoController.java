/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.com.micrium.modulobase.controllers;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.com.micrium.modulobase.commons.PermisoTipo;
import bo.com.micrium.modulobase.commons.PrivilegioTipo;
import bo.com.micrium.cifrado.ConfigEncriptacion;
import bo.com.micrium.exception.EncriptacionExcepcion;
import bo.com.micrium.logger.LoggerMain;
import bo.com.micrium.modulobase.commons.Acciones;
import bo.com.micrium.modulobase.commons.ConvercionUtil;
import bo.com.micrium.modulobase.commons.TipoAutenticacion;
import bo.com.micrium.modulobase.commons.TiposComunes;
import bo.com.micrium.modulobase.common.exceptions.ApiException;
import bo.com.micrium.modulobase.controllers.template.GenericControler;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Accion;
import com.micrium.bd.access.jpa.models.Formulario;
import com.micrium.bd.access.jpa.models.RolAccion;
import com.micrium.bd.access.jpa.models.RolTipoParametroPermiso;
import com.micrium.bd.access.jpa.models.TipoParametro;
import bo.com.micrium.modulobase.controllers.dto.AccionResponse;
import bo.com.micrium.modulobase.controllers.dto.FormularioResponse;
import bo.com.micrium.modulobase.controllers.dto.ModuloResponse;
import bo.com.micrium.modulobase.controllers.dto.PermisoRequest;
import bo.com.micrium.modulobase.controllers.dto.RolTipoParametroPermisoRequest;
import bo.com.micrium.modulobase.controllers.dto.RolTipoParametroPermisoResponse;
import com.micrium.bd.access.jpa.repositories.IAccionRepository;
import com.micrium.bd.access.jpa.repositories.IFormularioRepository;
import com.micrium.bd.access.jpa.repositories.IRolAccionRepository;
import com.micrium.bd.access.jpa.repositories.IRolTipoparametroRepository;
import com.micrium.bd.access.jpa.repositories.ITipoParametroRepository;
import bo.com.micrium.modulobase.security.utils.JwtTokenUtil;
import bo.com.micrium.modulobase.services.ParametroService;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 *
 * @author alepaco.maton
 */
@RestController
//@Service
@CrossOrigin
@RequestMapping(value = "/permisos", produces = {MediaType.APPLICATION_JSON_VALUE})
public class PermisoController extends GenericControler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient IFormularioRepository formularioRepository;

    @Autowired
    private transient IAccionRepository accionRepository;

    @Autowired
    private transient IRolAccionRepository rolAccionRepository;

    @Autowired
    private transient IRolTipoparametroRepository rolTipoparametroRepository;

    @Autowired
    private transient ITipoParametroRepository tipoparametroRepository;

    @Autowired
    private transient ParametroService parametroService;

    @GetMapping
    List<ModuloResponse> obtenerPlantillaPermisos(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form) {
        ipClient = obtenerIp(ipClient);

        LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        List<ModuloResponse> modulos = new ArrayList<>();
        int validacionActiveDirectory = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name())).intValue();
        //Boolean validacionGrupoLDAP=(Boolean) parametroService.getParamVal(ParametroID.ACTIVE_DIRECTORY_GRUPOLDAP);

        for (Formulario modulo : formularioRepository.findByModuloIdIsNull(Sort.by(Sort.Direction.ASC, "orden"))) {
            List<FormularioResponse> formularios = new ArrayList<>();

            for (Formulario formulario : formularioRepository.findByModuloId(modulo.getId(), Sort.by(Sort.Direction.ASC, "orden"))) {
                List<AccionResponse> acciones = new ArrayList<>();

                for (Accion accion : accionRepository.findByFormularioId(formulario.getId())) {

                    acciones.add(new AccionResponse(accion.getId(), accion.getNombre(), PrivilegioTipo.PERMISO_TIPO_ACCION));
                }

                if (validacionActiveDirectory == TipoAutenticacion.LOCAL.getId()) {
                    //*** dtn se comento "if (formulario.getId() != ParametroID.FORMULARIO_GRUPO)" esto para habilitar el formulario grupo para los usuario Locales
                    //if (formulario.getId() != ParametroID.FORMULARIO_GRUPO) {
                    //LoggerMain.info("----------- formularioId " + formulario.getId() +" FORMULARIO_GRUPO: "+ ParametroID.FORMULARIO_GRUPO);
                    formularios.add(new FormularioResponse(formulario.getId(), formulario.getNombre(),
                            formulario.getOrden(), PrivilegioTipo.PERMISO_TIPO_FORMULARIO,
                            formulario.getUrl(), formulario.getIcono(), acciones));
                    //}
                } else {
                    //if (!validacionGrupoLDAP){
                    formularios.add(new FormularioResponse(formulario.getId(), formulario.getNombre(),
                            formulario.getOrden(), PrivilegioTipo.PERMISO_TIPO_FORMULARIO,
                            formulario.getUrl(), formulario.getIcono(), acciones));
                    //}  
                }

            }

            modulos.add(new ModuloResponse(modulo.getId(), modulo.getNombre(), modulo.getOrden(),
                    PrivilegioTipo.PERMISO_TIPO_MODULO, modulo.getUrl(), modulo.getIcono(),
                    formularios));
        }

        LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", modulos)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return modulos;
    }

    @GetMapping("/rol/{rolId}")
    List<AccionResponse> obtenerPlantillaPermisos(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @PathVariable String rolId) throws NoHandlerFoundException {

        LoggerMain.info("obtenerPlantillaPermisos ");
        ipClient = obtenerIp(ipClient);
        
        LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("rolId ", rolId)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        Long rolIdInt = 0L;
        String desencriptadoId = "";
        try {
            rolId = rolId.replace('.','/');
            desencriptadoId = ConfigEncriptacion.decrypt(rolId);
            //log.info("desencriptadoId " + desencriptadoId); 
            rolIdInt = Long.parseLong(desencriptadoId);

        } catch (EncriptacionExcepcion | NumberFormatException e) {
            LoggerMain.error("Error: ", e);
            throw new NoHandlerFoundException("GET", "/rol/{rolId} " + e.getMessage(), HttpHeaders.EMPTY);
        }

        List<AccionResponse> acciones = new ArrayList<>();
        int validacionActiveDirectory = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name())).intValue();
        //Boolean validacionGrupoLDAP=(Boolean) parametroService.getParamVal(ParametroID.ACTIVE_DIRECTORY_GRUPOLDAP);

        for (RolAccion rolAccion : rolAccionRepository.findAllByRolId(rolIdInt)) {
            Optional<Accion> optinal = accionRepository.findById(rolAccion.getAccionId());

            if (optinal.isPresent()) {
                Accion accion = optinal.get();
                //quitar la visibilidad en parametro 1 ldap, 2 local, 3 hibrido
                if (validacionActiveDirectory == TipoAutenticacion.LOCAL.getId()) {
                    //** dtn se quito para q poder acceder Permisos de Formulario GRUPO para usuario LOCAL
                    //if (accion.getFormularioId() != ParametroID.FORMULARIO_GRUPO) {
                    acciones.add(new AccionResponse(accion.getId(), accion.getNombre(), PrivilegioTipo.PERMISO_TIPO_ACCION));
                    //}
                } else {
                    acciones.add(new AccionResponse(accion.getId(), accion.getNombre(), PrivilegioTipo.PERMISO_TIPO_ACCION));
                }
            }
        }

        LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", acciones)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return acciones;
    }

    @PostMapping
    ResponseEntity<?> guardarPermisos(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody PermisoRequest request) throws URISyntaxException, ApiException {
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

        rolAccionRepository.findAllByRolId(request.getRolId()).stream().forEach(ra -> {
            rolAccionRepository.delete(ra);
        });

        List<RolAccion> rolAcciones = new ArrayList<>();

        request.getAcciones().forEach((accionId) -> {
            rolAcciones.add(rolAccionRepository.save(new RolAccion(null, request.getRolId(), accionId)));
        });

        HashMap<String, String> mapNuevo = new HashMap<>();
        mapNuevo.put(TiposComunes.ModuloBase.PERMISOS, ConvercionUtil.toJson(rolAcciones));
        //bitacoraService.guardarBitacora(token, ipClient, form, "Actualizando permisos del rol " + request.getRolId() + ", " + rolAcciones);
        bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PERMISO_CREAR + " del rol " + request.getRolId(), null, mapNuevo);

        ResponseEntity<Object> out = ResponseEntity.ok().build();

        LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", out)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return out;
    }

    @GetMapping("/tipos/parametros/rol/{rolId}")
    List<RolTipoParametroPermisoResponse> obtenerTiposParametrosPermisos(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @PathVariable String rolId) throws NoHandlerFoundException {
        ipClient = obtenerIp(ipClient);
        LoggerMain.info("VALOR DE ROL ID ANTES: " + rolId);
        Long descrp;
        
        LoggerMain.printRequest(Stream.of(
                new AbstractMap.SimpleEntry<>("url ", httpServletRequest.getRequestURL()),
                new AbstractMap.SimpleEntry<>("metodo ", httpServletRequest.getMethod()),
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("form ", form),
                new AbstractMap.SimpleEntry<>("rolId ", rolId)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        String desencriptadoId = rolId;
        try {
            rolId = rolId.replace('.', '/');
            desencriptadoId = ConfigEncriptacion.decrypt(rolId);
            LoggerMain.debug("desencriptadoId " + desencriptadoId);
            descrp = Long.valueOf(desencriptadoId);

        } catch (EncriptacionExcepcion | NumberFormatException e) {
            LoggerMain.error("Error: ", e);
            throw new NoHandlerFoundException("GET", "/{id} " + e.getMessage() + desencriptadoId, HttpHeaders.EMPTY);
        }

        List<RolTipoParametroPermisoResponse> temp = rolTipoparametroRepository.findAllByRolId(descrp).stream().map(m -> {
            return new RolTipoParametroPermisoResponse(m.getId(), m.getRolId(), m.getTipoParametroId(),
                    tipoparametroRepository.findById(m.getTipoParametroId()).get().getNombre(), m.getTipoPermiso());
        }).collect(Collectors.toList());

        int validacionActiveDirectory = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name())).intValue();
        LoggerMain.info("VALOR DE ROL ID Despues: " + rolId + " desencriptado= " + desencriptadoId);
        for (TipoParametro tipoParametro : tipoparametroRepository.findAll()) {
            if (!temp.stream().anyMatch(t -> t.getTipoParametroId().equals(tipoParametro.getId()))) {
                // agregar condicion si es Admin (PermisoTipo.2)
                RolTipoParametroPermiso model;
                if (desencriptadoId.trim().equals("1")) {
                    model = new RolTipoParametroPermiso(null, descrp, tipoParametro.getId(), PermisoTipo.PERMISO_LECTURA_ESCRITURA);
                } else {
                    model = new RolTipoParametroPermiso(null, descrp, tipoParametro.getId(), PermisoTipo.PERMISO_NINGUNO);
                }
                model = rolTipoparametroRepository.save(model);

                HashMap<String, String> mapNuevo = new HashMap<>();
                mapNuevo.put(TiposComunes.ModuloBase.PERMISOS, ConvercionUtil.toJson(model));
                //bitacoraService.guardarBitacora(token, ipClient, form, "Se adiciono automaticamente al rol " + rolId + ", el rol tipo de paramaetro permiso " + model);
                bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PERMISO_TIPO_PARAM_CREAR + " automaticamente al rol " + rolId, null, mapNuevo);
            }
        }

        temp = rolTipoparametroRepository.findAllByRolId(descrp).stream().map(m -> {
            return new RolTipoParametroPermisoResponse(m.getId(), m.getRolId(), m.getTipoParametroId(),
                    tipoparametroRepository.findById(m.getTipoParametroId()).get().getNombre(), m.getTipoPermiso());
        }).collect(Collectors.toList());

        if (validacionActiveDirectory == TipoAutenticacion.LOCAL.getId()) {
            temp = temp.stream().filter(pa -> !pa.getTipoParametroId().equals(com.micrium.bd.access.enuns.TipoParametro.LDAP.getId().longValue())).
                    collect(Collectors.toList());
        }

        LoggerMain.printResponse(Stream.of(
                new AbstractMap.SimpleEntry<>("token ", token),
                new AbstractMap.SimpleEntry<>("ipClient ", ipClient),
                new AbstractMap.SimpleEntry<>("trazabilidad ", obtenerNombreUsuario()),
                new AbstractMap.SimpleEntry<>("response ", temp)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return temp;
    }

    @PostMapping("/tipos/parametros")
    ResponseEntity<?> guardarTiposParametrosPermisos(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody RolTipoParametroPermisoRequest request) {
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

        rolTipoparametroRepository.findAllByRolId(request.getRolId()).stream().forEach(ra -> {
            rolTipoparametroRepository.delete(ra);
        });

        List<RolTipoParametroPermiso> rolTipoParametroPermisos = new ArrayList<>();

        request.getTiposParametrosPermisos().forEach((tipoParametro) -> {
            rolTipoParametroPermisos.add(rolTipoparametroRepository.save(
                    new RolTipoParametroPermiso(null, request.getRolId(), tipoParametro.getTipoParametroId(), tipoParametro.getTipoPermiso())));
        });

        HashMap<String, String> mapNuevo = new HashMap<>();
        mapNuevo.put(TiposComunes.ModuloBase.PERMISOS, ConvercionUtil.toJson(rolTipoParametroPermisos));
        //bitacoraService.guardarBitacora(token, ipClient, form, "Actualizando tipos de parametros permisos del rol " + request.getRolId() + ", " + rolTipoParametroPermisos);
        bitacoraService.guardarBitacora(token, ipClient, form, Acciones.PERMISO_TIPO_PARAM_CREAR + " del rol " + request.getRolId(), null, mapNuevo);

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
