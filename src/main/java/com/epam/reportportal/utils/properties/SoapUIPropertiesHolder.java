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
package com.epam.reportportal.utils.properties;

import java.util.Map;

/**
 * Holds SOAPUI properties.
 * 
 * {@link PropertiesLoader} use soap properties from this holder during loading
 * properties from soapui.
 * 
 * @author Aliaksei_Makayed
 * 
 */
public class SoapUIPropertiesHolder {

	private static Map<String, String> soapUIProperties;

	public synchronized static Map<String, String> getSoapUIProperties() {
		return soapUIProperties;
	}

	public synchronized static void setSoapUIProperties(Map<String, String> soapUIProperties) {
		SoapUIPropertiesHolder.soapUIProperties = soapUIProperties;
	}
}
