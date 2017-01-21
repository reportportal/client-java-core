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

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.message.HashMarkSeparatedMessageParser;
import com.epam.reportportal.message.MessageParser;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.service.ReportPortalErrorHandler;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.avarabyeu.restendpoint.http.DefaultErrorHandler;
import com.github.avarabyeu.restendpoint.http.ErrorHandler;
import com.github.avarabyeu.restendpoint.http.HttpClientRestEndpoint;
import com.github.avarabyeu.restendpoint.http.RestEndpoint;
import com.github.avarabyeu.restendpoint.http.RestEndpoints;
import com.github.avarabyeu.restendpoint.serializer.ByteArraySerializer;
import com.github.avarabyeu.restendpoint.serializer.Serializer;
import com.github.avarabyeu.restendpoint.serializer.json.JacksonSerializer;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HttpContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Report portal service endpoint and utils module
 */

public class ReportPortalClientModule implements Module {

    public static final String API_BASE = "/api/v1";
    private static final String HTTPS = "https";

    @Override
    public void configure(Binder binder) {
        Names.bindProperties(binder, PropertiesLoader.getProperties());
        for (final ListenerProperty listenerProperty : ListenerProperty.values()) {
            binder.bind(Key.get(String.class, ListenerPropertyBinder.named(listenerProperty)))
                    .toProvider(() -> PropertiesLoader.getProperty(listenerProperty.getPropertyName()));
        }
    }

    /**
     * Default {@link ReportPortalErrorHandler
     * ReportPortalErrorHandler(Serializer)} binding
     *
     * @param serializer Serializer
     */
    @Provides
    @Singleton
    public ErrorHandler<HttpUriRequest, HttpResponse> provideErrorHandler(Serializer serializer) {
        return new ReportPortalErrorHandler(serializer);
    }

    /**
     * Default {@link Serializer} binding
     */
    @Provides
    @Singleton
    public Serializer provideSerializer() {
        return new JacksonSerializer(new ObjectMapper());
    }

    @Provides
    @Singleton
    @Named("serializers")
    public List<Serializer> provideSeriazers(Serializer defaultSerializer) {
        return Lists.newArrayList(defaultSerializer, new ByteArraySerializer());
    }

    /**
     * Default {@link RestEndpoint} binding
     *
     * @param serializers
     * @param errorHandler
     * @param baseUrl
     * @param keyStore
     * @param keyStorePassword
     * @throws MalformedURLException
     */
    @Provides
    public ReportPortalClient provideRestEndpoint(@Named("serializers") List<Serializer> serializers,
            ErrorHandler<HttpUriRequest, HttpResponse> errorHandler,
            @ListenerPropertyValue(ListenerProperty.BASE_URL) String baseUrl,
            @Nullable @ListenerPropertyValue(ListenerProperty.KEYSTORE_RESOURCE) String keyStore,
            @Nullable @ListenerPropertyValue(ListenerProperty.KEYSTORE_PASSWORD) String keyStorePassword,
            @ListenerPropertyValue(ListenerProperty.UUID) final String uuid,
            @ListenerPropertyValue(ListenerProperty.PROJECT_NAME) String project) throws MalformedURLException {

        final HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();

        clientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom()
                .setIoThreadCount(32).build())
                .addInterceptorLast(
                (HttpRequestInterceptor) (request, context) -> request.addHeader(HttpHeaders.AUTHORIZATION, "bearer " + uuid));


        //
        //        if (HTTPS.equals(new URL(baseUrl).getProtocol())) {
        //            if (null == keyStore) {
        //                assert false : "You should provide keystore parameter [" + ListenerProperty.KEYSTORE_RESOURCE
        //                        + "] if you use HTTPS protocol";
        //            }
        //            httpClientFactory = new SslClientFactory(null, keyStore, keyStorePassword, interceptors);
        //        } else {
        //            httpClientFactory = new AuthClientFactory(null, interceptors);
        //        }

        return RestEndpoints.forInterface(ReportPortalClient.class, new HttpClientRestEndpoint(clientBuilder.build(), serializers, new DefaultErrorHandler(),
                baseUrl + API_BASE + "/" + project));
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
     * provides message parser
     */
    @Provides
    @Singleton
    public MessageParser provideMessageParser() {
        return new HashMarkSeparatedMessageParser();
    }

}
