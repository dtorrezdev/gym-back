package com.dtorrez.gym.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormularioRequest2 implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String nombre;
    private Integer orden;
    private Long moduloId;
    //private Integer tipo;
    private String url;
    private String icono;
    //private List<AccionResponse> acciones;
}
