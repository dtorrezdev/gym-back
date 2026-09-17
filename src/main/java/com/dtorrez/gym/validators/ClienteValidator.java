package com.dtorrez.gym.validators;

import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.modulobase.commons.GlobalValidator;
import com.dtorrez.gym.controllers.dto.ClienteRequest;
import com.dtorrez.gym.services.ParametroService;
import com.micrium.bd.access.enuns.Parametro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

@Component
public class ClienteValidator extends GlobalValidator {

    @Autowired
    ParametroService parametroService;

    public void validateListar(Map<String, String> parametros) throws ValidateException {

        // LoggerMain.info("***dtn validateListar " + parametros.size());
        final String ci = parametros.get("ci");
        final String nombre = parametros.get("nombre");
        final String genero = parametros.get("genero");
        final String telefono = parametros.get("telefono");
        final String direccion = parametros.get("direccion");

        if (!isBlanck(ci) && (ci.length() > 20)) {
            throw new ValidateException("La longitud del CI no debe ser mayor a 20.");
        }

        if (!isBlanck(nombre) && (nombre.length() > 120)) {
            throw new ValidateException("La longitud del NOMBRE no debe ser mayor a 120.");
        }

        if (!isBlanck(genero) && (genero.length() > 15)) {
            throw new ValidateException("La longitud del GENERO no debe ser mayor a 15.");
        }

        if (!isBlanck(telefono) && (telefono.length() > 10)) {
            throw new ValidateException("La longitud del TELEFONO no debe ser mayor a 10.");
        }

        if (!isBlanck(direccion) && (direccion.length() > 255)) {
            throw new ValidateException("La longitud del DIRECCION no debe ser mayor a 255.");
        }
    }

    public void validate(ClienteRequest input, Long id, Errors errors) {

        if (isBlanck(input.getCi())) {
            errors.rejectValue("ci", "field.ci", "El CI del cliente es requerido.");
            return;
        }

        if (isBlanck(input.getNombre())) {
            errors.rejectValue("nombre", "field.nombre", "El nombre del cliente es requerido.");
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

        if (isBlanck(input.getGenero())) {
            errors.rejectValue("genero", "field.genero", "El genero del cliente es requerido.");
            return;
        }

        if (input.getGenero().length() > 15) {
            errors.rejectValue("genero", "field.genero", "El número de caracteres del genero debe ser mayor a 0 y menor o igual a 15.");
            return;
        }

        if (input.getTelefono() != null && input.getTelefono().length() > 10) {
            errors.rejectValue("telefono", "field.telefono", "El número de caracteres del telefono debe ser mayor a 0 y menor o igual a 10.");
            return;
        }

        if (input.getDireccion() != null && input.getDireccion().length() > 255) {
            errors.rejectValue("direccion", "field.direccion", "El número de caracteres de la direccion debe ser mayor a 0 y menor o igual a 255.");
            return;
        }

    }
}
