package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse implements Serializable {

    private Long id;
    private String nombre;
    private String especialidad;
//    private String horario;
    private String estado;
    private Date dateCreated;
    private Date dateUpdated;

}
