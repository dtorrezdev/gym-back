package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembresiaPlanRequest implements Serializable {
    private Long membresiaId;
    private List<PlanMembresiaRequest> planes;
}
