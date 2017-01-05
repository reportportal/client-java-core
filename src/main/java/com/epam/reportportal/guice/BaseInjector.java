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

import com.epam.reportportal.exception.InternalReportPortalClientException;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guice BaseInjector for testing purposes<br>
 * Simple Singleton implementation
 *
 * @author Andrei Varabyeu
 */
public class BaseInjector {

	private static final String RP_EXTENSION_PROPERTY_NAME = "rp.extension";

	/**
	 * Guice BaseInjector
	 */
	private com.google.inject.Injector injector;

	/**
	 * Creates default report portal injector
	 */
	protected BaseInjector(Module... module) {
		String extensions = System.getProperty(RP_EXTENSION_PROPERTY_NAME, System.getenv(RP_EXTENSION_PROPERTY_NAME));
		if (!Strings.isNullOrEmpty(extensions)) {
			List<Module> extensionModules = buildExtensions(extensions);
			injector = Guice.createInjector(Modules.override(module).with(extensionModules));
		} else {
			injector = Guice.createInjector(module);
		}

	}

	protected BaseInjector(com.google.inject.Injector nativeInjector) {
		this.injector = nativeInjector;
	}

	protected com.google.inject.Injector createChildInjector(Module... modules) {
		return injector.createChildInjector(modules);
	}

	/**
	 * Returns bean of provided type
	 *
	 * @param type
	 */
	public <T> T getBean(Class<T> type) {
		return injector.getInstance(type);
	}

	/**
	 * Returns bean by provided key
	 *
	 * @param key
	 */
	public <T> T getBean(Key<T> key) {
		return injector.getInstance(key);
	}

	/**
	 * Returns binded property
	 *
	 * @param key
	 * @see ListenerProperty
	 */
	public String getProperty(ListenerProperty key) {
		return injector.getInstance(Key.get(String.class, new ListenerPropertyValueImpl(key)));
	}

	/**
	 * Builds extensions based on environment variable
	 *
	 * @param extensions Command-separated list of extension module classes
	 * @return List of Guice's modules
	 */
	private List<Module> buildExtensions(String extensions) {
		List<String> extensionClasses = Splitter.on(",").splitToList(extensions);
		List<Module> extensionModules = new ArrayList<>(extensionClasses.size());
		for (String extensionClass : extensionClasses) {
			try {
				Class<Module> extension = (Class<Module>) Class.forName(extensionClass);
				Preconditions.checkArgument(Module.class.isAssignableFrom(extension), "Extension class '%s' is not an Guice's Module", extensionClass);
				extensionModules.add(extension.getConstructor(new Class[] {}).newInstance());
			} catch (ClassNotFoundException e) {
				String errorMessage = "Extension class with name '" + extensionClass + "' not found";
				System.err.println(errorMessage);
				throw new InternalReportPortalClientException(errorMessage, e);

			} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
				String errorMessage = "Unable to create instance of '" + extensionClass + "'. Does it have empty constructor?";
				System.err.println(errorMessage);
				throw new InternalReportPortalClientException(errorMessage, e);
			}
		}
		return extensionModules;
	}
}
