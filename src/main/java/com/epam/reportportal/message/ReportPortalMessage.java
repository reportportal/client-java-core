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
package com.epam.reportportal.message;

import java.io.File;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;

/**
 * Report portal message wrapper. This wrapper should be used if any file <br>
 * should be attached to log message. This wrapper should be used<br>
 * only with log4j log framework.
 */
public class ReportPortalMessage {

	private ByteSource data;

	private String message;

	public ReportPortalMessage() {
	}

	public ReportPortalMessage(String message) {
		this.message = message;
	}

	public ReportPortalMessage(final ByteSource data, String message) {
		this(message);
		this.data = data;
	}

	public ReportPortalMessage(File file, String message) {
		this(message);
		data = Files.asByteSource(file);
	}

	public String getMessage() {
		return message;
	}

	public ByteSource getData() {
		return data;
	}

}
