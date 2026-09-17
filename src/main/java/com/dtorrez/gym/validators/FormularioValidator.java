package com.dtorrez.gym.validators;

import bo.com.micrium.modulobase.commons.GlobalValidator;

// import com.micrium.bd.access.jpa.models.Formulario;
import com.micrium.bd.access.jpa.repositories.IFormularioRepository;
import com.dtorrez.gym.controllers.dto.FormularioRequest2;
import com.dtorrez.gym.services.ParametroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author alepaco.maton
 */
@Component
public class FormularioValidator extends GlobalValidator {

    @Autowired
    IFormularioRepository repository;

    @Autowired
    ParametroService parametroService;

    public void validate(FormularioRequest2 input, Long id, Errors errors) {
        /* if (isBlanck(input.getNombre())) {
            errors.rejectValue("nombre", "field.nombre", "El nombre del formulario es requerido.");
            return;
        }

        if (input.getNombre().length() > 50) {
            errors.rejectValue("nombre", "field.nombre", "El número de caracteres del nombre debe ser mayor a 0 y menor o igual a 50.");
            return;
        }

        if (!input.getNombre().matches(parametroService.getParametroByNombre(Parametro.DelSistema.EXPRESION_REGULAR_GENERAL.name()).getValor())) {
            errors.rejectValue("nombre", "field.nombre", parametroService.getParametroByNombre(Parametro.DelSistema.MENSAJE_VALIDACION_GENERAL.name()).getValor());
            return;
        }

        if (input.getOrden() != null && input.getOrden().length() > 200) {
            errors.rejectValue("descripcion", "field.descripcion", "El número de caracteres de la descripción debe ser mayor a 0 y menor o igual a 200.");
            return;
        }

        if (id != null) {
            Optional<Formulario> model = repository.findById(id);

            if (!model.isPresent()) {
                errors.rejectValue("id", "field.invalido", "Identificador de usuario invalido.");
            } else {
                Formulario temp = repository.findByNombreAndEstadoTrue(input.getNombre());

                if (temp != null && !temp.getId().equals(model.get().getId())) {
                    errors.rejectValue("nombre", "field.invalido", "El nombre del formulario, se encuentra en uso.");
                }
            }
        } else {
            Formulario temp = repository.findByNombreAndEstadoTrue(input.getNombre());

            if (temp != null) {
                errors.rejectValue("nombre", "field.invalido", "El nombre del formulario, se encuentra en uso.");
            }
        }*/
    }

    public void validatePlus(FormularioRequest2[] input, Errors errors) {
       /*if (input.length == 0) {
            errors.rejectValue(null, "field.cargaMasivaFail", "No se envio ningun registro.");
            return;
        }

        List<Formulario> formularios = repository.findAll();

        int line = 2;
        List<String> formularioNull = new ArrayList<>();
        List<String> nombreRepetidos = new ArrayList<>();
        for (FormularioRequest formulario : input) {
            if (isBlanck(formulario.getNombre())) {
                formularioNull.add("" + line);
            }

            Optional<Formulario> find = formularios.stream().filter(f -> f.getNombre().equals(formulario.getNombre())).findFirst();
            if (find.isPresent()) {
                nombreRepetidos.add("El nombre " + formulario.getNombre() + " se repite en la fila: " + +line);
            }

            line++;
        }

        if (!formularioNull.isEmpty()) {
            errors.rejectValue(null, "field.formulariolNull", "El nombre es requerido en las filas: " + formularioNull);
        }

        if (!nombreRepetidos.isEmpty()) {
            errors.rejectValue(null, "field.nombreRepetidos", "Los nombres mencionados se encuentra en uso: " + nombreRepetidos);
        }*/
    }

}
