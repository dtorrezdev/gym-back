package com.dtorrez.gym.security.controllers.dto;

import java.util.List;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.dtorrez.gym.controllers.dto.ModuloResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
public class AutenticacionResponse implements Serializable {

    private final Long id;
    private final String token;
    private final Long rolId;
    private final String rol;
    private final List<ModuloResponse> modulos;
    private final String nombre;

    private final String inactivityTime;
    private final String timeoutBackend;
    private final String urlNoTimeoutBackend;
    private final String tipoAutenticacion;
    private final String fraseSecreta;

}
