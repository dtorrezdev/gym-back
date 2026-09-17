/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
@AllArgsConstructor
@NoArgsConstructor
public class ParametroRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private short tipo;
    private String valor;
    private String descripcion;
    private Long tipoParametroId;

}
