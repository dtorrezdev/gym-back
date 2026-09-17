package com.dtorrez.gym.common.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 *
 * @author alepaco.maton
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@AllArgsConstructor
public class ExceptionResponse implements Serializable {

    private static final long serialVersionUID = 1321060619595537832L;

    private HttpStatus estatus;
    private String mensaje;
    private List<String> errores;

    public ExceptionResponse(HttpStatus estatus, String mensaje, String errores) {
        this.estatus = estatus;
        this.mensaje = mensaje;
        this.errores = Arrays.asList(errores);
    }
}
