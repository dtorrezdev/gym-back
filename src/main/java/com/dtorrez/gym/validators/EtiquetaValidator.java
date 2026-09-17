/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.validators;

import java.util.Map;
import java.util.Optional;

import com.micrium.bd.access.jpa.models.Etiqueta;
import com.micrium.bd.access.jpa.repositories.IEtiquetaRepository;
import com.dtorrez.gym.controllers.dto.EtiquetaRequest;
import bo.com.micrium.modulobase.commons.GlobalValidator;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author alepaco.maton
 */
@Component
public class EtiquetaValidator extends GlobalValidator {

    @Autowired
    IEtiquetaRepository repository;

    public void validateListar(Map<String, String> parametros) throws Exception {
        final String llave = parametros.get("llave");
        final String grupo = parametros.get("grupo");
        final String valor = parametros.get("valor");        

        if (!isBlanck(llave) && (llave.length() > 255)) {
            throw new Exception("La longitud del llave no debe ser mayor a 255.");
        }

        if (!isBlanck(grupo) && (grupo.length() > 255)) {
            throw new Exception("La longitud del grupo no debe ser mayor a 255.");
        }

        if (!isBlanck(valor) && (valor.length() > 255)) {
            throw new Exception("La longitud del valor no debe ser mayor a 255.");
        }
    }

    public void validate(EtiquetaRequest input, Long id, Errors errors) {        

        // if (isBlanck(input.getLlave())) {
        //     errors.rejectValue("llave", "field.llave", "La llave es requerido.");
        //     return;
        // }

        // if (isBlanck(input.getGrupo())) {
        //     errors.rejectValue("grupo", "field.grupo", "La grupo es requerido.");
        //     return;
        // }

        if (isBlanck(input.getValor())) {
            errors.rejectValue("valor", "field.valor", "La valor es requerido.");
            return;
        }

        if( id != null ) {
            Optional<Etiqueta> byId = repository.findById(id);
            if (!byId.isPresent()) {
                errors.rejectValue("id", "field.id", "Identificador de Etiqueta invalido.");
                return;
            }
        }
    }

}
