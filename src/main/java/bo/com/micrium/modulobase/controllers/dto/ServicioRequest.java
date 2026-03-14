package bo.com.micrium.modulobase.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRequest implements Serializable {

    private String nombre;
    private String descripcion;

    private String tipoServicio;
    private String horario;
    private Short cupos;
    private Short estado;
    private Long staffId;

}
