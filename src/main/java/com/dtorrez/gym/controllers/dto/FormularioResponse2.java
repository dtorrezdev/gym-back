package com.dtorrez.gym.controllers.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormularioResponse2 implements Serializable, Comparable<FormularioResponse2> {

    private Long id;
    private String nombre;
    private Integer orden;
    private Integer moduloId;
    private String url;
    private String icono;
   // private List<AccionResponse> acciones;

    @Override
    public int compareTo(FormularioResponse2 o) {
        return Integer.compare(this.getOrden(), o.getOrden());
    }

}
