package com.pl.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * STUDY — Application entry point (bootstraps the whole warehouse API).
 *
 * <h2>What is {@code @SpringBootApplication}?</h2>
 * One annotation that combines three:
 * <ul>
 *   <li>{@code @Configuration} — this class can declare {@code @Bean} methods</li>
 *   <li>{@code @EnableAutoConfiguration} — Spring Boot wires JDBC, JPA, Security, etc. from the classpath</li>
 *   <li>{@code @ComponentScan} — finds {@code @Service}, {@code @RestController}, {@code @Repository}
 *       under {@code com.pl.warehouse} and below</li>
 * </ul>
 *
 * <p>Scan root = this class's package. Feature packages ({@code item}, {@code inventory}, …)
 * and shared ones ({@code config}, {@code common}) are picked up automatically.
 *
 * <p>Users / password encoding live in {@link com.pl.warehouse.config.SecurityConfig}.
 */
@SpringBootApplication
public class WareHouseApplication {

	public static void main(String[] args) {
		SpringApplication.run(WareHouseApplication.class, args);
	}
}
