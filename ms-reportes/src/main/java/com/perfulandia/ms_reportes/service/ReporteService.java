package com.perfulandia.ms_reportes.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.perfulandia.ms_reportes.dto.ItemInventarioDTO;
import com.perfulandia.ms_reportes.dto.ProductoDTO;
import com.perfulandia.ms_reportes.dto.ReporteRequestDTO;
import com.perfulandia.ms_reportes.dto.VentaDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.repository.ReporteRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.inventario.url:http://localhost:8083}")
    private String msInventarioUrl;

    @Value("${ms.catalogo.url:http://localhost:8082}")
    private String msCatalogoUrl;

    @Value("${ms.ventas.url:http://localhost:8084}")
    private String msVentasUrl;

    public Reporte generarReporte(ReporteRequestDTO request) {
        if (request.periodoInicio() == null || request.periodoFin() == null) {
            throw new RuntimeException("Debe indicar el periodo del reporte");
        }
        if (request.periodoInicio().isAfter(request.periodoFin())) {
            throw new RuntimeException("El periodo de inicio no puede ser posterior al periodo de fin");
        }

        String resumen = consultarDatos(request.tipo(), request.idSucursal());

        Reporte reporte = new Reporte();
        reporte.setTipo(request.tipo());
        reporte.setIdSucursal(request.idSucursal());
        reporte.setPeriodoInicio(request.periodoInicio());
        reporte.setPeriodoFin(request.periodoFin());
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setResumen(resumen);

        return reporteRepository.save(reporte);
    }

    private String consultarDatos(TipoReporte tipo, String idSucursal) {
        try {
            switch (tipo) {
                case VENTAS:
                    return consultarVentas(idSucursal);
                case INVENTARIO:
                    return consultarInventario(idSucursal);
                case RENDIMIENTO_SUCURSAL:
                    return consultarRendimiento(idSucursal);
                default:
                    return "Tipo de reporte no reconocido";
            }
        } catch (RestClientException e) {
            return "No se pudo obtener datos del microservicio: " + e.getMessage();
        }
    }

    private String consultarVentas(String idSucursal) {
        VentaDTO[] ventas = restTemplate.getForObject(
            msVentasUrl + "/api/v1/ventas/historial/" + idSucursal,
            VentaDTO[].class
        );
        if (ventas == null || ventas.length == 0) {
            return "No se encontraron ventas para la sucursal " + idSucursal;
        }
        List<VentaDTO> lista = Arrays.asList(ventas);
        double totalVentas = lista.stream().mapToDouble(VentaDTO::total).sum();
        return String.format(
            "REPORTE DE VENTAS | Sucursal: %s | Total de ventas: %d | Monto total: $%.2f",
            idSucursal, lista.size(), totalVentas
        );
    }

    private String consultarInventario(String idSucursal) {
        ItemInventarioDTO[] items = restTemplate.getForObject(
            msInventarioUrl + "/api/v1/inventario/sucursal/" + idSucursal,
            ItemInventarioDTO[].class
        );
        if (items == null || items.length == 0) {
            return "No se encontraron items de inventario para la sucursal " + idSucursal;
        }
        List<ItemInventarioDTO> lista = Arrays.asList(items);
        long bajoStock = lista.stream().filter(i -> i.cantidad() <= i.stockMinimo()).count();
        return String.format(
            "REPORTE DE INVENTARIO | Sucursal: %s | Total productos: %d | Bajo stock: %d",
            idSucursal, lista.size(), bajoStock
        );
    }

    private String consultarRendimiento(String idSucursal) {
        ProductoDTO[] productos = restTemplate.getForObject(
            msCatalogoUrl + "/api/v1/productos",
            ProductoDTO[].class
        );
        if (productos == null || productos.length == 0) {
            return "No se encontraron productos en el catalogo";
        }
        List<ProductoDTO> lista = Arrays.asList(productos);
        double precioPromedio = lista.stream().mapToDouble(ProductoDTO::precio).average().orElse(0);
        return String.format(
            "REPORTE DE RENDIMIENTO | Sucursal: %s | Productos en catalogo: %d | Precio promedio: $%.2f",
            idSucursal, lista.size(), precioPromedio
        );
    }
}