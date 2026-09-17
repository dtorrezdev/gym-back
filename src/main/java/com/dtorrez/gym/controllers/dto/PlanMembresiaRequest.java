package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanMembresiaRequest implements Serializable {
    private String fechaInicio;
    private String fechaFin;
    private Double monto;
    private Long planId;
}
