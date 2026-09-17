package com.dtorrez.gym.validators;

import bo.com.micrium.modulobase.commons.GlobalValidator;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Rol;
import com.micrium.bd.access.jpa.models.Usuario;
import com.micrium.bd.access.jpa.repositories.IRolRepository;
import com.micrium.bd.access.jpa.repositories.IUsuarioRepository;
import com.dtorrez.gym.controllers.dto.CambioContrasenaRequest;
import com.dtorrez.gym.controllers.dto.CambioContrasenaRequestLogin;
import com.dtorrez.gym.controllers.dto.UsuarioRequest;
import com.dtorrez.gym.services.ParametroService;

import bo.com.micrium.modulobase.commons.TipoAutenticacion;
import bo.com.micrium.modulobase.commons.UsuarioEstado;
import java.util.Map;

/**
 *
 * @author alepaco.maton
 */
@Component
public class UsuarioValidator extends GlobalValidator {

    @Autowired
    IUsuarioRepository usuarioRepository;

    @Autowired
    IRolRepository rolRepository;

    @Autowired
    ParametroService parametroService;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public void validateListar(Map<String, String> parametros) throws Exception {
        //LoggerMain.info("***dtn validateListar " + parametros.size());
        final String id = parametros.get("id");
        final String nombreCompleto = parametros.get("nombreCompleto");
        final String nombreUsuario = parametros.get("nombreUsuario");
        final String rolId = parametros.get("rolId");
        final String rolNombre = parametros.get("rolNombre");
        final String tipo = parametros.get("tipo");
        final String estado = parametros.get("estado");

        if (!isBlanck(id) && (id.length() > 15)) {
            throw new Exception("La longitud del id no debe ser mayor a 15.");
            //errors.rejectValue("id", "field.id", "La longitud del id no debe ser mayor a 15.");
            //return;
        }

        if (!isBlanck(nombreCompleto) && (nombreCompleto.length() > 50)) {
            throw new Exception("La longitud del nombre usuario no debe ser mayor a 50.");
            //errors.rejectValue("nombreCompleto", "field.nombreCompleto", "La longitud del nombre usuario no debe ser mayor a 50.");
            //return;
        }

        if (!isBlanck(nombreUsuario) && (nombreUsuario.length() > 100)) {
            throw new Exception("La longitud del nombre completo no debe ser mayor a 100.");
            //errors.rejectValue("nombreUsuario", "field.nombreUsuario", "La longitud del nombre usuario no debe ser mayor a 50.");
            //return;
        }

        if (!isBlanck(rolId) && (rolId.length() > 15)) {
            throw new Exception("La longitud del id rol id no debe ser mayor a 15.");
            //errors.rejectValue("rolId", "field.rolId", "La longitud del id rol id no debe ser mayor a 15.");
            //return;
        }

        if (!isBlanck(rolNombre) && (rolNombre.length() > 50)) {
            throw new Exception("La longitud de rolNombre no debe ser mayor a 50.");
            //errors.rejectValue("rolNombre", "field.rolNombre", "La longitud de rolNombre no debe ser mayor a 50.");
            //return;
        }

        if (!isBlanck(tipo) && (tipo.length() > 20)) {
            throw new Exception("La longitud del tipo no debe ser mayor a 20.");
            //errors.rejectValue("tipo", "field.tipo", "La longitud del tipo no debe ser mayor a 20.");
            //return;
        }

        if (!isBlanck(estado) && (estado.length() > 20)) {
            throw new Exception("La longitud del estado no debe ser mayor a 20.");
            //errors.rejectValue("estado", "field.estado", "La longitud del tipo no debe ser mayor a 20.");
            //return;
        }
    }

