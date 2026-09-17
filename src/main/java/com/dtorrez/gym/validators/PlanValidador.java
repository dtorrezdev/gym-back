package com.dtorrez.gym.validators;

import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.modulobase.commons.GlobalValidator;
import com.dtorrez.gym.controllers.dto.PlanRequest;
import com.dtorrez.gym.services.ParametroService;
import com.micrium.bd.access.enuns.Parametro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

@Component
public class PlanValidador extends GlobalValidator {

    @Autowired
    private ParametroService parametroService;

    public void validateListar(Map<String, String> parametros) throws ValidateException {

        final String nombre = parametros.get("nombre");
        final String descripcion = parametros.get("descripcion");
        final String precio = parametros.get("precio");
        final String duracion = parametros.get("duracion");
        final String estado = parametros.get("estado");

        if (!isBlanck(nombre) && (nombre.length() > 120)) {
            throw new ValidateException("La longitud del nombre no debe ser mayor a 120.");
        }

        if (!isBlanck(descripcion) && (descripcion.length() > 255)) {
            throw new ValidateException("La longitud del descripcion no debe ser mayor a 255.");
        }

        if (!isBlanck(precio) && (precio.length() > 12)) {
            try {
                Double.parseDouble(precio);
            } catch ( NumberFormatException e) {
                throw new ValidateException("El formato del numero no valido.");
            }
            throw new ValidateException("La valor maximo del precio no debe ser mayor a 9999999999.99.");
        }

        if (!isBlanck(duracion) && (duracion.length() > 120)) {
            throw new ValidateException("La longitud de la duracion no debe ser mayor a 120.");
        }

        if (!isBlanck(estado) && (estado.length() > 1)) {
            throw new ValidateException("La valor del estado debe ser [0: Inactivo, 1: Activo]");
        }

    }

    public void validate(PlanRequest input, Long id, Errors errors) {
        if (isBlanck(input.getNombre())) {
            errors.rejectValue("nombre", "field.nombre", "El nombre del Plan es requerido.");
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

        if (input.getDescripcion().length() > 255) {
            errors.rejectValue("descripcion", "field.descripcion", "El número de caracteres del descripcion debe ser mayor a 0 y menor o igual a 255.");
            return;
        }

        if (input.getDuracion() != null && input.getDuracion().length() > 120) {
            errors.rejectValue("duracion", "field.duracion", "El número de caracteres del duracion debe ser mayor a 0 y menor o igual a 120.");
            return;
        }

//        if (input.getEstado() != null && input.getEstado() > 1) {
//            errors.rejectValue("estado", "field.estado", "El número de caracteres de la estado debe ser ente 0 y 1.");
//        }

    }

}
