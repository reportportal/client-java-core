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
package com.epam.reportportal.service;

import com.epam.reportportal.exception.ReportPortalClientException;
import com.epam.reportportal.exception.ReportPortalServerException;
import com.epam.ta.reportportal.ws.model.ErrorRS;
import com.github.avarabyeu.restendpoint.http.DefaultErrorHandler;
import com.github.avarabyeu.restendpoint.http.HttpMethod;
import com.github.avarabyeu.restendpoint.http.exception.SerializerException;
import com.github.avarabyeu.restendpoint.serializer.Serializer;
import com.google.common.base.Throwables;

import java.net.URI;

/**
 * Report Portal Error Handler<br>
 * Converts error from Endpoint to ReportPortal-related errors
 *
 * @author Andrei Varabyeu
 */
public class ReportPortalErrorHandler extends DefaultErrorHandler {

    private Serializer serializer;

    public ReportPortalErrorHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void handleClientError(URI requestUri, HttpMethod requestMethod, int statusCode, String statusMessage,
            byte[] errorBody) {
        try {
            throw new ReportPortalClientException(statusCode, statusMessage, deserializeError(errorBody));
        } catch (SerializerException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected void handleServerError(URI requestUri, HttpMethod requestMethod, int statusCode, String statusMessage,
            byte[] errorBody) {
        try {
            throw new ReportPortalServerException(statusCode, statusMessage, deserializeError(errorBody));
        } catch (SerializerException e) {
            throw Throwables.propagate(e);
        }
    }

    private ErrorRS deserializeError(byte[] content) throws SerializerException {
        return serializer.deserialize(content, ErrorRS.class);
    }
}
