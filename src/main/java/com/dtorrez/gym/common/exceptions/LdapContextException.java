package com.dtorrez.gym.common.exceptions;

/**
 * Esta Clase tiene como finalidad principal identificar y personalizar los
 * tipos de error que suceden cuando se realiza la conexion al servidor LDAP
 *
 * @author: Cesar Augusto Choque S
 * @version: 20-04-2015
 *
 * *
 */
public class LdapContextException extends Exception {
    
    private static final String DATA = "data";
    private static final String ERROR_NO_SUCH_USER = "525";
    private static final String ERROR_LOGON_FAILURE = "52e";
    private static final String ERROR_INVALID_LOGON_HOURS = "530";
    private static final String ERROR_INVALID_WORKSTATION = "531";
    private static final String ERROR_PASSWORD_EXPIRED = "532";
    private static final String ERROR_ACCOUNT_DISABLED = "533";
    private static final String ERROR_ACCOUNT_NOAUTORIZED_MACHINE = "534";
    private static final String ERROR_ACCOUNT_EXPIRED = "701";
    private static final String ERROR_PASSWORD_MUST_CHANGE = "773";
    private static final String ERROR_ACCOUNT_LOCKED_OUT = "775";

    private String mensaje;

    public LdapContextException(Throwable cause) {
        super(cause);
    }

    public LdapContextException(String message, Throwable cause) {
        super(message, cause);
        mensaje = message;
    }

    public LdapContextException(String message) {
        super(message);
        mensaje = message;
    }

    public LdapContextException() {
        mensaje = "";
    }

    @Override
    public String getMessage() {
        // LDAP: error code 49 - 80090308: LdapErr: DSID-0C0903A9, comment:
        // AcceptSecurityContext error, data 52e, v1db1
        String msg = mensaje;
        if (msg.contains(DATA)) {
            int beginIndex = msg.indexOf(DATA) + 5;
            int endIndex = beginIndex + 3;
            String code = msg.substring(beginIndex, endIndex);

            if (code.equals(ERROR_NO_SUCH_USER)) {
                return "El Nombre de Usuario No es Valido [Usuario no encontrado]";
            }

            if (code.equals(ERROR_LOGON_FAILURE)) {
                return "Nombre de Usuario Desconocido o Contraseña Incorrecta [Credenciales no validas]";
            }

            if (code.equals(ERROR_INVALID_LOGON_HOURS)) {
                return "El Usuario No Puede Iniaciar Sesion en Este Momento [Error de inicio, restricción de tiempo de inicio de sesión]";
            }

            if (code.equals(ERROR_INVALID_WORKSTATION)) {
                return "El Usuario No Puede Iniciar Sesion en Este Equipo..";
            }

            if (code.equals(ERROR_PASSWORD_EXPIRED)) {
                return "Su Contraseña ha Expirado [la contraseña de la cuenta especificada ha caducado]";
            }

            if (code.equals(ERROR_ACCOUNT_DISABLED)) {
                return "Cuenta de Usuario Desactivada [cuenta actualmente deshabilitada]";
            }

            if (code.equals(ERROR_ACCOUNT_NOAUTORIZED_MACHINE)) {
                return "El usuario No esta autorizado para el tipo de inicio de sesión solicitado en este equipo";
            }

            if (code.equals(ERROR_ACCOUNT_EXPIRED)) {
                return "Su Cuenta De Usuario esta Expirado [La cuenta del usuario ha caducado]";
            }

            if (code.equals(ERROR_PASSWORD_MUST_CHANGE)) {
                return "Antes de Iniciar Sesion Restablesca Su Contraseña [contraseña del usuario debe ser cambiado antes de iniciar sesión]";
            }

            if (code.equals(ERROR_ACCOUNT_LOCKED_OUT)) {
                return "Su Cuenta de Usuario Esta Bloqueada [La cuenta referenciada está bloqueada actualmente y no puede iniciar sesión]";
            }
        }
        return super.getMessage();
    }
}
