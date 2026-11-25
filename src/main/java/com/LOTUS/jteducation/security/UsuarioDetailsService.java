package com.LOTUS.jteducation.security;

import com.LOTUS.jteducation.entity.Alumno;
import com.LOTUS.jteducation.entity.Usuario;
import com.LOTUS.jteducation.repository.AlumnoRepository;
import com.LOTUS.jteducation.repository.UsuarioRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Le dice a Spring Security cómo buscar un usuario en NUESTRA base de datos.
 * Al enviar el formulario de login, Spring Security llama a loadUserByUsername con el email escrito,
 * y después compara la contraseña escrita con el hash guardado usando el PasswordEncoder (BCrypt).
 * Nosotros nunca comparamos contraseñas a mano.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        // Los emails se guardan en minúsculas (ver AlumnoService), así que se busca igual
        String emailLimpio = (email == null) ? "" : email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmail(emailLimpio)
                // El mensaje no se muestra: el login dice siempre "Email o contraseña incorrectos",
                // para no revelar qué emails existen
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese email."));

        // Si es un alumno, se guarda el id de su ficha para limitar lo que puede ver
        Long idAlumno = alumnoRepository.findByUsuario_Id(usuario.getId())
                .map(Alumno::getIdAlumno)
                .orElse(null);

        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.getPasswordHash(),
                usuario.getRol(), idAlumno);
    }
}
