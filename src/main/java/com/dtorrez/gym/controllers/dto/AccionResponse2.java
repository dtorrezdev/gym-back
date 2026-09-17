package com.dtorrez.gym.controllers.dto;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccionResponse2 implements Serializable  {

    private Long id;
    private int formularioId;
    private String nombre;
    private String url;
    private String metodo;

    /*@Override
    public int compareTo(AccionResponse2 o) {
        return Integer.compare(this.getOrden(), o.getOrden());
    }*/

}