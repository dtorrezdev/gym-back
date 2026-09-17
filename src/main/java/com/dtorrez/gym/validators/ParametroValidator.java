/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dtorrez.gym.validators;

import bo.com.micrium.modulobase.commons.GlobalValidator;
import com.micrium.bd.access.jpa.models.Parametro;
import com.micrium.bd.access.jpa.repositories.IParametroRepository;
import com.micrium.bd.access.jpa.repositories.ITipoParametroRepository;
import com.dtorrez.gym.controllers.dto.ParametroRequest;
import com.dtorrez.gym.services.ParametroService;

import bo.com.micrium.modulobase.commons.ParametroTipo;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author alepaco.maton
 */
@Component
public class ParametroValidator extends GlobalValidator {

    @Autowired
    transient IParametroRepository repository;

    @Autowired
    transient ITipoParametroRepository tipoParametroRepository;

    @Autowired
    transient ParametroService parametroService;

    // private transient AESUtils aesutils;
    public void validate(ParametroRequest input, Long id, Errors errors) {       

        if (id != null) {
            Optional<Parametro> model = repository.findById(id);
            //Optional<Parametro> model = repository.findById(Integer.parseInt(descrp));
            if (!model.isPresent()) {
                errors.rejectValue("id", "field.invalido", "Identificador de usuario invalido.");
            } else {
                Parametro temp = repository.findByNombre(input.getNombre());

                if (temp != null && !temp.getId().equals(model.get().getId())) {
                    errors.rejectValue("nombre", "field.invalido", "Ya se encuentra en uso.");
                }
            }
        } else {
            Parametro temp = repository.findByNombre(input.getNombre());

            if (temp != null) {
                errors.rejectValue("nombre", "field.invalido", "Ya se encuentra en uso.");
            }
        }

        if (isBlanck(input.getNombre())) {
            errors.rejectValue("nombre", "field.nombre", "La longitud del nombre debe ser mayor a 0 y menor a 255.");
        }

        if (input.getValor() != null && input.getValor().length() > 4000) {
            errors.rejectValue("valor", "field.valor", "La longitud del nombre debe ser menor a 4000.");
        }
        // Descomentar cuando se tengas tipo Parametro cron quartz
        /*if (input.getNombre().contains("_CRON")) {
            //log.error("valido: " + org.quartz.CronExpression.isValidExpression(input.getValor()));
            if (!org.quartz.CronExpression.isValidExpression(input.getValor())) {
                errors.rejectValue("valor", "field.valor", "Expresion Cron Quartz incorrecta.");
                //log.error("No se pudo desencriptar en Parametrovalidator: ");
            }
        }*/

        if (!ParametroTipo.esValido(input.getTipo())) {
            errors.rejectValue("tipo", "field.tipo", "Tipo de valor invalido.");
        }

        if (errors.hasErrors()) {
            return;
        }

        if (!input.getNombre().matches(parametroService.getParametroByNombre(com.micrium.bd.access.enuns.Parametro.DelSistema.EXPRESION_REGULAR_GENERAL.name()).getValor())) {
            errors.rejectValue("nombre", "field.nombre", parametroService.getParametroByNombre(com.micrium.bd.access.enuns.Parametro.DelSistema.MENSAJE_VALIDACION_GENERAL.name()).getValor());
        }

        if (!tipoParametroRepository.findById(input.getTipoParametroId()).isPresent()) {
            errors.rejectValue("tipoParametroId", "field.tipoParametroId", "Tipo de parametro invalido.");
        }
    }

    /*public static void main(String[] args) {
        String expresion = "0 0/10 Nan * * ? *";
        if (org.quartz.CronExpression.isValidExpression(expresion)) {
            System.out.println("expresion: " + expresion);
        } else {
            System.out.println("no expresion");
        }
    }*/

}
