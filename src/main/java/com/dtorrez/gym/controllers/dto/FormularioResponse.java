package com.dtorrez.gym.controllers.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
public class FormularioResponse implements Serializable, Comparable<FormularioResponse> {

    private Long id;
    private String nombre;
    private int orden;
    private int tipo;
    private String url;
    private String icono;
    private List<AccionResponse> acciones;

    @Override
    public int compareTo(FormularioResponse o) {
        return Integer.compare(this.getOrden(), o.getOrden());
    }

}
