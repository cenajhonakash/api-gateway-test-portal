package com.testportal.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.testportal.gateway.service.AutheticationService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

//https://stackoverflow.com/questions/67459625/spring-cloud-gateway-custom-gateway-filter-not-working
@Component
@Slf4j
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {

	@Autowired
	private AutheticationService autheticationService;

	public AuthenticationFilterFactory() {
		super(Config.class);
	}

	/*
	 * Logic to filter incoming requests Webflux dependency is mandatory
	 * 
	 */
	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			// ServerHttpRequest request;
			try {
				log.info("filter applied for exchange: {}", exchange.getRequest().getURI().getPath());
				autheticationService.validateExchangeRequest(exchange);
			} catch (Exception e) {
				log.error("Invalid access to resource: {}", e.getMessage());
				return onError(exchange, "Error while validating exchange request", HttpStatus.UNAUTHORIZED);
			}
			return chain.filter(exchange);
		});
	}

	private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		return response.setComplete();
	}

	public static class Config {

	}
}
