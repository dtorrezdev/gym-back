package bo.com.micrium.modulobase.validators;

import bo.com.micrium.exception.ValidateException;
import bo.com.micrium.modulobase.commons.GlobalValidator;
import bo.com.micrium.modulobase.controllers.dto.MembresiaRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

@Component
public class MembresiaValidator extends GlobalValidator {

    public void validateListar(Map<String, String> parametros) throws ValidateException {

        final String descripcion = parametros.get("descripcion");
        final String tipoPago = parametros.get("tipo_pago");
        final String montoTotal = parametros.get("monto_total");
        final String estado = parametros.get("estado");
        final String clienteId = parametros.get("cliente_id");
        final String usuarioId = parametros.get("usuario_id");

        if (!isBlanck(descripcion) && (descripcion.length() > 255)) {
            throw new ValidateException("La longitud del descripcion no debe ser mayor a 255.");
        }

        if (!isBlanck(tipoPago) && (tipoPago.length() > 50)) {
            throw new ValidateException("La longitud del tipoPago no debe ser mayor a 50.");
        }

        if (!isBlanck(montoTotal) && (montoTotal.length() > 10)) {
            throw new ValidateException("La longitud del montoTotal no debe ser mayor a 10.");
        }

        if (!isBlanck(estado) && (estado.length() > 6)) {
            throw new ValidateException("La longitud del direccion no debe ser mayor a 6.");
        }
    }

    public void validate(MembresiaRequest input, Long id, Errors errors) {

        if (input.getDescripcion().length() > 255) {
            errors.rejectValue("descripcion", "field.descripcion", "El número de caracteres de la descripcion debe ser mayor a 0 y menor o igual a 255.");
            return;
        }

        if (input.getTipoPago() != null && input.getTipoPago().length() > 50) {
            errors.rejectValue("tipo_pago", "field.tipo_pago", "El número de caracteres del TipoPago debe ser mayor a 0 y menor o igual a 50.");
            return;
        }
    }


}
