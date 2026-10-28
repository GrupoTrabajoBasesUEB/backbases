package co.edu.unbosque.backbases.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.backbases.dto.Rol;

class JwtServiceTest {

	private static final String SECRETO = "c2VjcmV0by1zb2xvLXBhcmEtcHJ1ZWJhcy1kZWwtYmFjay0yNTYtYml0cyEhIQ==";
	private static final String OTRO_SECRETO = "b3RybyBzZWNyZXRvIGRpc3RpbnRvIHBhcmEgZmlybWFyIHRva2VucyBmYWxzb3MhIQ==";

	@Test
	void elTokenConservaLosDatosDelVendedor() {
		JwtService jwt = new JwtService(SECRETO, 60_000);
		UsuarioAutenticado camila = new UsuarioAutenticado(3, "camila.rojas@ferreteria.demo", Rol.VENDEDOR, 1);

		assertThat(jwt.leer(jwt.generar(camila))).contains(camila);
	}

	@Test
	void elAdministradorNoLlevaSucursal() {
		JwtService jwt = new JwtService(SECRETO, 60_000);
		UsuarioAutenticado laura = new UsuarioAutenticado(1, "laura.gomez@ferreteria.demo", Rol.ADMINISTRADOR, null);

		assertThat(jwt.leer(jwt.generar(laura))).hasValueSatisfying(u -> assertThat(u.idSucursal()).isNull());
	}

	@Test
	void rechazaUnTokenFirmadoConOtraClave() {
		String falso = new JwtService(OTRO_SECRETO, 60_000).generar(new UsuarioAutenticado(1, "x@y.co", Rol.ADMINISTRADOR, null));

		assertThat(new JwtService(SECRETO, 60_000).leer(falso)).isEmpty();
	}

	@Test
	void rechazaUnTokenVencido() {
		JwtService vencido = new JwtService(SECRETO, -1_000);

		assertThat(vencido.leer(vencido.generar(new UsuarioAutenticado(1, "x@y.co", Rol.ADMINISTRADOR, null)))).isEmpty();
	}

	@Test
	void rechazaTextoQueNoEsToken() {
		assertThat(new JwtService(SECRETO, 60_000).leer("no-es-un-token")).isEmpty();
	}
}
