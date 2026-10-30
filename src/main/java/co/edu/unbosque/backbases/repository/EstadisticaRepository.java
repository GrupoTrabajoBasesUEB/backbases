package co.edu.unbosque.backbases.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.backbases.dto.EstadisticasDTO.Agrupado;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.Comision;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.ProductoVendido;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.Resumen;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.SucursalDetalle;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.UnidadesInventario;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasDia;

@Repository
public class EstadisticaRepository {

	public record Filtro(LocalDate desde, LocalDate hasta, Integer idSucursal, Integer idEmpleado) {

		MapSqlParameterSource parametros() {
			return new MapSqlParameterSource()
					.addValue("desde", desde)
					.addValue("hasta", hasta)
					.addValue("idSucursal", idSucursal)
					.addValue("idEmpleado", idEmpleado);
		}
	}

	private static final String VENTAS_DEL_PERIODO = """
			v.FECHA BETWEEN :desde AND :hasta
			AND (:idSucursal IS NULL OR v.ID_SUCURSAL = :idSucursal)
			AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)
			""";

	private static final String CALENDARIO = """
			WITH RECURSIVE DIAS (DIA) AS (
			  SELECT CAST(:desde AS DATE)
			  UNION ALL
			  SELECT DIA + INTERVAL 1 DAY FROM DIAS WHERE DIA < :hasta
			)
			""";

	private final NamedParameterJdbcTemplate jdbc;

