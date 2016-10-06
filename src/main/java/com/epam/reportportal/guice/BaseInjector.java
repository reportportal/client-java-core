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
package com.epam.reportportal.guice;

import com.epam.reportportal.utils.properties.ListenerProperty;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;

/**
 * Guice BaseInjector for testing purposes<br>
 * Simple Singleton implementation
 * 
 * @author Andrei Varabyeu
 * 
 */
public class BaseInjector {

	/** Guice BaseInjector */
	private com.google.inject.Injector injector;

	/**
	 * Creates default report portal injector
	 */
	protected BaseInjector(Module... module) {
		injector = Guice.createInjector(module);
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
	 *
	 * @see ListenerProperty
	 */
	public String getProperty(ListenerProperty key) {
		return injector.getInstance(Key.get(String.class, new ListenerPropertyValueImpl(key)));
	}
}
