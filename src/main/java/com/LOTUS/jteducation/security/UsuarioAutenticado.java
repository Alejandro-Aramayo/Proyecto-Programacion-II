package com.LOTUS.jteducation.security;

import com.LOTUS.jteducation.entity.Rol;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * El usuario que inició sesión, tal como lo guarda Spring Security en la sesión.
 * Implementa UserDetails, la interfaz con la que Spring Security pide los datos de un usuario
 * (nombre de usuario, contraseña hasheada y permisos) sin conocer nuestra entidad Usuario.
 * Además guarda el id del alumno, para saber qué ficha puede ver un usuario con rol ALUMNO.
 */
@Getter
public class UsuarioAutenticado implements UserDetails {

    private final Long idUsuario;
    private final String email;
    private final String passwordHash;
    private final Rol rol;
    private final Long idAlumno; // null para el administrador, que no es un alumno

    public UsuarioAutenticado(Long idUsuario, String email, String passwordHash, Rol rol, Long idAlumno) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.idAlumno = idAlumno;
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    // Spring Security espera los roles con el prefijo "ROLE_": hasRole("ADMIN") busca "ROLE_ADMIN"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    // El email es el nombre de usuario con el que se inicia sesión
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }
}
