package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest implements Serializable {
    private String nombre;
    private String descripcion;
    private Double precio;
    private String duracion;
    private String estado;
    private List<Long> servicios;
}
