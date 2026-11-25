package com.LOTUS.jteducation.controller;

import com.LOTUS.jteducation.entity.Alumno;
import com.LOTUS.jteducation.exception.RecursoNoEncontradoException;
import com.LOTUS.jteducation.security.UsuarioAutenticado;
import com.LOTUS.jteducation.service.AlumnoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final AlumnoService alumnoService;

    // @AuthenticationPrincipal: Spring Security entrega el usuario que inició sesión (lo guarda en la sesión)
    @GetMapping("/")
    public String index(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        // El alumno no tiene un panel de gestión: su página de inicio es su propia ficha
        if (!usuario.esAdmin()) {
            if (usuario.getIdAlumno() == null) {
                throw new RecursoNoEncontradoException("Tu cuenta no tiene una ficha de alumno asociada.");
            }
            return "redirect:/alumnos/" + usuario.getIdAlumno();
        }
        // Primera página (0) con los 6 últimos alumnos inscriptos; getContent() extrae la lista del Page
        List<Alumno> ultimosAlumnos = alumnoService.obtenerAlumnos(0, 6).getContent();
        model.addAttribute("alumnos", ultimosAlumnos);
        return "index";
    }
}
