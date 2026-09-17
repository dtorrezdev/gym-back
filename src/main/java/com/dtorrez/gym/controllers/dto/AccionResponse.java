package com.dtorrez.gym.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
public class AccionResponse implements Serializable {
    private Long id;
    private String nombre;
    private int tipo;
}
