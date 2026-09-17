package com.dtorrez.gym.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STRestartNumber;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse implements Serializable {

    private Long id;
    private String ci;
    private String nombre;
    private String genero;
    private String telefono;
    private String direccion;
    private Date dateCreated;
    private Date dateUpdated;

}
