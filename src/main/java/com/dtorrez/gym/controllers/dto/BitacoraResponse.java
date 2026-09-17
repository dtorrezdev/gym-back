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
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String accion;
    private String direccionIp;
    private String fecha;
    private String formulario;
    private String usuario;
    private String valorAnterior;
    private String valorNuevo;
    private Long logSistemaId;

}
