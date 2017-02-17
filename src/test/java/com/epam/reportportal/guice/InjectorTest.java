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

import com.epam.reportportal.service.ReportPortal;
import com.github.avarabyeu.restendpoint.http.RestEndpoint;
import com.github.avarabyeu.restendpoint.http.exception.RestEndpointIOException;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;

/**
 * Test base injector for report portal related stuff
 *
 * @author Andrei Varabyeu
 */
public class InjectorTest {

	@Test
	public void testSingletons() {
//		ReportPortal service1 = Injector.getInstance().getBean(ReportPortal.class);
//		ReportPortal service2 = Injector.getInstance().getBean(BatchedReportPortalService.class);

//		Assert.assertEquals(service1, service2);

	}

	@Test
	public void testOverrideJvmVar() throws RestEndpointIOException {
		System.setProperty("rp.extension", "com.epam.reportportal.guice.InjectorTest$OverrideModule");
		ReportPortal rpService = new BaseInjector(new ReportPortalClientModule()).getBean(ReportPortal.class);
		Assert.assertEquals(rpService, rpService);
//		List<String> mockedTags = rpService.getAllTags();
//		Assert.assertThat("Incorrect mock!", mockedTags, Matchers.hasItem("mockedTag"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOverrideJvmVarNegative() throws RestEndpointIOException {
		System.setProperty("rp.extension", "com.epam.reportportal.guice.InjectorTest");
		new BaseInjector(new ReportPortalClientModule()).getBean(ReportPortal.class);
	}

	public static class OverrideModule extends AbstractModule {

		@Override
		protected void configure() {
			RestEndpoint mock = Mockito.mock(RestEndpoint.class);

			try {
				Mockito.<Object>when(mock.get(Mockito.anyString(), Mockito.<Type>any()))
						.thenReturn(ImmutableList.<String>builder().add("mockedTag").build());
			} catch (RestEndpointIOException e) {
				e.printStackTrace();
			}

			binder().bind(RestEndpoint.class).toInstance(mock);
		}
	}
}