	public EstadisticaRepository(NamedParameterJdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public Resumen resumen(Filtro filtro) {
		return jdbc.queryForObject("""
				SELECT COUNT(DISTINCT v.ID_VENTA) AS CANTIDAD,
				       COALESCE(SUM(d.CANTIDAD * d.PRECIO_UNITARIO), 0) AS TOTAL,
				       COALESCE(SUM(d.CANTIDAD), 0) AS UNIDADES
				FROM VENTA v
				JOIN DETALLE_VENTA d ON d.ID_VENTA = v.ID_VENTA
				WHERE""" + " " + VENTAS_DEL_PERIODO, filtro.parametros(), (rs, i) -> {
					long cantidad = rs.getLong("CANTIDAD");
					BigDecimal total = rs.getBigDecimal("TOTAL");
					return new Resumen(total, cantidad, rs.getLong("UNIDADES"), promedio(total, cantidad));
				});
	}

	public List<VentasDia> ventasPorDia(Filtro filtro) {
		return jdbc.query(CALENDARIO + """
				SELECT DIAS.DIA AS FECHA, COALESCE(SUM(t.MONTO_TOTAL), 0) AS TOTAL, COUNT(v.ID_VENTA) AS CANTIDAD
				FROM DIAS
				LEFT JOIN VENTA v ON v.FECHA = DIAS.DIA
				  AND (:idSucursal IS NULL OR v.ID_SUCURSAL = :idSucursal)
				  AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)
				LEFT JOIN V_VENTA_TOTAL t ON t.ID_VENTA = v.ID_VENTA
				GROUP BY DIAS.DIA
				ORDER BY DIAS.DIA
				""", filtro.parametros(), (rs, i) -> new VentasDia(rs.getObject("FECHA", LocalDate.class), rs.getBigDecimal("TOTAL"), rs.getLong("CANTIDAD")));
	}

	public List<Agrupado> porMetodoPago(Filtro filtro) {
		return jdbc.query("""
				SELECT m.ID_METODO_PAGO AS ID, m.TIPO AS NOMBRE, COALESCE(SUM(t.MONTO_TOTAL), 0) AS TOTAL, COUNT(v.ID_VENTA) AS CANTIDAD
				FROM METODO_PAGO m
				LEFT JOIN VENTA v ON v.ID_METODO_PAGO = m.ID_METODO_PAGO
				  AND v.FECHA BETWEEN :desde AND :hasta
				  AND (:idSucursal IS NULL OR v.ID_SUCURSAL = :idSucursal)
				  AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)
				LEFT JOIN V_VENTA_TOTAL t ON t.ID_VENTA = v.ID_VENTA
				GROUP BY m.ID_METODO_PAGO, m.TIPO
				ORDER BY TOTAL DESC, NOMBRE
				""", filtro.parametros(), EstadisticaRepository::agrupado);
	}

	public List<ProductoVendido> rendimientoPorProducto(Filtro filtro) {
		return jdbc.query("""
				SELECT p.ID_PRODUCTO, p.CODIGO, p.NOMBRE_PRODUCTO,
				       COALESCE(x.UNIDADES, 0) AS UNIDADES,
				       COALESCE(x.TOTAL, 0) AS TOTAL,
				       COALESCE(x.TOTAL, 0) - COALESCE(x.UNIDADES, 0) * p.COSTO_COMPRA AS UTILIDAD
				FROM PRODUCTO p
				LEFT JOIN (
				  SELECT d.ID_PRODUCTO, SUM(d.CANTIDAD) AS UNIDADES, SUM(d.CANTIDAD * d.PRECIO_UNITARIO) AS TOTAL
				  FROM DETALLE_VENTA d
				  JOIN VENTA v ON v.ID_VENTA = d.ID_VENTA
				  WHERE""" + " " + VENTAS_DEL_PERIODO + """
				  GROUP BY d.ID_PRODUCTO
				) x ON x.ID_PRODUCTO = p.ID_PRODUCTO
				ORDER BY TOTAL DESC, p.NOMBRE_PRODUCTO
				""", filtro.parametros(), EstadisticaRepository::productoVendido);
	}

	public List<VentasDia> ventasDeProductoPorDia(Filtro filtro, Integer idProducto) {
		return jdbc.query(CALENDARIO + """
				SELECT DIAS.DIA AS FECHA, COALESCE(SUM(d.CANTIDAD * d.PRECIO_UNITARIO), 0) AS TOTAL, COALESCE(SUM(d.CANTIDAD), 0) AS CANTIDAD
				FROM DIAS
				LEFT JOIN VENTA v ON v.FECHA = DIAS.DIA
				  AND (:idSucursal IS NULL OR v.ID_SUCURSAL = :idSucursal)
				  AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)
				LEFT JOIN DETALLE_VENTA d ON d.ID_VENTA = v.ID_VENTA AND d.ID_PRODUCTO = :idProducto
				GROUP BY DIAS.DIA
				ORDER BY DIAS.DIA
				""", filtro.parametros().addValue("idProducto", idProducto),
				(rs, i) -> new VentasDia(rs.getObject("FECHA", LocalDate.class), rs.getBigDecimal("TOTAL"), rs.getLong("CANTIDAD")));
	}

	public List<UnidadesInventario> unidadesPorInventario(Filtro filtro) {
		return jdbc.query("""
				WITH VENDIDAS AS (
				  SELECT d.ID_PRODUCTO, v.ID_SUCURSAL, SUM(d.CANTIDAD) AS UNIDADES
				  FROM DETALLE_VENTA d
				  JOIN VENTA v ON v.ID_VENTA = d.ID_VENTA
				  WHERE""" + " " + VENTAS_DEL_PERIODO + """
				  GROUP BY d.ID_PRODUCTO, v.ID_SUCURSAL
				)
				SELECT i.ID_PRODUCTO, i.ID_SUCURSAL, COALESCE(u.UNIDADES, 0) AS UNIDADES
				FROM INVENTARIO i
				LEFT JOIN VENDIDAS u ON u.ID_PRODUCTO = i.ID_PRODUCTO AND u.ID_SUCURSAL = i.ID_SUCURSAL
				WHERE :idSucursal IS NULL OR i.ID_SUCURSAL = :idSucursal
				UNION ALL
				SELECT u.ID_PRODUCTO, u.ID_SUCURSAL, u.UNIDADES
				FROM VENDIDAS u
				WHERE NOT EXISTS (SELECT 1 FROM INVENTARIO i WHERE i.ID_PRODUCTO = u.ID_PRODUCTO AND i.ID_SUCURSAL = u.ID_SUCURSAL)
				ORDER BY ID_PRODUCTO, ID_SUCURSAL
				""", filtro.parametros(), (rs, i) -> new UnidadesInventario(rs.getInt("ID_PRODUCTO"), rs.getInt("ID_SUCURSAL"), rs.getLong("UNIDADES")));
	}

	public List<SucursalDetalle> sucursalesDetalle(Filtro filtro, int umbralStockBajo) {
		return jdbc.query("""
				SELECT s.ID_SUCURSAL, s.NOMBRE_SUCURSAL, s.CIUDAD,
				       (SELECT COUNT(*) FROM VENTA v
				         WHERE v.ID_SUCURSAL = s.ID_SUCURSAL AND v.FECHA BETWEEN :desde AND :hasta
				           AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)) AS CANTIDAD,
				       (SELECT COALESCE(SUM(d.CANTIDAD * d.PRECIO_UNITARIO), 0) FROM VENTA v JOIN DETALLE_VENTA d ON d.ID_VENTA = v.ID_VENTA
				         WHERE v.ID_SUCURSAL = s.ID_SUCURSAL AND v.FECHA BETWEEN :desde AND :hasta
				           AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)) AS TOTAL,
				       (SELECT COALESCE(SUM(d.CANTIDAD * (d.PRECIO_UNITARIO - p.COSTO_COMPRA)), 0)
				          FROM VENTA v JOIN DETALLE_VENTA d ON d.ID_VENTA = v.ID_VENTA JOIN PRODUCTO p ON p.ID_PRODUCTO = d.ID_PRODUCTO
				         WHERE v.ID_SUCURSAL = s.ID_SUCURSAL AND v.FECHA BETWEEN :desde AND :hasta
				           AND (:idEmpleado IS NULL OR v.ID_EMPLEADO = :idEmpleado)) AS UTILIDAD,
				       (SELECT COUNT(*) FROM INVENTARIO i
				         WHERE i.ID_SUCURSAL = s.ID_SUCURSAL AND i.CANTIDAD_DISPONIBLE <= :umbral) AS STOCK_BAJO,
				       (SELECT COALESCE(SUM(i.CANTIDAD_DISPONIBLE * p.COSTO_COMPRA), 0) FROM INVENTARIO i JOIN PRODUCTO p ON p.ID_PRODUCTO = i.ID_PRODUCTO
				         WHERE i.ID_SUCURSAL = s.ID_SUCURSAL) AS CAPITAL
				FROM SUCURSAL s
				WHERE :idSucursal IS NULL OR s.ID_SUCURSAL = :idSucursal
				ORDER BY TOTAL DESC, s.NOMBRE_SUCURSAL
				""", filtro.parametros().addValue("umbral", umbralStockBajo), (rs, i) -> {
					long cantidad = rs.getLong("CANTIDAD");
					BigDecimal total = rs.getBigDecimal("TOTAL");
					return new SucursalDetalle(rs.getInt("ID_SUCURSAL"), rs.getString("NOMBRE_SUCURSAL"), rs.getString("CIUDAD"), cantidad, total,
							promedio(total, cantidad), rs.getBigDecimal("UTILIDAD"), rs.getLong("STOCK_BAJO"), rs.getBigDecimal("CAPITAL"));
				});
	}

	public List<Comision> comisiones(Filtro filtro) {
		return jdbc.query("""
				SELECT ve.ID_EMPLEADO, CONCAT(e.NOMBRE, ' ', e.APELLIDO) AS NOMBRE, ve.COMISION_VENTA AS TASA,
				       GROUP_CONCAT(DISTINCT s.NOMBRE_SUCURSAL ORDER BY s.NOMBRE_SUCURSAL SEPARATOR '|') AS SUCURSALES,
				       COUNT(v.ID_VENTA) AS CANTIDAD,
				       COALESCE(SUM(t.MONTO_TOTAL), 0) AS TOTAL
				FROM VENDEDOR ve
				JOIN EMPLEADO e ON e.ID_EMPLEADO = ve.ID_EMPLEADO
				LEFT JOIN VENTA v ON v.ID_EMPLEADO = ve.ID_EMPLEADO
				  AND v.FECHA BETWEEN :desde AND :hasta
				  AND (:idSucursal IS NULL OR v.ID_SUCURSAL = :idSucursal)
				LEFT JOIN V_VENTA_TOTAL t ON t.ID_VENTA = v.ID_VENTA
				LEFT JOIN SUCURSAL s ON s.ID_SUCURSAL = v.ID_SUCURSAL
				WHERE :idEmpleado IS NULL OR ve.ID_EMPLEADO = :idEmpleado
				GROUP BY ve.ID_EMPLEADO, e.NOMBRE, e.APELLIDO, ve.COMISION_VENTA
				HAVING :idSucursal IS NULL OR COUNT(v.ID_VENTA) > 0
				ORDER BY TOTAL DESC, NOMBRE
				""", filtro.parametros(), (rs, i) -> {
					long cantidad = rs.getLong("CANTIDAD");
					BigDecimal total = rs.getBigDecimal("TOTAL");
					BigDecimal tasa = rs.getBigDecimal("TASA");
					String sucursales = rs.getString("SUCURSALES");
					return new Comision(rs.getInt("ID_EMPLEADO"), rs.getString("NOMBRE"),
							sucursales == null ? List.of() : Arrays.asList(sucursales.split("\\|")), cantidad, total, promedio(total, cantidad),
							tasa, total.multiply(tasa).setScale(2, RoundingMode.HALF_UP));
				});
	}

	private static Agrupado agrupado(ResultSet rs, int fila) throws SQLException {
		return new Agrupado(rs.getInt("ID"), rs.getString("NOMBRE"), rs.getBigDecimal("TOTAL"), rs.getLong("CANTIDAD"));
	}

	private static ProductoVendido productoVendido(ResultSet rs, int fila) throws SQLException {
		return new ProductoVendido(rs.getInt("ID_PRODUCTO"), rs.getString("CODIGO"), rs.getString("NOMBRE_PRODUCTO"), rs.getLong("UNIDADES"),
				rs.getBigDecimal("TOTAL"), rs.getBigDecimal("UTILIDAD"));
	}

	private static BigDecimal promedio(BigDecimal total, long cantidad) {
		return cantidad == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
	}
}
