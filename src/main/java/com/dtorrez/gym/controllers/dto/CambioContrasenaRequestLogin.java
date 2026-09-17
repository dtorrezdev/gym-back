package com.dtorrez.gym.controllers.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioContrasenaRequestLogin implements Serializable {

    private static final long serialVersionUID = 4497165945877138919L;

    private String userName;
    private String contrasenaAntigua;
    private String contrasenaNueva;
}
