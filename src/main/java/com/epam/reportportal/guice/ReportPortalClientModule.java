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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.message.HashMarkSeparatedMessageParser;
import com.epam.reportportal.message.MessageParser;
import com.epam.reportportal.service.BatchedReportPortalService;
import com.epam.reportportal.service.ReportPortalErrorHandler;
import com.epam.reportportal.service.ReportPortalService;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.reportportal.restclient.serializer.Jackson2Serializer;
import com.epam.reportportal.apache.http.HttpRequestInterceptor;
import com.epam.reportportal.apache.http.HttpResponse;
import com.epam.reportportal.restclient.endpoint.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Report portal service endpoint and utils module
 * 
 */

public class ReportPortalClientModule implements Module {

	public static final String API_BASE = "/api/v1";
	private static final String HTTPS = "https";

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
	 * @param serializer
	 */
	@Provides
	@Singleton
	public ErrorHandler<HttpResponse> provideErrorHandler(Serializer serializer) {
		return new ReportPortalErrorHandler(serializer);
	}

	/**
	 * Default {@link com.epam.reportportal.restclient.endpoint.Serializer} binding
	 * 
	 */
	@Provides
	@Singleton
	public Serializer provideSeriazer() {
		return new Jackson2Serializer(new ObjectMapper());
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
	 * @param serializers
	 * @param errorHandler
	 * @param baseUrl
	 * @param keyStore
	 * @param keyStorePassword
	 * @throws MalformedURLException
	 */
	@Provides
	public RestEndpoint provideRestEndpoint(@Named("serializers") List<Serializer> serializers, ErrorHandler<HttpResponse> errorHandler,
			@ListenerPropertyValue(ListenerProperty.BASE_URL) String baseUrl,
			@Nullable @ListenerPropertyValue(ListenerProperty.KEYSTORE_RESOURCE) String keyStore,
			@Nullable @ListenerPropertyValue(ListenerProperty.KEYSTORE_PASSWORD) String keyStorePassword,
			@ListenerPropertyValue(ListenerProperty.UUID) String uuid) throws MalformedURLException {

		HttpClientFactory httpClientFactory;

		List<HttpRequestInterceptor> interceptors = new ArrayList<HttpRequestInterceptor>(1);
		interceptors.add(new BearerAuthorizationInterceptor(uuid));

		if (HTTPS.equals(new URL(baseUrl).getProtocol())) {
			if (null == keyStore) {
				assert false : "You should provide keystore parameter [" + ListenerProperty.KEYSTORE_RESOURCE
						+ "] if you use HTTPS protocol";
			}
			httpClientFactory = new SslClientFactory(null, keyStore, keyStorePassword, interceptors);
		} else {
			httpClientFactory = new AuthClientFactory(null, interceptors);
		}

		return new HttpClientRestEndpoint(httpClientFactory.createHttpClient(), serializers, errorHandler, baseUrl);
	}

	/**
	 * Provides wrapper for report portal properties
	 * 
	 */
	@Provides
	@Singleton
	public ListenerParameters provideListenerProperties() {
		return new ListenerParameters(PropertiesLoader.getProperties());
	}

	/**
	 * Provides junit-style reportportal service
	 * 
	 * @param restEndpoint
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
	 * Guice cannot bind {@link #provideRepoPortalService(BatchedReportPortalService)} to {@link ReportPortalService} automatically
	 * @param reportPortalService Instance for binding
	 * @return {@link ReportPortalService} instance
	 */
	@Provides
	@Singleton
	public ReportPortalService provideRepoPortalService(BatchedReportPortalService reportPortalService){
		return reportPortalService;
	}

	/**
	 * provides message parser
	 * 
	 */
	@Provides
	@Singleton
	public MessageParser provideMessageParser() {
		return new HashMarkSeparatedMessageParser();
	}

}
