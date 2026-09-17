package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembresiaRequest implements Serializable {

    private String descripcion;
    private String tipoPago;
    private Double montoTotal;
    private String estado;
    private Long clienteId;
    private Long usuarioId;
    private List<PlanMembresiaRequest> planes;
}