    public void validate(UsuarioRequest input, Long id, Errors errors) {
        if (isBlanck(input.getNombreUsuario())) {
            errors.rejectValue("nombreUsuario", "field.nombreUsuario", "El número de caracteres del nombre de usuario debe ser mayor a 0 y menor o igual a 50.");
            return;
        }

        if (!input.getNombreUsuario().matches(parametroService.getParametroByNombre(Parametro.DelSistema.EXPRESION_REGULAR_NOMBRE_USUARIO.name()).getValor())) {
            errors.rejectValue("nombreUsuario", "field.nombreUsuario", parametroService.getParametroByNombre(Parametro.DelSistema.MENSAJE_VALIDACION_NOMBRE_USUARIO.name()).getValor());
            return;
        }

        if (input.getRolId() == null) {
            errors.rejectValue("rolId", "field.rolId", "Seleccione un rol.");
            return;
        }

        //Boolean isAD = true;
        if (id != null) {
            Optional<Usuario> model = usuarioRepository.findById(id);
            if (!model.isPresent()) {
                errors.rejectValue("nombreUsuario", "field.nombreUsuario", "Identificador de usuario invalido.");
                return;
            } else {
                Usuario temp = usuarioRepository.findByNombreUsuarioAndEstadoIn(input.getNombreUsuario(), Arrays.asList(UsuarioEstado.HABILITADO, UsuarioEstado.BLOQUEADO));

                if (temp != null && !temp.getId().equals(model.get().getId())) {
                    errors.rejectValue("nombreUsuario", "field.nombreUsuario", "El nombre de usuario, se encuentra en uso.");
                    return;
                }

                //  OAC LDAP
                //if (temp != null && temp.getTipo() == UsuarioTipo.USUARIO_ACTIVE_DIRECTORY) {
                /*if (temp != null && temp.getTipo() == TipoAutenticacion.LDAP.getId()) {
                    isAD = true;
                }*/
            }
        } else {
            Usuario temp = usuarioRepository.findByNombreUsuarioAndEstadoIn(input.getNombreUsuario(), Arrays.asList(UsuarioEstado.HABILITADO, UsuarioEstado.BLOQUEADO));
            if (temp != null) {
                errors.rejectValue("nombreUsuario", "field.nombreUsuario", "El nombre de usuario, se encuentra en uso.");
                return;
            }

            /*int validacion = ((BigDecimal) parametroService.getParamVal(ParametroID.VALIDACION_ACTIVE_DIRECTORY)).intValue();
            if (validacion == TipoAutenticacion.LDAP.getId()) {
                isAD = true;
            }*/
        }

        /*if (isAD) {
            try {
                String name = new ActiveDirectory(parametroService).getNombreCompleto(input.getNombreUsuario().trim());
                if (name.trim().isEmpty()) {
                    errors.rejectValue("nombreUsuario", "field.nombreUsuario", "No se encontró el usuario en active directory con el valor de usuario ingresado.");
                    return;
                }
            } catch (LdapContextException e1) {
                //log.error("Error de conexion ldap " + e1.getMessage(), e1);
                errors.rejectValue("nombreUsuario", "filed.nombreUsuario", "Error de conexión con active directory, " + e1.getMessage());
                return;
            }
        }*/
        Optional<Rol> model = rolRepository.findById(input.getRolId());
        if (!model.isPresent()) {
            errors.rejectValue("rolId", "field.rolId", "Rol invalido, seleccione otro rol.");
        }
    }

    public void validate(Usuario model, CambioContrasenaRequest input, Errors errors) {
        if (isBlanck(input.getContrasenaAntigua())) {
            errors.rejectValue("contrasenaAntigua", "field.contrasenaAntigua", "La contraseña actual es requerido.");
            return;
        }

        if (input.getContrasenaAntigua().length() > 50) {
            errors.rejectValue("contrasenaAntigua", "field.contrasenaAntigua", "El número de caracteres de la contraseña actual debe ser mayor a 0 y menor o igual a 50.");
            return;
        }

        if (isBlanck(input.getContrasenaNueva())) {
            errors.rejectValue("contrasenaNueva", "field.contrasenaNueva", "La contraseña nueva es requerida.");
            return;
        }

        if (input.getContrasenaNueva().length() > 50) {
            errors.rejectValue("contrasenaNueva", "field.contrasenaNueva", "El número de caracteres de la contraseña nueva debe ser mayor a 0 y menor a 50.");
            return;
        }

        if (model == null) {
            errors.rejectValue("contrasenaNueva", "field.contrasenaNueva", "Usuario invalido.");
            return;
        }

        //  OAC LDAP
        //if (UsuarioTipo.SUPER_USUARIO != model.getTipo() && UsuarioTipo.USUARIO_NORMAL != model.getTipo()) {
        if (TipoAutenticacion.SUPER.getId() != model.getTipo() && TipoAutenticacion.LOCAL.getId() != model.getTipo()) {
            errors.rejectValue("contrasenaNueva", "field.contrasenaNueva", "Tipo de usuario no puede cambiar de contraseña.");
            return;
        }

        if (!passwordEncoder.matches(input.getContrasenaAntigua(), model.getContrasena())) {
            errors.rejectValue("contrasenaAntigua", "field.contrasenaAntigua", "Contraseña actual incorrecta.");
        }
    }

    public void validate(Usuario model, CambioContrasenaRequestLogin input, Errors errors) {
        if (isBlanck(input.getContrasenaAntigua())) {
            errors.rejectValue("contrasenaAntigua", "field.contrasenaAntigua", "La longitud de la contraseña debe ser mayor a 0 y menor a 50.");
        }

        if (isBlanck(input.getContrasenaNueva())) {
            errors.rejectValue("contrasenaNueva", "field.contrasenaNueva", "La longitud de la contraseña debe ser mayor a 0 y menor a 50.");
        }

        if (model == null) {
            errors.rejectValue("usuario", "field.usuario", "Usuario invalido.");
            return;
        }

        //  OAC LDAP
        //if (UsuarioTipo.SUPER_USUARIO != model.getTipo() && UsuarioTipo.USUARIO_NORMAL != model.getTipo()) {
        if (TipoAutenticacion.SUPER.getId() != model.getTipo() && TipoAutenticacion.LOCAL.getId() != model.getTipo()) {
            errors.rejectValue("tipo", "field.tipo", "Tipo de usuario no puede cambiar de contraseña.");
        }

    }

}
