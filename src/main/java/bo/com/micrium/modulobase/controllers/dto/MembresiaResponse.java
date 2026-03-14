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
public class MembresiaResponse implements Serializable {
    private Long id;
    private String descripcion;
    private String tipoPago;
    private Double montoTotal;
    private String estado;
    private Long clienteId;
    private String cliente;
    private Long usuarioId;
    private List<PlanMembresiaResponse> planes;
    private Date dateCreated;
    private Date dateUpdated;

}
