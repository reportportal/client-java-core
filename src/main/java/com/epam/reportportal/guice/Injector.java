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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Module;

/**
 * Guice BaseInjector for testing purposes<br>
 * Simple Singleton implementation
 * 
 * @author Andrei Varabyeu
 * 
 */
public class Injector extends BaseInjector {

	/*
	 * Here is implementation of singleton with lazy init based on Google Guava
	 * Suppliers
	 */
	private static Supplier<Injector> instance = Suppliers.memoize(Injector::new);

	public static Injector wrap(com.google.inject.Injector nativeInjector) {
		return new Injector(nativeInjector);
	}

	public static Injector getInstance() {
		return instance.get();
	}

	/**
	 * Creates default report portal injector
	 */
	private Injector() {
		super(new ReportPortalClientModule());
	}

	private Injector(com.google.inject.Injector nativeInjector) {
		super(nativeInjector);
	}

	public Injector getChildInjector(Module... modules) {
		com.google.inject.Injector nativeInjector = instance.get().createChildInjector(modules);
		return new Injector(nativeInjector);

	}

}
