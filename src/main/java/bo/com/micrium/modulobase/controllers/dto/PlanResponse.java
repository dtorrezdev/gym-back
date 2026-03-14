package bo.com.micrium.modulobase.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse implements Serializable {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String duracion;
    private String estado;
    private List<Long> servicios;
    private Date dateCreated;
    private Date dateUpdated;
}
