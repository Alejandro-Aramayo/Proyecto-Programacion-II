package com.LOTUS.jteducation.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Solo muestra la página de login. El formulario se envía por POST a /login, y ese POST no llega a ningún
// controlador nuestro: lo procesa Spring Security (busca el usuario, compara la contraseña y crea la sesión).
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication autenticacion) {
        // Si ya inició sesión, no tiene sentido mostrarle el login otra vez
        boolean yaInicioSesion = autenticacion != null && autenticacion.isAuthenticated()
                && !(autenticacion instanceof AnonymousAuthenticationToken);
        return yaInicioSesion ? "redirect:/" : "login";
    }
}
