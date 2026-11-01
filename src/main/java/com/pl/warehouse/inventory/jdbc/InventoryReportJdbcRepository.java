package com.pl.warehouse.inventory.jdbc;

import com.pl.warehouse.inventory.dto.InventoryStockReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: JDBC report repository — contrast with Spring Data JPA elsewhere.
 *
 * <p>Uses {@link JdbcTemplate}: you write SQL; Spring maps {@code ResultSet} rows to DTOs.
 * Great for joins/aggregations where JPA would be awkward or N+1-prone.
 *
 * <p><b>Compare briefly:</b>
 * <ul>
 *   <li>JPA — entities, derived queries, CRUD; abstraction cost for complex reports</li>
 *   <li>JdbcTemplate (this class) — full SQL control, manual mapping</li>
 *   <li>Spring Data JDBC / MyBatis / jOOQ — other SQL-centric options in larger systems</li>
 * </ul>
 *
 * <p>No {@code @Transactional} needed for a single read query; the connection is short-lived.
 */
@Repository
public class InventoryReportJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public InventoryReportJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InventoryStockReport> findStockReport() {
        String sql = """
                SELECT
                    inv.id,
                    w.name  AS warehouse_name,
                    i.item_name,
                    inv.quantity,
                    inv.min_quantity
                FROM inventory inv
                JOIN warehouses w ON w.id = inv.warehouse_id
                JOIN warehouse_items i ON i.id = inv.item_id
                ORDER BY w.name, i.item_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new InventoryStockReport(
                UUID.fromString(rs.getString("id")),
                rs.getString("warehouse_name"),
                rs.getString("item_name"),
                rs.getInt("quantity"),
                rs.getInt("min_quantity"),
                rs.getInt("quantity") < rs.getInt("min_quantity")
        ));
    }
}
