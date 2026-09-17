package com.dtorrez.gym.validators;


import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.modulobase.commons.GlobalValidator;
import com.dtorrez.gym.controllers.dto.StaffRequest;
import com.dtorrez.gym.services.ParametroService;
import com.micrium.bd.access.enuns.Parametro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

@Component
public class StaffValidator extends GlobalValidator {

    @Autowired
    private ParametroService parametroService;

    public void validateListar(Map<String, String> parametros) throws ValidateException {

        // LoggerMain.info("***dtn validateListar " + parametros.size());
        final String nombre = parametros.get("nombre");
        final String especialidad = parametros.get("especialidad");
        final String estado = parametros.get("estado");

        if (!isBlanck(nombre) && (nombre.length() > 120)) {
            throw new ValidateException("La longitud del nombre no debe ser mayor a 120.");
        }

        if (!isBlanck(especialidad) && (especialidad.length() > 255)) {
            throw new ValidateException("La longitud del especialidad no debe ser mayor a 255.");
        }

        if (!isBlanck(estado) && (estado.length() > 10)) {
            throw new ValidateException("La longitud del estado no debe ser mayor a 10.");
        }
    }

    public void validate(StaffRequest input, Long id, Errors errors) {
        if (isBlanck(input.getNombre())) {
            errors.rejectValue("nombre", "field.nombre", "El nombre del Staff es requerido.");
            return;
        }

        if (input.getNombre().length() > 120) {
            errors.rejectValue("nombre", "field.nombre", "El número de caracteres del nombre debe ser mayor a 0 y menor o igual a 120.");
            return;
        }

        final String expresionRegularValidate = parametroService.getParametroByNombre(Parametro.DelSistema.EXPRESION_REGULAR_GENERAL.name()).getValor();
        final String mensajeValidacion = parametroService.getParametroByNombre(Parametro.DelSistema.MENSAJE_VALIDACION_GENERAL.name()).getValor();

        if (!input.getNombre().matches(expresionRegularValidate)) {
            errors.rejectValue("nombre", "field.nombre", mensajeValidacion);
            return;
        }

        if (input.getEspecialidad() != null &&  input.getEspecialidad().length() > 255) {
            errors.rejectValue("especialidad", "field.especialidad", "El número de caracteres de la especialidad debe ser mayor a 0 y menor o igual a 255.");
            return;
        }

//        if (input.getHorario() != null && input.getHorario().length() > 120) {
//            errors.rejectValue("horario", "field.horario", "El número de caracteres del horario debe ser mayor a 0 y menor o igual a 120.");
//            return;
//        }

        if (input.getEstado() != null) {
            errors.rejectValue("estado", "field.estado", "El número de caracteres de la estado debe ser entre 0 y 1.");
        }

    }
}
