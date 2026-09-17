package com.dtorrez.gym.security.controllers.dto;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author alepaco.maton
 */
@ToString(exclude = {"contrasena"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutenticacionRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    @NotNull(message = "Introdusca el nombre de usuario")
    @Size(min = 1,max = 50,message = "Nombre de usuario invalido, debe tener almenos 1 caracter y no mas de 50 caracteres.")
    private String nombreUsuario;
    @NotNull(message = "Introdusca la contraseña")
    @Size(min = 1,max = 100,message = "Contraseña invalida, debe ser tener almenos un caracter y no mas de 100 caracteres.")
    private String contrasena;
}
