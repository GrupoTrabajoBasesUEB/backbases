package co.edu.unbosque.backbases.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import co.edu.unbosque.backbases.dto.Rol;

class JwtServiceTest {

	private static final String SECRETO = "c2VjcmV0by1zb2xvLXBhcmEtcHJ1ZWJhcy1kZWwtYmFjay0yNTYtYml0cyEhIQ==";
	private static final String OTRO_SECRETO = "b3RybyBzZWNyZXRvIGRpc3RpbnRvIHBhcmEgZmlybWFyIHRva2VucyBmYWxzb3MhIQ==";

	@Test
	void elTokenConservaLosDatosDelVendedor() {
		JwtService jwt = new JwtService(SECRETO, 60_000);
		UsuarioAutenticado camila = new UsuarioAutenticado(3, "camila.rojas@ferreteria.demo", Rol.VENDEDOR, 1, null);

		assertThat(jwt.leer(jwt.generar(camila))).contains(camila);
	}

	@Test
	void elAdministradorNoLlevaSucursal() {
		JwtService jwt = new JwtService(SECRETO, 60_000);
		UsuarioAutenticado laura = new UsuarioAutenticado(1, "laura.gomez@ferreteria.demo", Rol.ADMINISTRADOR, null, 3);

		assertThat(jwt.leer(jwt.generar(laura))).hasValueSatisfying(u -> assertThat(u.idSucursal()).isNull());
	}

	@Test
	void elAdministradorLlevaSuNivelYLosPermisosSonAcumulativos() {
		JwtService jwt = new JwtService(SECRETO, 60_000);
		UsuarioAutenticado andres = new UsuarioAutenticado(2, "andres.pardo@ferreteria.demo", Rol.ADMINISTRADOR, null, 2);

		assertThat(jwt.leer(jwt.generar(andres))).hasValueSatisfying(u -> {
			assertThat(u.nivelAcceso()).isEqualTo(2);
			assertThat(u.permisos()).extracting(GrantedAuthority::getAuthority)
					.containsExactly("ROLE_ADMINISTRADOR", "NIVEL_1", "NIVEL_2");
		});
	}

	@Test
	void elVendedorNoTieneNiveles() {
		UsuarioAutenticado camila = new UsuarioAutenticado(3, "camila.rojas@ferreteria.demo", Rol.VENDEDOR, 1, null);

		assertThat(camila.permisos()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_VENDEDOR");
	}

	@Test
	void rechazaUnTokenDeAdministradorSinNivel() {
		JwtService jwt = new JwtService(SECRETO, 60_000);

		assertThat(jwt.leer(jwt.generar(new UsuarioAutenticado(1, "x@y.co", Rol.ADMINISTRADOR, null, null)))).isEmpty();
	}

	@Test
	void rechazaUnTokenFirmadoConOtraClave() {
		String falso = new JwtService(OTRO_SECRETO, 60_000).generar(new UsuarioAutenticado(1, "x@y.co", Rol.ADMINISTRADOR, null, 3));

		assertThat(new JwtService(SECRETO, 60_000).leer(falso)).isEmpty();
	}

	@Test
	void rechazaUnTokenVencido() {
		JwtService vencido = new JwtService(SECRETO, -1_000);

		assertThat(vencido.leer(vencido.generar(new UsuarioAutenticado(1, "x@y.co", Rol.ADMINISTRADOR, null, 3)))).isEmpty();
	}

	@Test
	void rechazaTextoQueNoEsToken() {
		assertThat(new JwtService(SECRETO, 60_000).leer("no-es-un-token")).isEmpty();
	}
}
