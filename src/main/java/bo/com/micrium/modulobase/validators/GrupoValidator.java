/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.com.micrium.modulobase.validators;

import bo.com.micrium.modulobase.commons.GlobalValidator;
import bo.com.micrium.modulobase.commons.GrupoEstado;
import bo.com.micrium.modulobase.security.services.ActiveDirectoryService;
import bo.com.micrium.modulobase.common.exceptions.LdapContextException;

import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Grupo;
import com.micrium.bd.access.jpa.repositories.IGrupoRepository;
import bo.com.micrium.modulobase.controllers.dto.GrupoRequest;
import bo.com.micrium.modulobase.services.ParametroService;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author alepaco.maton
 */
@Component
public class GrupoValidator extends GlobalValidator {

    @Autowired
    IGrupoRepository repository;

    @Autowired
    ParametroService parametroService;

    public void validate(GrupoRequest input, Long id, Errors errors) {
        if (isBlanck(input.getNombre()) || input.getNombre().length() > 255) {
            errors.rejectValue("nombre", "field.nombre", "La longitud del nombre debe ser mayor a 0 y menor a 255.");
            return;
        }
        
        if (!input.getNombre().matches(parametroService.getParametroByNombre(Parametro.DelSistema.EXPRESION_REGULAR_GENERAL.name()).getValor())) {
            errors.rejectValue("nombre", "field.nombre", parametroService.getParametroByNombre(Parametro.DelSistema.MENSAJE_VALIDACION_GENERAL.name()).getValor());
            return;
        }
        
        if (input.getRolId() == null) {
            errors.rejectValue("rolId", "field.rolId", "Seleccione un rol.");
            return;
        }
        
        /*log.error("AAA: id: " + id);
        {
            Optional<Rol> model = rolRepository.findById(input.getRolId());
            if (!model.isPresent()) {
                errors.rejectValue("rolId", "field.rolId", "Seleccione un rol.");
            }
        }*/

        if (id != null) {
            Optional<Grupo> model = repository.findById(id);
            if (!model.isPresent()) {
                errors.rejectValue("id", "field.invalido", "Identificador de usuario invalido.");
            } else {
                Grupo temp = repository.findByNombreAndEstado(input.getNombre(), GrupoEstado.HABILITADO);
                if (!temp.getRolId().equals(model.get().getRolId())) {
                    errors.rejectValue("nombre", "field.invalido", "El nombre del grupo, se encuentra en uso.");
                }
            }
        } else {
            Grupo temp = repository.findByNombreAndEstado(input.getNombre(), GrupoEstado.HABILITADO);
            if (temp != null) {
                errors.rejectValue("nombre", "field.invalido", "El nombre del grupo, se encuentra en uso.");
            }
        }
        if (errors.hasErrors()) {
            return;
        }
        try {
            if (!(new ActiveDirectoryService(parametroService).validarGrupo(input.getNombre().trim()))) {
                errors.rejectValue("nombre", "field.nombre", "No se encontró el grupo en active directory.");
            }
        } catch (LdapContextException e1) {
            //log.error("Error de conexion ldap " + e1.getMessage(), e1);
            errors.rejectValue("nombre", "ldap.nombre", "Error de conexión con active directory, " + e1.getMessage());
        }
    }

}
