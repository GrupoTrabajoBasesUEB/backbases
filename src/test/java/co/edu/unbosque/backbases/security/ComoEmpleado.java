package co.edu.unbosque.backbases.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import co.edu.unbosque.backbases.dto.Rol;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = ComoEmpleado.Fabrica.class)
public @interface ComoEmpleado {

	int id() default 1;

	Rol rol() default Rol.ADMINISTRADOR;

	int idSucursal() default 0;

	class Fabrica implements WithSecurityContextFactory<ComoEmpleado> {

		@Override
		public SecurityContext createSecurityContext(ComoEmpleado anotacion) {
			UsuarioAutenticado usuario = new UsuarioAutenticado(anotacion.id(), "prueba@ferreteria.demo", anotacion.rol(),
					anotacion.idSucursal() == 0 ? null : anotacion.idSucursal());
			SecurityContext contexto = SecurityContextHolder.createEmptyContext();
			contexto.setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null,
					List.of(new SimpleGrantedAuthority("ROLE_" + anotacion.rol().name()))));
			return contexto;
		}
	}
}
