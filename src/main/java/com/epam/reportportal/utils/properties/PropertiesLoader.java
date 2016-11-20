/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import com.epam.reportportal.exception.InternalReportPortalClientException;
import com.epam.reportportal.restclient.endpoint.IOUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Resources;

/**
 * Load report portal launch start properties
 */
public class PropertiesLoader {
	public static final String INNER_PATH = "reportportal.properties";
	public static final String PATH = "./reportportal.properties";
	private static final String[] PROXY_PROPERTIES = { "http.proxyHost", "http.proxyPort", "http.nonProxyHosts", "https.proxyHost",
			"https.proxyPort", "ftp.proxyHost", "ftp.proxyPort", "ftp.nonProxyHosts", "socksProxyHost", "socksProxyPort", "http.proxyUser",
			"http.proxyPassword" };

	private static Supplier<Properties> propertiesSupplier = Suppliers.memoize(new Supplier<Properties>() {
		@Override
		public Properties get() {
			try {
				return loadProperties();
			} catch (IOException e) {
				throw new InternalReportPortalClientException("Unable to load properties", e);
			}
		}
	});

	/**
	 * Get specified property loaded from properties file and reloaded from from
	 * environment variables.
	 *
	 * @param propertyName
	 */
	public static String getProperty(String propertyName) {
		return propertiesSupplier.get().getProperty(propertyName);
	}

	/**
	 * Get all properties loaded from properties file and reloaded from from
	 * environment variables.
	 */
	public static Properties getProperties() {
		return propertiesSupplier.get();
	}

	/**
	 * Try to load properties from file situated in the class path, and then
	 * reload existing parameters from environment variables
	 *
	 * @return
	 * @throws IOException
	 */
	private static Properties loadProperties() throws IOException {
		Properties props = new Properties();
		Optional<URL> propertyFile = getResource(INNER_PATH);
		if (propertyFile.isPresent()) {
			props.load(Resources.asByteSource(propertyFile.get()).openBufferedStream());
		}
        reloadFromSystemProperties(props);
		reloadFromEnvVariables(props);
		reloadFromSoapUI(props);
		validateProperties(props);
		reloadProperties(props);
		setProxyProperties(props);
		return props;
	}

	// will be removed in next release
	private static void reloadProperties(Properties props) {
		for (ListenerProperty property : ListenerProperty.values()) {
			if (property.getPropertyName().startsWith("rp.") && props.getProperty(property.getPropertyName()) == null) {
				String value = props.getProperty(property.getPropertyName().replace("rp.", "com.epam.ta.reportportal.ws."));
				if (value != null)
					props.put(property.getPropertyName(), value);
			}
		}
	}

	/**
	 * Current version of agents should load properties only from properties
	 * file on classpath
	 */
	@SuppressWarnings("unused")
	@Deprecated()
	private static Properties loadFromFile() throws IOException {
		Properties props = new Properties();
		File propertiesFile = new File(PATH);
		InputStream is = null;
		try {
			is = propertiesFile.exists() ? new FileInputStream(propertiesFile) : PropertiesLoader.class.getResourceAsStream(INNER_PATH);
			if (is == null) {
				throw new FileNotFoundException(INNER_PATH);
			}
			props.load(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
		return props;
	}

	/**
	 * Reload properties from system properties.
	 *
	 * @param props
	 * @return props
	 */
	public static Properties reloadFromSystemProperties(Properties props) {
        Properties systemProperties = System.getProperties();
        for (ListenerProperty listenerProperty : ListenerProperty.values()) {
            if (systemProperties.getProperty(listenerProperty.getPropertyName()) != null) {
                props.setProperty(listenerProperty.getPropertyName(), systemProperties.getProperty(listenerProperty.getPropertyName()));
            }
        }
        return props;
    }

    /**
     * Reload properties from environment variables.
     *
     * @param props
     * @return props
     */
	public static Properties reloadFromEnvVariables(Properties props) {
		Map<String, String> environmentVariables = System.getenv();
		for (ListenerProperty listenerProperty : ListenerProperty.values()) {
			if (environmentVariables.get(listenerProperty.getPropertyName()) != null) {
				props.setProperty(listenerProperty.getPropertyName(), environmentVariables.get(listenerProperty.getPropertyName()));
			}
		}
		return props;
	}

	/**
	 * Validate that properties: {@link ListenerProperty#USER_NAME};
	 * {@link ListenerProperty#UUID}; {@link ListenerProperty#BASE_URL};
	 * {@link ListenerProperty#PROJECT_NAME};
	 * {@link ListenerProperty#LAUNCH_NAME}; not null
	 *
	 * @param properties
	 */
	private static void validateProperties(Properties properties) {
		// don't remove this code !!!
		for (ListenerProperty listenerProperty : ListenerProperty.values()) {
			if (listenerProperty.isRequired() && properties.getProperty(listenerProperty.getPropertyName()) == null) {
				throw new IllegalArgumentException(new StringBuilder("Property '").append(listenerProperty.getPropertyName())
						.append("' should not be null.").toString());
			}
		}
	}

	/**
	 * Reload soapui properties if required
	 *
	 * @param properties
	 */
	public static Properties reloadFromSoapUI(Properties properties) {
		Map<String, String> soapUIProperties = SoapUIPropertiesHolder.getSoapUIProperties();
		if (soapUIProperties == null) {
			return properties;
		}
		for (ListenerProperty listenerProperty : ListenerProperty.values()) {
			if (soapUIProperties.containsKey(listenerProperty.getPropertyName())) {
				properties.setProperty(listenerProperty.getPropertyName(), soapUIProperties.get(listenerProperty.getPropertyName()));
			}
		}
		return properties;
	}

	private static Optional<URL> getResource(String resourceName) {
		ClassLoader loader = MoreObjects.firstNonNull(Thread.currentThread().getContextClassLoader(),
				PropertiesLoader.class.getClassLoader());
		return Optional.fromNullable(loader.getResource(resourceName));
	}

	private static void setProxyProperties(Properties properties) {
		for (String property : PROXY_PROPERTIES) {
			if (properties.containsKey(property)) {
				System.setProperty(property, properties.get(property).toString());
			}
		}
		final String userName = System.getProperty("http.proxyUser");
		final String password = System.getProperty("http.proxyPassword");
		if (userName != null && password != null) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password.toCharArray());
				}
			});
		}
	}
}
