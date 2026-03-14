package bo.com.micrium.modulobase.security.services;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bo.com.micrium.modulobase.commons.GrupoEstado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import bo.com.micrium.modulobase.commons.TipoAutenticacion;
import bo.com.micrium.modulobase.commons.UsuarioEstado;
import bo.com.micrium.modulobase.commons.UsuarioTipo;
import bo.com.micrium.modulobase.security.services.ActiveDirectoryService;
import com.micrium.bd.access.jpa.repositories.IUsuarioRepository;
import com.micrium.bd.access.jpa.repositories.IGrupoRepository;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import bo.com.micrium.modulobase.services.ParametroService;
import com.micrium.bd.access.jpa.models.Usuario;
import bo.com.micrium.modulobase.common.exceptions.LdapContextException;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Grupo;
import com.micrium.bd.access.jpa.models.Rol;
import bo.com.micrium.logger.AbstractLogger;
import bo.com.micrium.logger.LoggerMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author alepaco.maton
 */
@Component
public class AuthenticationLdapManager implements AuthenticationManager, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LogManager.getLogger(AuthenticationLdapManager.class);

    @Autowired
    private transient IUsuarioRepository usuarioRepository;

    @Autowired
    private transient IRolRepository rolRepository;

    @Autowired
    private transient IGrupoRepository grupoRepository;     //  37327 coverity

    @Autowired
    private transient ParametroService parametroService;

    @Autowired
    private transient BCryptPasswordEncoder passwordEncoder;

    private Rol validarUsuarioLocal(Usuario usuario, String contrasena) {
        if (usuario == null) {
            throw new BadCredentialsException("El usuario o contraseña ingresados son incorrectos") {
                private static final long serialVersionUID = 5538299138211283825L;
            };
        }

        if (usuario.getEstado() == UsuarioEstado.BLOQUEADO) {
            usuario = cambiaEstadoUsuarioAHabilitadoSiPasoElTiempoDeBloqueo(usuario);
        }

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            usuario = contarNroIntentoDeInicioLogin(usuario);
            usuarioRepository.save(usuario);

            throw new BadCredentialsException("El usuario o contraseña ingresados son incorrectos") {
                private static final long serialVersionUID = 5538299138211283825L;
            };
        }

        usuario.setNumeroIntentos((short) 0);
        usuario.setUltimoIntento(null);

        usuarioRepository.save(usuario);

        return usuario.getRolId();
    }

    private Usuario cambiaEstadoUsuarioAHabilitadoSiPasoElTiempoDeBloqueo(Usuario usuario) {
        int tiempoDeBloqueo = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.TIEMPO_BLOQUEO_AUTENTICACION.name())).intValue();

        Calendar cal = Calendar.getInstance();
        cal.setTime(usuario.getUltimoIntento());
        cal.add(Calendar.MINUTE, tiempoDeBloqueo);

        if (cal.getTime().getTime() < (new Date()).getTime()) {
            usuario.setNumeroIntentos((short) 0);
            usuario.setEstado(UsuarioEstado.HABILITADO);
        } else {
            throw new DisabledException("Usuario: " + '"' + usuario.getNombreUsuario() + '"' + " ha sido bloqueado por varios reintentos. vuelva a intentar en " + tiempoDeBloqueo + " minutos.");
        }
        return usuario;
    }

    private Usuario contarNroIntentoDeInicioLogin(Usuario usuario) {
        int nroIntentos = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.NUMERO_INTENTOS_AUTENTICACION.name())).intValue();
        int tiempoDeBloqueo = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.TIEMPO_BLOQUEO_AUTENTICACION.name())).intValue();

        if (usuario.getUltimoIntento() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(usuario.getUltimoIntento());
            cal.add(Calendar.MINUTE, tiempoDeBloqueo);

            Date actual = new Date();
            log.info("*** OAC Tiempo ultimoIntento: " + cal.getTime() + " TiempoActual: " + actual);
            if (cal.getTime().getTime() < actual.getTime()) {
                log.info("*** OAC es mayor a 5 min");
                usuario.setNumeroIntentos((short) 0);
            }
        }

        usuario.setUltimoIntento(new Date());
        if (usuario.getNumeroIntentos() >= nroIntentos) {
            log.info("*** OAC numeroIntentos es mayor nroOption");
            usuario.setEstado(UsuarioEstado.BLOQUEADO);
            throw new DisabledException("El usuario " + '"' + usuario.getNombreUsuario() + '"' + " ha sido inhabilitado. vuelva a intentar en " + tiempoDeBloqueo + " minutos.") {
                private static final long serialVersionUID = 5538299138211283825L;
            };
        }

        usuario.setNumeroIntentos((short) (usuario.getNumeroIntentos() + 1));

        return usuario;
    }

    public Rol validarCredenciales(String nombreUsuario, String contrasena) throws AuthenticationException {
        int validacionActiveDirectory = ((BigDecimal) parametroService.getParamVal(Parametro.DelSistema.VALIDACION_ACTIVE_DIRECTORY.name())).intValue();
        log.info("validacionActiveDirectory: " + validacionActiveDirectory);
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEstadoIn(nombreUsuario, Arrays.asList(UsuarioEstado.HABILITADO, UsuarioEstado.BLOQUEADO, UsuarioEstado.INHABILITADO));

        LoggerMain.debug("***** OAC validacion si es usuario null y LOCAL");
        if (usuario == null && validacionActiveDirectory == TipoAutenticacion.LOCAL.getId()) {
            throw new BadCredentialsException("Usuario no existe en el sistema");
            //throw new BadCredentialsException("El usuario no existe en la BD."); // el usuario no existe en la BD            
        }

        LoggerMain.debug("***** OAC validacion si es usuario null y GRUPO LDAP");
        if (usuario == null && (validacionActiveDirectory == TipoAutenticacion.LDAP.getId() || validacionActiveDirectory == TipoAutenticacion.HIBRIDO.getId())) {
            try {
                Boolean validacionGrupoLDAP = (Boolean) parametroService.getParamVal(Parametro.DelSistema.ACTIVE_DIRECTORY_GRUPOLDAP.name());
                log.debug("************* OAC Logueo por logica de GrupoLDAP - validacionGrupoLDAP: " + validacionGrupoLDAP);
                //Si el usuario no existe, empieza a intentar loguear por Logica de GrupoLDAP, primero verifica parametro
                if (!validacionGrupoLDAP) {
                    throw new InsufficientAuthenticationException("Usuario no valido en el sistema");
                }

                List<Grupo> gruposAD = new ArrayList<>();
                for (String grupoAD : new ActiveDirectoryService(parametroService).getListaGrupos(nombreUsuario)) {
                    Grupo grupo = grupoRepository.findByNombreAndEstado(grupoAD, GrupoEstado.HABILITADO);
                    if (grupo != null) {
                        gruposAD.add(grupo);
                    }
                }
                
                /**
                 * Si el usuario no tiene ningun grupo en active directory
                 * entonces no puede loggearse al sistema en el caso que tenga
                 * habilitada la validacion de grupos de active directory
                 */
                if (gruposAD.isEmpty()) {
                    throw new InsufficientAuthenticationException("Usted no tiene grupo de trabajo. Solicite a su administrador que lo incluya a un grupo de trabajo");
                }

                /**
                 * en el caso que el rol asignado al usuario no tengan ya no se
                 * encuentre relacionado con mis grupos de active directory,
                 * pero tenga un grupo de active directory que esta registrado
                 * en el sistema, entonces retorno el rol de ese grupo de active
                 * directory que tengo relacionado y que se encuentra en el
                 * sistema
                 */
                return gruposAD.stream().findFirst().get().getRolId();
            } catch (RuntimeException | LdapContextException e) {
                log.error("Error al iniciar sesion con " + nombreUsuario + ", " + e.getMessage());
                throw new BadCredentialsException("El usuario no pudo iniciar sesion. Problemas con LDAP. " + e.getMessage());
            }
        }

        //  si el usuario es null, no deberia suceder este escenario, de seguro existe mala configuracion en parametro
        if (usuario == null) {
            throw new BadCredentialsException("Usuario no existe en el sistema o mal cnonfigurado en la autenticacion");
        }

        LoggerMain.debug("***** OAC validacion ESTADO: " + usuario.getEstado());
        if (usuario.getEstado() == UsuarioEstado.INHABILITADO) {
            throw new BadCredentialsException("El Usuario esta inhabilitado en el sistema");
        }

        if (usuario.getEstado() == UsuarioEstado.BLOQUEADO) {
            usuario = cambiaEstadoUsuarioAHabilitadoSiPasoElTiempoDeBloqueo(usuario);
        }

        LoggerMain.debug("***** OAC validacion si es SUPER ADMIN, no importa como este configurado la autentificacion");
        if (usuario.getTipo().equals(TipoAutenticacion.SUPER.getId())) {
            return validarUsuarioLocal(usuario, contrasena);
        }

        LoggerMain.debug("***** OAC validacion si es LOCAL");
        if (validacionActiveDirectory == TipoAutenticacion.LOCAL.getId()) {
            LoggerMain.debug("***** OAC tipoAuth LOCAL: " + usuario.getTipo());
            if (usuario.getTipo().equals(TipoAutenticacion.LOCAL.getId())) {
                return validarUsuarioLocal(usuario, contrasena);
            }
            if (validacionActiveDirectory == TipoAutenticacion.LOCAL.getId()) {
                //  throw new BadCredentialsException("Usuario LDAP no valido en el sistema");
                throw new BadCredentialsException("El sistema solo permite ingresar usuarios locales.");
            }
        }

        LoggerMain.debug("***** OAC validacion si es LDAP o HIBRIDO: " + usuario.getTipo());
        if (validacionActiveDirectory == TipoAutenticacion.LDAP.getId() || validacionActiveDirectory == TipoAutenticacion.HIBRIDO.getId()) {
            if (validacionActiveDirectory != TipoAutenticacion.HIBRIDO.getId() && usuario.getTipo().equals(TipoAutenticacion.LOCAL.getId())) {
                throw new BadCredentialsException("El sistema solo permite ingresar usuarios LDAP.");
            }
            Boolean existeUsuarioLDAP = existeUsuarioEnLDAPConLaCredenciales(nombreUsuario, contrasena);
            LoggerMain.debug("***** OAC existeUsuarioLDAP: " + existeUsuarioLDAP);
            if (existeUsuarioLDAP) {
                usuario.setNumeroIntentos((short) 0);
                usuario.setUltimoIntento(null);
                usuarioRepository.save(usuario);
                return usuario.getRolId();
            }
            if (validacionActiveDirectory == TipoAutenticacion.LDAP.getId()) {
                usuario = contarNroIntentoDeInicioLogin(usuario);
                usuarioRepository.save(usuario);
                throw new BadCredentialsException("El usuario o contraseña ingresados son incorrectos");
            }
        }

        LoggerMain.debug("***** OAC validacion si es LOCAL HIBRIDO");
        if (validacionActiveDirectory == TipoAutenticacion.HIBRIDO.getId()) {
            return validarUsuarioLocal(usuario, contrasena);
        }

        // no deberia suceder este escenario...
        throw new BadCredentialsException("El usuario no pudo iniciar sesion.");
    }

    private Boolean existeUsuarioEnLDAPConLaCredenciales(String nombreUsuario, String contrasena) {
        try {
            return new ActiveDirectoryService(parametroService).validarUsuario(nombreUsuario, contrasena);
        } catch (LdapContextException ex) {
            //log.warn("usuario: " + nombreUsuario + ", No pudo iniciar session " + ex.getMessage(), ex);
            log.error("usuario: " + nombreUsuario + ", No pudo iniciar session " + AbstractLogger.getMsgError(ex));
        }
        return false;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("authentication " + authentication);
        log.info("authentication " + authentication.getPrincipal());
        log.info("authentication " + authentication.getCredentials());
        log.info("authentication " + authentication.getName());
        log.info("authentication " + authentication.getDetails());
        return authentication;
    }

}
