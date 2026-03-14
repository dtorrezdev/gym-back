package bo.com.micrium.modulobase.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffRequest implements Serializable {

    private String nombre;
    private String especialidad;
//    private String horario;
    private String estado;

}
