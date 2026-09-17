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
import lombok.ToString;

/**
 *
 * @author alepaco.maton
 */
@ToString(exclude = {"contrasenaAntigua", "contrasenaNueva"})
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioContrasenaRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String contrasenaAntigua;
    private String contrasenaNueva;

}
