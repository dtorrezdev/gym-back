package bo.com.micrium.modulobase.security.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JwtAccessDeneiedController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // Nombre del archivo HTML en templates
    }            
}
