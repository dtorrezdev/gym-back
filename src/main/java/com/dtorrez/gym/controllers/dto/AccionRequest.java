package com.dtorrez.gym.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccionRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Long formularioId;
    private String nombre;
    private String url;
    private String metodo;
}
