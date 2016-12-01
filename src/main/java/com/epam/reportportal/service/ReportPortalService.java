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

import java.util.*;

import org.springframework.hateoas.PagedResources;

import com.epam.reportportal.exception.InternalReportPortalClientException;
import com.epam.reportportal.utils.queue.Result;
import com.epam.reportportal.apache.http.entity.ContentType;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import com.epam.ta.reportportal.ws.model.favorites.AddFavoriteResourceRQ;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.user.*;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.epam.reportportal.restclient.endpoint.MultiPartRequest;
import com.epam.reportportal.restclient.endpoint.RestEndpoint;
import com.epam.reportportal.restclient.endpoint.exception.RestEndpointIOException;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

/**
 * Default ReportPortal Reporter implementation. Uses
 * {@link com.epam.ta.restclient.endpoint.RestEndpoint} as REST WS Client
 * 
 * @author Andrei Varabyeu
 * 
 */
public class ReportPortalService implements IReportPortalService {
	/** REST Client */
	protected RestEndpoint endpoint;

	protected String apiBase;

	protected String project;

	public ReportPortalService(RestEndpoint endpoint, String apiBase, String project) {
		this.endpoint = Preconditions.checkNotNull(endpoint, "RestEndpoing shouldn't be NULL");
		this.apiBase = Preconditions.checkNotNull(apiBase, "API base shouldn't be null");
		this.project = Preconditions.checkNotNull(project, "Project shouldn't be null");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#startLaunch
	 * (java.lang.String,
	 * com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ)
	 */
	@Override
	public EntryCreatedRS startLaunch(StartLaunchRQ rq) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/launch/", rq, EntryCreatedRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#finishLaunch
	 * (java.lang.String, java.lang.String,
	 * com.epam.ta.reportportal.ws.model.FinishExecutionRQ)
	 */
	@Override
	public OperationCompletionRS finishLaunch(String launchID, FinishExecutionRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/launch/" + launchID + "/finish", rq, OperationCompletionRS.class);
	}

	@Override
	public Result<? extends EntryCreatedRS> log(SaveLogRQ rq) throws RestEndpointIOException {
		if (null == rq.getFile()) {
			EntryCreatedRS response = endpoint.post(apiBase + "/" + project + "/log", rq, EntryCreatedRS.class);
			return new Result<EntryCreatedRS>(response);
		} else {
			MultiPartRequest<SaveLogRQ[]> request = new MultiPartRequest.Builder<SaveLogRQ[]>()
					.addSerializedPart(Constants.LOG_REQUEST_JSON_PART, new SaveLogRQ[] { rq }).addBinaryPart(rq.getFile().getName(),
							rq.getFile().getName(), ContentType.APPLICATION_OCTET_STREAM.getMimeType(), rq.getFile().getContent())
					.build();

			BatchSaveOperatingRS response = endpoint.post(apiBase + "/" + project + "/log", request, BatchSaveOperatingRS.class);
			if (response.getResponses() == null || response.getResponses().size() == 0) {
				throw new InternalReportPortalClientException("Report portal hasn't sent any response.");
			}
			return new Result<EntryCreatedRS>(response.getResponses().get(0));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getLaunch
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public LaunchResource getLaunch(String launchId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/launch/" + launchId, LaunchResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getLaunches
	 * (java.lang.String)
	 */
	@Override
	public PagedResources<LaunchResource> getProjectLaunches() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/launch", new TypeToken<PagedResources<LaunchResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getLaunches
	 * (java.lang.String)
	 */
	@Override
	public PagedResources<LaunchResource> getLaunches(Map<String, String> parameters) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/launch", parameters, new TypeToken<PagedResources<LaunchResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	@Override
	public PagedResources<LaunchResource> getUserLaunches(String project, Map<String, String> parameters) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/launch/mode", parameters, new TypeToken<PagedResources<LaunchResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getProjectTestItems()
	 */
	@Override
	public PagedResources<TestItemResource> getProjectTestItems() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/item", new TypeToken<PagedResources<TestItemResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# getAllTestItems
	 * (java.lang.String, java.util.Map)
	 */
	@Override
	public PagedResources<TestItemResource> getAllTestItems(Map<String, String> parameters) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/item", parameters, new TypeToken<PagedResources<TestItemResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# getTestItem
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public TestItemResource getTestItem(String itemId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/item/" + itemId, TestItemResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# deleteTestItem
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public OperationCompletionRS deleteTestItem(String itemId) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + project + "/item/" + itemId, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * startRootTestItem (java.lang.String,
	 * com.epam.ta.reportportal.ws.model.StartTestItemRQ)
	 */
	@Override
	public EntryCreatedRS startRootTestItem(StartTestItemRQ rq) throws RestEndpointIOException {
		return this.startTestItem(null, rq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# startTestItem
	 * (java.lang.String, java.lang.String,
	 * com.epam.ta.reportportal.ws.model.StartTestItemRQ)
	 */
	@Override
	public EntryCreatedRS startTestItem(String parentItemId, StartTestItemRQ rq) throws RestEndpointIOException {
		StringBuilder urlBuilder = new StringBuilder(apiBase);
		urlBuilder.append("/");
		urlBuilder.append(project);
		urlBuilder.append("/item");
		if (parentItemId != null) {
			urlBuilder.append("/");
			urlBuilder.append(parentItemId);
		}
		return endpoint.post(urlBuilder.toString(), rq, EntryCreatedRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# finishTestItem
	 * (java.lang.String, java.lang.String,
	 * com.epam.ta.reportportal.ws.model.FinishTestItemRQ)
	 */
	@Override
	public OperationCompletionRS finishTestItem(String itemId, FinishTestItemRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/item/" + itemId, rq, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# deleteLaunch
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public OperationCompletionRS deleteLaunch(String launchId) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + project + "/launch/" + launchId, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * defineTestItemIssueType (java.lang.String,
	 * com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ)
	 */

	@Override
	public List<Issue> defineTestItemIssueType(DefineIssueRQ request) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/item/", request, new TypeToken<List<Issue>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# deleteLog
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public OperationCompletionRS deleteLog(String logId) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + project + "/log/" + logId, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# getLog
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public LogResource getLog(String logId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/log/" + logId, LogResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# getAllLogs
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public PagedResources<LogResource> getAllLogs(String testStepId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/log", Collections.singletonMap("filter.eq.item", testStepId),
				new TypeToken<PagedResources<LogResource>>() {
					private static final long serialVersionUID = 1L;
				}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# getAllLogs
	 * (java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public PagedResources<LogResource> getAllLogs(String testStepId, Map<String, String> parameters) throws RestEndpointIOException {
		parameters.put("filter.eq.item", testStepId);
		return endpoint.get(apiBase + "/" + project + "/log", parameters, new TypeToken<PagedResources<LogResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# createUserFilter
	 * (java.lang.String, com.epam.ta.reportportal.ws.model.filter.UserFilterRQ)
	 */
	@Override
	public List<EntryCreatedRS> createUserFilter(CollectionsRQ<CreateUserFilterRQ> rq) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/filter", rq, new TypeToken<List<EntryCreatedRS>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getUserFilter
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public UserFilterResource getUserFilter(String userFilterId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/filter/" + userFilterId, UserFilterResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * updateUserFilter(java.lang.String,
	 * com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ)
	 */
	@Override
	public OperationCompletionRS updateUserFilter(String filterId, UpdateUserFilterRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/filter/" + filterId, rq, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getAllUserFilters (java.lang.String)
	 */
	@Override
	public PagedResources<UserFilterResource> getProjectUserFilters() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/filter", new TypeToken<PagedResources<UserFilterResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getAllUserFilters (java.lang.String, java.util.Map)
	 */
	@Override
	public PagedResources<UserFilterResource> getAllUserFilters(Map<String, String> parameters) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/filter", parameters, new TypeToken<PagedResources<UserFilterResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getAllUserFilterNames (java.lang.String)
	 */
	@Override
	public Map<String, SharedEntity> getUserFilters() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/filter/names", new TypeToken<LinkedHashMap<String, SharedEntity>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getAllSharedFilterNames (java.lang.String)
	 */
	@Override
	public Map<String, SharedEntity> getSharedFilters() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/filter/names", Collections.singletonMap("is_shared", "true"),
				new TypeToken<LinkedHashMap<String, SharedEntity>>() {
					private static final long serialVersionUID = 1L;
				}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# deleteUserFilter
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public OperationCompletionRS deleteUserFilter(String userFilterId) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + project + "/filter/" + userFilterId, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#createWidget
	 * (com.epam.ta.reportportal.ws.model.widget.WidgetRQ)
	 */
	@Override
	public EntryCreatedRS createWidget(WidgetRQ rq) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/widget", rq, EntryCreatedRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getWidget
	 * (java.lang.String)
	 */
	@Override
	public WidgetResource getWidget(String widgetId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/widget/" + widgetId, WidgetResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getAllWidgets
	 * (java.lang.String)
	 */
	@Override
	public Map<String, SharedEntity> getSharedWidgets() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/widget/names/shared", new TypeToken<Map<String, SharedEntity>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	@Override
	public Iterable<Iterable<String>> getChartData(String widgetId) throws RestEndpointIOException {
		return null;
		// return endpoint.get(apiBase + "/" + project + "/widget/"+widgetId,
		// null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#deleteWidget
	 * (java.lang.String)
	 */
	@Override
	public OperationCompletionRS updateWidget(String widgetId, WidgetRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/widget/" + widgetId, rq, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService# createDashboard
	 * (com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ)
	 */
	@Override
	public EntryCreatedRS createDashboard(CreateDashboardRQ rq) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/dashboard", rq, EntryCreatedRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getProjectDashboards()
	 */
	@Override
	public List<DashboardResource> getProjectDashboards() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/dashboard", new TypeToken<List<DashboardResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * deleteDashboard(java.lang.String)
	 */
	@Override
	public OperationCompletionRS deleteDashboard(String dashboardID) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + project + "/dashboard/" + dashboardID, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * updateDashboard(java.lang.String,
	 * com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ)
	 */
	@Override
	public OperationCompletionRS updateDashboard(String dashboardID, UpdateDashboardRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/dashboard/" + dashboardID, rq, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getSharedDashboards()
	 */
	@Override
	public Map<String, SharedEntity> getSharedDashboards() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/dashboard/shared", new TypeToken<Map<String, SharedEntity>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * synchronizeUser()
	 */
	@Override
	public OperationCompletionRS synchronizeUser() throws RestEndpointIOException {
		return endpoint.post(apiBase + "/upsa/synchronize", new CreateUserRQ(), OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getUser
	 * (java.lang.String)
	 */
	@Override
	public UserResource getUser(String username) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/user/" + username, UserResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#editUser
	 * (java.lang.String, com.epam.ta.reportportal.ws.model.user.EditUserRQ)
	 */
	@Override
	public OperationCompletionRS editUser(String username, EditUserRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/user/" + username, rq, OperationCompletionRS.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getMyPhoto ()
	 */
	@Override
	public byte[] getMyPhoto() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/data/photo", byte[].class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#
	 * getTestItemsHistory(java.lang.String)
	 */
	@Override
	public List<TestItemHistoryElement> getTestItemsHistory(Map<String, String> parameters) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/item/history", parameters, new TypeToken<ArrayList<TestItemHistoryElement>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getAllTags
	 * (java.lang.String)
	 */
	@Override
	public List<String> getAllTags(String value) throws RestEndpointIOException {

		return endpoint.get(apiBase + "/" + project + "/launch/tags", Collections.singletonMap("filter.cnt.tags", value),
				new TypeToken<ArrayList<String>>() {

					private static final long serialVersionUID = 1L;
				}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#getAllTags ()
	 */
	@Override
	public List<String> getAllTags() throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/launch/tags", new TypeToken<ArrayList<String>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.reportportal.service.IReportPortalService#updateLaunch
	 * (java.lang.String,
	 * com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ)
	 */
	@Override
	public OperationCompletionRS updateLaunch(String launchID, UpdateLaunchRQ rq) throws RestEndpointIOException {

		return endpoint.put(apiBase + "/" + project + "/launch/" + launchID + "/update", rq, OperationCompletionRS.class);
	}

	@Override
	public CreateUserBidRS createUser(CreateUserRQ rq) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/user/bid", rq, CreateUserBidRS.class);
	}

	@Override
	public CreateUserRS confirmUser(String registrationUuid, CreateUserRQConfirm rq) throws RestEndpointIOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("uuid", registrationUuid);
		return endpoint.post(apiBase + "/user/registration", parameters, rq, CreateUserRS.class);
	}

	@Override
	public OperationCompletionRS deleteUser(String login) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/user/" + login, OperationCompletionRS.class);
	}

	@Override
	public EntryCreatedRS createProject(CreateProjectRQ rq) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/project", rq, EntryCreatedRS.class);
	}

	@Override
	public OperationCompletionRS updateProject(UpdateProjectRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/project/" + project, rq, OperationCompletionRS.class);
	}

	@Override
	public OperationCompletionRS deleteProject(String project) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/project/" + project, OperationCompletionRS.class);
	}

	@Override
	public OperationCompletionRS unassignProjectUsers(String projectName, UnassignUsersRQ unassignUsersRQ) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/project/" + projectName + "/unassign", unassignUsersRQ, OperationCompletionRS.class);
	}

	@Override
	public OperationCompletionRS assignProjectUsers(String projectName, AssignUsersRQ assignUsersRQ) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/project/" + projectName + "/assign", assignUsersRQ, OperationCompletionRS.class);
	}

	@Override
	public DashboardResource addFavoriteResource(AddFavoriteResourceRQ favoriteResource) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/favorites", favoriteResource, DashboardResource.class);
	}

	@Override
	public List<ActivityResource> getAllTestItemActivities(String testItemId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/activity/item/" + testItemId, new TypeToken<ArrayList<ActivityResource>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	@Override
	public List<Issue> defineItemIssue(DefineIssueRQ defineIssueRQ) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/item", defineIssueRQ, new TypeToken<ArrayList<Issue>>() {
			private static final long serialVersionUID = 1L;
		}.getType());
	}

	@Override
	public Ticket createTicket(String externalSystemName, PostTicketRQ postTicketRQ) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/external-system/" + externalSystemName + "/ticket", postTicketRQ, Ticket.class);
	}

	@Override
	public Ticket getTicket(String externalSystemName, String ticketId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/external-system/" + externalSystemName + "/ticket/" + ticketId, Ticket.class);
	}

	@Override
	public LaunchResource mergeLaunches(String projectName, MergeLaunchesRQ mergeLaunchesRQ) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/launch/merge", mergeLaunchesRQ, LaunchResource.class);
	}

	@Override
	public EntryCreatedRS createExternalSystem(CreateExternalSystemRQ createRQ, String projectName) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + projectName + "/external-system", createRQ, EntryCreatedRS.class);
	}

	@Override
	public ExternalSystemResource getExternalSystem(String projectName, String systemId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + projectName + "/external-system/" + systemId, ExternalSystemResource.class);
	}

	@Override
	public OperationCompletionRS deleteExternalSystem(String projectName, String systemId) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + projectName + "/external-system/" + systemId, OperationCompletionRS.class);
	}

	@Override
	public OperationCompletionRS deleteAllExternalSystems(String projectName) throws RestEndpointIOException {
		return endpoint.delete(apiBase + "/" + projectName + "/external-system/clear", OperationCompletionRS.class);
	}

	@Override
	public OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ rq, String project, String systemId)
			throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/external-system/" + systemId, rq, OperationCompletionRS.class);
	}

	@Override
	public OperationCompletionRS checkConnection(String project, String systemId, UpdateExternalSystemRQ rq)
			throws RestEndpointIOException {
		return endpoint.put(apiBase + "/" + project + "/external-system/" + systemId + "/connect", rq, OperationCompletionRS.class);
	}

	@Override
	public Ticket createTicket(PostTicketRQ ticketRQ, String project, String systemId) throws RestEndpointIOException {
		return endpoint.post(apiBase + "/" + project + "/external-system/" + systemId + "/ticket", ticketRQ, Ticket.class);
	}

	@Override
	public Ticket getTicket(String ticketId, String project, String systemId) throws RestEndpointIOException {
		return endpoint.get(apiBase + "/" + project + "/external-system/" + systemId + "/ticket/" + ticketId, Ticket.class);
	}

	@Override
	public Map<String, List<ChartObject>> compareLaunches(String projectName, String[] ids) throws RestEndpointIOException {
		final Map<String, String> parameters = new HashMap<String, String>();
		StringBuilder joinedIds = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			joinedIds.append(ids[i]);
			if (i < ids.length - 1)
				joinedIds.append(",");
		}
		parameters.put("ids", joinedIds.toString());
		return endpoint.get(apiBase + "/" + projectName + "/launch/compare", parameters, new TypeToken<Map<String, List<ChartObject>>>() {
		}.getType());
	}

	@Override
	public OperationCompletionRS updateProjectEmailConfig(String project, UpdateProjectRQ rq) throws RestEndpointIOException {
		return endpoint.put(apiBase + "/project/" + project + "/emailconfig", rq, OperationCompletionRS.class);
	}
}
