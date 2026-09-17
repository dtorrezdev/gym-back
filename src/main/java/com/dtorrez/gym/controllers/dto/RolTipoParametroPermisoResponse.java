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
public class RolTipoParametroPermisoResponse implements Serializable {

    private Long id;
    private Long rolId;
    private Long tipoParametroId;
    private String tipoParametroNombre;
    private Integer tipoPermiso;

}
