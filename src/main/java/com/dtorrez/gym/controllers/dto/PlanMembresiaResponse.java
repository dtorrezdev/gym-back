package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanMembresiaResponse implements Serializable {

    private Long id;
    private Long membresiaId;
    private String fechaInicio;
    private String fechaFin;
    private Double monto;
    private Long planId;
    private String plan;
}
