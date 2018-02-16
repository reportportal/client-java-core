/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/client-java-core
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.guice;

import com.epam.reportportal.apache.http.HttpRequestInterceptor;
import com.epam.reportportal.apache.http.HttpResponse;
import com.epam.reportportal.apache.http.client.HttpClient;
import com.epam.reportportal.exception.InternalReportPortalClientException;
import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.message.HashMarkSeparatedMessageParser;
import com.epam.reportportal.message.MessageParser;
import com.epam.reportportal.restclient.endpoint.*;
import com.epam.reportportal.restclient.serializer.Jackson2Serializer;
import com.epam.reportportal.service.BatchedReportPortalService;
import com.epam.reportportal.service.ReportPortalErrorHandler;
import com.epam.reportportal.service.ReportPortalService;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Report portal service endpoint and utils module
 */

public class ReportPortalClientModule implements Module {

	public static final String API_BASE = "/api/v1";
	private static final String HTTPS = "https";
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	@Override
	public void configure(Binder binder) {
		Names.bindProperties(binder, PropertiesLoader.getProperties());
		for (final ListenerProperty listenerProperty : ListenerProperty.values()) {
			binder.bind(Key.get(String.class, ListenerPropertyBinder.named(listenerProperty))).toProvider(new Provider<String>() {

				@Override
				public String get() {
					return PropertiesLoader.getProperty(listenerProperty.getPropertyName());
				}
			});
		}
	}

	/**
	 * Default {@link ReportPortalErrorHandler
	 * ReportPortalErrorHandler(Serializer)} binding
	 *
	 * @param serializer Default serializer for error handler
	 */
	@Provides
	@Singleton
	public ErrorHandler<HttpResponse> provideErrorHandler(Serializer serializer) {
		return new ReportPortalErrorHandler(serializer);
	}

	/**
	 * Default {@link com.epam.reportportal.restclient.endpoint.Serializer} binding
	 */
	@Provides
	@Singleton
	public Serializer provideSeriazer() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return new Jackson2Serializer(objectMapper);
	}

	@Provides
	@Singleton
	@Named("serializers")
	public List<Serializer> provideSeriazers(Serializer defaultSerializer) {
		return Lists.newArrayList(defaultSerializer, new ByteArraySerializer());
	}

	/**
	 * Default {@link com.epam.reportportal.restclient.endpoint.RestEndpoint} binding
	 *
	 * @param serializers  Set of serializers to marshal request/response body
	 * @param errorHandler Handler for 4xx/5xx HTTP responses
	 * @param baseUrl      Base url of application
	 */
	@Provides
	public RestEndpoint provideRestEndpoint(HttpClient httpClient, @Named("serializers") List<Serializer> serializers,
			ErrorHandler<HttpResponse> errorHandler, @ListenerPropertyValue(ListenerProperty.BASE_URL) String baseUrl) {
		return new HttpClientRestEndpoint(httpClient, serializers, errorHandler, baseUrl);
	}

	/**
	 * Default {@link com.epam.reportportal.apache.http.client.HttpClient} binding
	 *
	 * @param baseUrl          Base URL of application
	 * @param keyStore         Path to keystore
	 * @param keyStorePassword Keystore password
	 * @throws MalformedURLException If URL is not correct
	 */
	@Provides
	public HttpClient provideHttpClient(@ListenerPropertyValue(ListenerProperty.BASE_URL) String baseUrl,
			@Nullable @ListenerPropertyValue(ListenerProperty.KEYSTORE_RESOURCE) String keyStore,
			@Nullable @ListenerPropertyValue(ListenerProperty.KEYSTORE_PASSWORD) String keyStorePassword,
			@ListenerPropertyValue(ListenerProperty.UUID) String uuid) throws MalformedURLException {

		HttpClientFactory httpClientFactory;

		List<HttpRequestInterceptor> interceptors = new ArrayList<HttpRequestInterceptor>(1);
		interceptors.add(new BearerAuthorizationInterceptor(uuid));

		if (HTTPS.equals(new URL(baseUrl).getProtocol()) && keyStore != null) {
			if (null == keyStorePassword) {
				throw new InternalReportPortalClientException(
						"You should provide keystore password parameter [" + ListenerProperty.KEYSTORE_PASSWORD
								+ "] if you use HTTPS protocol");
			}
			httpClientFactory = new SslClientFactory(null, keyStore, keyStorePassword, interceptors);
		} else {
			httpClientFactory = new AuthClientFactory(null, interceptors);
		}

		return httpClientFactory.createHttpClient();
	}

	/**
	 * Provides wrapper for report portal properties
	 */
	@Provides
	@Singleton
	public ListenerParameters provideListenerProperties() {
		return new ListenerParameters(PropertiesLoader.getProperties());
	}

	/**
	 * Provides junit-style reportportal service
	 *
	 * @param restEndpoint {@link RestEndpoint} instance
	 */
	@Provides
	@Singleton
	public BatchedReportPortalService provideReportPortalService(RestEndpoint restEndpoint,
			@ListenerPropertyValue(ListenerProperty.PROJECT_NAME) String project,
			@ListenerPropertyValue(ListenerProperty.BATCH_SIZE_LOGS) String batchLogsSize) { // NOSONAR
		int logsBatchSize;
		try {
			logsBatchSize = Integer.parseInt(batchLogsSize);
		} catch (NumberFormatException e) {
			logsBatchSize = 10;
		}
		return new BatchedReportPortalService(restEndpoint, API_BASE, project, logsBatchSize);
	}

	/**
	 * Binds the same instance for {@link ReportPortalService} interface.
	 * Guice cannot bind one implementation to two interfaces automatically
	 *
	 * @param reportPortalService Instance for binding
	 * @return {@link ReportPortalService} instance
	 */
	@Provides
	@Singleton
	public ReportPortalService provideRepoPortalService(BatchedReportPortalService reportPortalService) {
		return reportPortalService;
	}

	/**
	 * provides message parser
	 */
	@Provides
	@Singleton
	public MessageParser provideMessageParser() {
		return new HashMarkSeparatedMessageParser();
	}

}
