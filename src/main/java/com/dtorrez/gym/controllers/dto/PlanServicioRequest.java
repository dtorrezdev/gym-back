package com.dtorrez.gym.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanServicioRequest implements Serializable {
    private Long planId;
    private ArrayList<Long> servicioIds;
}
