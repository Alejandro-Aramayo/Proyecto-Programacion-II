package com.LOTUS.jteducation.config;

import com.LOTUS.jteducation.security.UsuarioAutenticado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration: clase donde se declaran objetos que Spring tiene que crear y administrar.
// Acá está TODA la seguridad del sitio: quién puede entrar a cada página, cómo se inicia y se cierra
// la sesión, y con qué algoritmo se guardan las contraseñas.
@Configuration
public class SeguridadConfig {

    // @Bean: Spring crea UN solo encoder y lo inyecta en quien pida un PasswordEncoder.
    // Se devuelve la interfaz: los servicios no saben que es BCrypt, así se puede cambiar el algoritmo
    // tocando solo esta línea. Spring Security también lo usa para comprobar la contraseña en el login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // La cadena de filtros de Spring Security: cada petición pasa por acá ANTES de llegar al controlador.
    // Si el usuario no inició sesión, se lo manda al login; si no tiene el rol necesario, recibe un 403.
    @Bean
    public SecurityFilterChain filtrosDeSeguridad(HttpSecurity http) throws Exception {
        http
            // PERMISOS POR ROL. Las reglas se revisan en orden y gana la primera que coincide.
            .authorizeHttpRequests(permisos -> permisos
                // Páginas públicas: el login, los estilos, las imágenes y la página de error
                .requestMatchers("/login", "/css/**", "/images/**", "/error").permitAll()
                // Inicio: cualquier usuario con sesión (el alumno es redirigido a su ficha, ver IndexController)
                .requestMatchers(HttpMethod.GET, "/").authenticated()
                // "/alumnos/nuevo" va antes que "/alumnos/{id}", porque si no "nuevo" se tomaría como un id
                .requestMatchers("/alumnos/nuevo").hasRole("ADMIN")
                // Ficha de un alumno: el administrador ve cualquiera; el alumno, solo la suya
                .requestMatchers(HttpMethod.GET, "/alumnos/{id}").access((autenticacion, contexto) ->
                        new AuthorizationDecision(puedeVerFicha(autenticacion.get(), contexto.getVariables().get("id"))))
                // Todo lo demás (listados, altas, ediciones, bajas y carga de notas) es solo del administrador
                .anyRequest().hasRole("ADMIN"))

            // AUTENTICACIÓN con un formulario propio (templates/login.html)
            .formLogin(login -> login
                .loginPage("/login")                 // GET: nuestra página. El POST lo procesa Spring Security
                .usernameParameter("email")          // el campo del formulario se llama "email", no "username"
                .defaultSuccessUrl("/", true)        // después de entrar, siempre al inicio
                .failureUrl("/login?error")          // email o contraseña incorrectos
                .permitAll())

            // CIERRE DE SESIÓN: POST /logout invalida la sesión y borra la cookie que la identifica
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())

            // SESIÓN: si llega una sesión vencida (ver server.servlet.session.timeout), se pide entrar de nuevo
            .sessionManagement(sesion -> sesion
                .invalidSessionUrl("/login?expirada"));

        // La protección CSRF queda activada (es lo que viene por defecto): cada formulario POST lleva un token
        // oculto que Thymeleaf agrega solo en los <form th:action="...">. Así, otro sitio no puede enviar
        // formularios en nombre de un usuario que tiene la sesión abierta.
        return http.build();
    }

    // Regla de negocio de permisos: un alumno solo puede ver su propia ficha
    private static boolean puedeVerFicha(Authentication autenticacion, String idPedido) {
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            return false;
        }
        return usuario.esAdmin()
                || (usuario.getIdAlumno() != null && usuario.getIdAlumno().toString().equals(idPedido));
    }
}
