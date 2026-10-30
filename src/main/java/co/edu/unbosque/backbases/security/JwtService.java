package co.edu.unbosque.backbases.security;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.unbosque.backbases.dto.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final String CLAIM_CORREO = "correo";
	private static final String CLAIM_ROL = "rol";
	private static final String CLAIM_SUCURSAL = "idSucursal";
	private static final String CLAIM_NIVEL = "nivelAcceso";

	private final SecretKey clave;
	private final long vigenciaMs;

	public JwtService(@Value("${jwt.secret}") String secreto, @Value("${jwt.expiration-ms}") long vigenciaMs) {
		this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
		this.vigenciaMs = vigenciaMs;
	}

	public String generar(UsuarioAutenticado usuario) {
		Instant ahora = Instant.now();
		var token = Jwts.builder()
				.subject(String.valueOf(usuario.idEmpleado()))
				.claim(CLAIM_CORREO, usuario.correo())
				.claim(CLAIM_ROL, usuario.rol().name())
				.issuedAt(Date.from(ahora))
				.expiration(Date.from(ahora.plusMillis(vigenciaMs)));
		if (usuario.idSucursal() != null) {
			token.claim(CLAIM_SUCURSAL, usuario.idSucursal());
		}
		if (usuario.nivelAcceso() != null) {
			token.claim(CLAIM_NIVEL, usuario.nivelAcceso());
		}
		return token.signWith(clave).compact();
	}

	public Optional<UsuarioAutenticado> leer(String token) {
		try {
			Claims datos = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
			Rol rol = Rol.valueOf(datos.get(CLAIM_ROL, String.class));
			Integer nivelAcceso = datos.get(CLAIM_NIVEL, Integer.class);
			if (rol == Rol.ADMINISTRADOR && nivelAcceso == null) {
				return Optional.empty();
			}
			return Optional.of(new UsuarioAutenticado(Integer.valueOf(datos.getSubject()), datos.get(CLAIM_CORREO, String.class),
					rol, datos.get(CLAIM_SUCURSAL, Integer.class), nivelAcceso));
		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public long getVigenciaMs() {
		return vigenciaMs;
	}
}
