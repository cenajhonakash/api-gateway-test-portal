package com.testportal.gateway.helper;

import java.util.Arrays;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidatorHelper {

	@Value("${app.ms.routes.openendpoints}")
	private String[] openendpoints;

	public Predicate<ServerHttpRequest> isSecured = request -> Arrays.asList(openendpoints).stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
}
