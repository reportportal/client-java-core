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

import com.epam.reportportal.service.ReportPortalService;
import org.junit.Assert;
import org.junit.Test;

import com.epam.reportportal.service.BatchedReportPortalService;

/**
 * Test base injector for report portal related stuff
 * 
 * @author Andrei Varabyeu
 * 
 */
public class InjectorTest {

	@Test
	public void testSingletons() {
		ReportPortalService service1 = Injector.getInstance().getBean(ReportPortalService.class);
		ReportPortalService service2 = Injector.getInstance().getBean(BatchedReportPortalService.class);

		Assert.assertEquals(service1, service2);

	}
}
