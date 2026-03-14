package bo.com.micrium.modulobase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"bo.com.micrium.modulobase", "com.micrium.bd.access"})
public class ModulobaseApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ModulobaseApplication.class, args);
	}

}
