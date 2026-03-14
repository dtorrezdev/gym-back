package bo.com.micrium.modulobase.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest implements Serializable {

    private String ci;
    private String nombre;
    private String genero;
    private String telefono;
    private String direccion;
}
