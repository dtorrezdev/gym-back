package com.dtorrez.gym.common.exceptions;

import org.springframework.validation.BindingResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author alepaco.maton
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiException extends Exception {

    private BindingResult errors;
    
    public ApiException(String message) {
        super(message);
    }

    public ApiException(BindingResult errors, String message) {
        super(message);
        this.errors = errors;
    }
    
    public ApiException(String message, Throwable t) {
        super(message,t);        
    }
    
    public ApiException(BindingResult errors, String message, Throwable t) {
        super(message,t);
        this.errors = errors;
    }
    
}
