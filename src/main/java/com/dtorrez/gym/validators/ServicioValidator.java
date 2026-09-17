package com.dtorrez.gym.validators;

import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.modulobase.commons.GlobalValidator;
import com.dtorrez.gym.controllers.dto.ServicioRequest;
import com.dtorrez.gym.services.ParametroService;
import com.micrium.bd.access.enuns.Parametro;
import com.micrium.bd.access.jpa.models.Staff;
import com.micrium.bd.access.jpa.repositories.IStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;
import java.util.Optional;

@Component
public class ServicioValidator extends GlobalValidator {

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private IStaffRepository repositoryStaff;

    public void validateListar(Map<String, String> parametros) throws ValidateException {

        final String nombre = parametros.get("nombre");
        final String descripcion = parametros.get("descripcion");
        final String tipo = parametros.get("tipo");
        final String horario = parametros.get("horario");
        final String cupos = parametros.get("cupos");
        final String staffId = parametros.get("staff_id");

        if (!isBlanck(nombre) && (nombre.length() > 120)) {
            throw new ValidateException("La longitud del nombre no debe ser mayor a 120.");
        }

        if (!isBlanck(descripcion) && (descripcion.length() > 255)) {
            throw new ValidateException("La longitud del descripcion no debe ser mayor a 255.");
        }

        if (!isBlanck(tipo) && (tipo.length() > 50)) {
            throw new ValidateException("La longitud del tipo no debe ser mayor a 50.");
        }

        if (!isBlanck(horario) && (horario.length() > 120)) {
            throw new ValidateException("La longitud del horario no debe ser mayor a 120.");
        }

        if (!isBlanck(cupos) && (cupos.length() > 12)) {
            throw new ValidateException("La longitud del cupos no debe ser mayor a 120.");
        }

    }

    public void validate(ServicioRequest input, Long id, Errors errors) {

        if (isBlanck(input.getNombre())) {
            errors.rejectValue("nombre", "field.nombre", "El nombre del servicio es requerido.");
            return;
        }

        if (input.getNombre().length() > 120) {
            errors.rejectValue("nombre", "field.nombre", "El número de caracteres del nombre debe ser mayor a 0 y menor o igual a 120.");
            return;
        }

        if (!input.getNombre().matches(parametroService.getParametroByNombre(Parametro.DelSistema.EXPRESION_REGULAR_GENERAL.name()).getValor())) {
            errors.rejectValue("nombre", "field.nombre", parametroService.getParametroByNombre(Parametro.DelSistema.MENSAJE_VALIDACION_GENERAL.name()).getValor());
            return;
        }


        if (input.getDescripcion() != null && input.getDescripcion().length() > 255) {
            errors.rejectValue("descripcion", "field.descripcion", "El número de caracteres de la descripcion debe ser mayor a 0 y menor o igual a 255.");
            return;
        }

        if (input.getTipoServicio() != null && input.getTipoServicio().length() > 50) {
            errors.rejectValue("tipo", "field.tipo", "El número de caracteres del tipo debe ser mayor a 0 y menor o igual a 50.");
            return;
        }

        if (input.getHorario() != null && input.getHorario().length() > 120) {
            errors.rejectValue("horario", "field.horario", "El número de caracteres de la horario debe ser mayor a 0 y menor o igual a 120.");
        }

        if (input.getEstado() != null && input.getEstado() > 1 ) {
            errors.rejectValue("estado", "field.estado", "El número de caracteres de estado debe ser entre 0 y 1.");
        }

        if (input.getStaffId() != null) {
            final Optional<Staff> byId = repositoryStaff.findById(input.getStaffId());
            if (!byId.isPresent()) {
                errors.rejectValue("staffId", "field.staffId", "El staffId no existe.");
            }
        }

    }

}
