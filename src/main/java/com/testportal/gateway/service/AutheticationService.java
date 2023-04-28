package com.testportal.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.testportal.gateway.constants.AppConstants;
import com.testportal.gateway.exception.AccessDeniedException;
import com.testportal.gateway.helper.RouteValidatorHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AutheticationService {

	@Autowired
	private RouteValidatorHelper routeValidatorHelper;
	@Autowired
	private RestTemplate restTemplate;

	@Value("${auth.validate.token.path.internal}")
	private String validateTokenPath;

	public void validateExchangeRequest(ServerWebExchange exchange) throws Exception {
		if (routeValidatorHelper.isSecured.test(exchange.getRequest())) {
			// log.info("Unauthorized endpoint: {}", exchange.getRequest().getURI().getPath());
			if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				log.debug("Missing Authorization Header");
				throw new AccessDeniedException("Missing Authorization Header");
			}
			String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			if (authHeader != null && authHeader.startsWith(AppConstants.Bearer.name())) {
				authHeader = authHeader.substring(7);
			}
			final HttpHeaders headers = exchange.getRequest().getHeaders();
			MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
			final String urlTemplate = UriComponentsBuilder.fromHttpUrl(validateTokenPath).queryParams(queryParams).build().encode().toUriString();
			// final String urlTemplate = UriComponentsBuilder.fromHttpUrl(validateTokenPath).build().encode().toUriString();
			log.info("url created: {}", urlTemplate);
			try {
				ResponseEntity<Boolean> response = restTemplate.exchange(urlTemplate, HttpMethod.POST, new HttpEntity<>(headers), Boolean.class);
				if (response == null || response.getBody() == null || response.getBody().equals(Boolean.FALSE)) {
					throw new AccessDeniedException("Invalid Token");
				}
			} catch (AccessDeniedException e) {
				log.info("Aceess denied for the validation: {}", e.getMessage());
				throw new AccessDeniedException("Invalid Token");
			} catch (Exception e) {
				log.info("error while validating token: {}", e.getMessage());
				throw new RuntimeException("Error in getting response from rest call");
			}
		}
	}

}
