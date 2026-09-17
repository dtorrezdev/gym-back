package com.dtorrez.gym.controllers.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
public class ModuloResponse implements Serializable, Comparable<ModuloResponse> {

    private Long id;
    private String nombre;
    private int orden;
    private int tipo;
    private String url;
    private String icono;
    private List<FormularioResponse> formularios = new ArrayList<>();

    public void sortFormularios() {
        Collections.sort(formularios);
    }

    public void removeNotVisible(Map<Long, Boolean> visible) {                
        for (int i = this.formularios.size() - 1; i >= 0; i--) {            
            if (!visible.get(formularios.get(i).getId())) {
                formularios.remove(i);
            }
        }
    }

    @Override
    public int compareTo(ModuloResponse o) {
        return Long.compare(this.getId(), o.getId());
    }


}
