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

import java.util.List;
import java.util.Map;

import org.springframework.hateoas.PagedResources;

import com.epam.reportportal.utils.queue.Result;
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
import com.epam.ta.reportportal.ws.model.launch.*;
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
import com.epam.reportportal.restclient.endpoint.exception.RestEndpointIOException;

/**
 * ReportPortal Reporter. Set of methods for reporting test data to the report
 * portal
 * 
 * @author Andrei Varabyeu
 * 
 */

public interface IReportPortalService {

	/**
	 * Starts launch in ReportPortal
	 * 
	 * @param rq
	 *            - Launch Data
	 * @return - Response from ReportPortal
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS startLaunch(StartLaunchRQ rq) throws RestEndpointIOException;

	/**
	 * Finishes launch in ReportPortal
	 * 
	 * @param launchID
	 * @param rq
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS finishLaunch(String launchID, FinishExecutionRQ rq) throws RestEndpointIOException;

	/**
	 * Update {@link Mode} for Launch with specified id
	 * 
	 * @param launchID
	 * @param rq
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS updateLaunch(String launchID, UpdateLaunchRQ rq) throws RestEndpointIOException;

	/**
	 * Starts child test item execution
	 * 
	 * @param rq
	 * @return EntryCreatedRS
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS startTestItem(String parentItemId, StartTestItemRQ rq) throws RestEndpointIOException;

	/**
	 * Finishes test item execution
	 * 
	 * @param rq
	 * @return OperationCompletionRS
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS finishTestItem(String itemId, FinishTestItemRQ rq) throws RestEndpointIOException;

	/**
	 * Starts root test item execution
	 * 
	 * @param rq
	 * @return EntryCreatedRS
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS startRootTestItem(StartTestItemRQ rq) throws RestEndpointIOException;

	/**
	 * Log some data in report portal
	 * 
	 * @param rq
	 * @throws RestEndpointIOException
	 */
	Result<? extends EntryCreatedRS> log(SaveLogRQ rq) throws RestEndpointIOException;

	/**
	 * 
	 * @throws RestEndpointIOException
	 */
	List<EntryCreatedRS> createUserFilter(CollectionsRQ<CreateUserFilterRQ> rq) throws RestEndpointIOException;

	/**
	 * Update {@link UserFilterResource} with specified ID
	 * 
	 * @param filterId
	 * @param rq
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS updateUserFilter(String filterId, UpdateUserFilterRQ rq) throws RestEndpointIOException;

	/**
	 * Finds specified launch in report portal
	 * 
	 * @param launchId
	 * @throws RestEndpointIOException
	 */
	LaunchResource getLaunch(String launchId) throws RestEndpointIOException;

	/**
	 * Finds all launches of specified project
	 * 
	 * @throws RestEndpointIOException
	 */
	PagedResources<LaunchResource> getProjectLaunches() throws RestEndpointIOException;

	/**
	 * Finds all launches of specified project
	 * 
	 * @param parameters
	 * @throws RestEndpointIOException
	 */
	PagedResources<LaunchResource> getLaunches(Map<String, String> parameters) throws RestEndpointIOException;

	/**
	 * get launches for user
	 * 
	 * @param parameters
	 * @throws RestEndpointIOException
	 */
	PagedResources<LaunchResource> getUserLaunches(String user, Map<String, String> parameters) throws RestEndpointIOException;

	/**
	 * Find test item
	 * 
	 * @param itemId
	 * @throws RestEndpointIOException
	 */
	TestItemResource getTestItem(String itemId) throws RestEndpointIOException;

	/**
	 * Get all test items
	 * 
	 * @throws RestEndpointIOException
	 */
	PagedResources<TestItemResource> getProjectTestItems() throws RestEndpointIOException;

	/**
	 * Get all test items
	 * 
	 * @param parameters
	 * @throws RestEndpointIOException
	 */
	PagedResources<TestItemResource> getAllTestItems(Map<String, String> parameters) throws RestEndpointIOException;

	/**
	 * Get history from test item.
	 * 
	 * There are following parameters can be used in request:<br>
	 * <li>ids - ids of testitems whose history has been requested. Several ids
	 * have to separate by comma.</li>
	 * <li>history_depth - number of returned test items. It returns from the
	 * last created test item.</li>
	 * 
	 * @param parameters
	 *            - http request parameters
	 * @throws RestEndpointIOException
	 */
	List<TestItemHistoryElement> getTestItemsHistory(Map<String, String> parameters) throws RestEndpointIOException;

	/**
	 * get log by its Id
	 * 
	 * @param logId
	 * @throws RestEndpointIOException
	 */
	LogResource getLog(String logId) throws RestEndpointIOException;

	/**
	 * Get all logs
	 * 
	 * @throws RestEndpointIOException
	 */
	PagedResources<LogResource> getAllLogs(String testStepId) throws RestEndpointIOException;

	/**
	 * Get all logs
	 * 
	 * @param testStepId
	 * @param parameters
	 * @throws RestEndpointIOException
	 */
	PagedResources<LogResource> getAllLogs(String testStepId, Map<String, String> parameters) throws RestEndpointIOException;

	/**
	 * get user filter by Id
	 * 
	 * @param userFilterId
	 * @throws RestEndpointIOException
	 */
	UserFilterResource getUserFilter(String userFilterId) throws RestEndpointIOException;

	/**
	 * Get all user filters
	 * 
	 * @throws RestEndpointIOException
	 */
	PagedResources<UserFilterResource> getProjectUserFilters() throws RestEndpointIOException;

	/**
	 * Get all user filters
	 * 
	 * 
	 * @param parameters
	 * @throws RestEndpointIOException
	 */
	PagedResources<UserFilterResource> getAllUserFilters(Map<String, String> parameters) throws RestEndpointIOException;

	/**
	 * Get user filters for project
	 * 
	 * @throws RestEndpointIOException
	 */
	Map<String, SharedEntity> getUserFilters() throws RestEndpointIOException;

	/**
	 * Get shared filters for project
	 * 
	 * @throws RestEndpointIOException
	 */
	Map<String, SharedEntity> getSharedFilters() throws RestEndpointIOException;

	/**
	 * Delete item
	 * 
	 * @param itemId
	 * @throws RestEndpointIOException
	 */

	OperationCompletionRS deleteTestItem(String itemId) throws RestEndpointIOException;

	/**
	 * Delete launch
	 * 
	 * @param launchId
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteLaunch(String launchId) throws RestEndpointIOException;

	/**
	 * Delete log
	 * 
	 * @param logId
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteLog(String logId) throws RestEndpointIOException;

	/**
	 * Delete user filter
	 * 
	 * @param userFilterId
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteUserFilter(String userFilterId) throws RestEndpointIOException;

	/**
	 * define type of bug
	 * 
	 * @param request
	 * @throws RestEndpointIOException
	 */
	List<Issue> defineTestItemIssueType(DefineIssueRQ request) throws RestEndpointIOException;

	/**
	 * @param createWidgetRQ
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS createWidget(WidgetRQ createWidgetRQ) throws RestEndpointIOException;

	/**
	 * @param widgetId
	 * @throws RestEndpointIOException
	 */
	WidgetResource getWidget(String widgetId) throws RestEndpointIOException;

	/**
	 * @throws RestEndpointIOException
	 */
	Map<String, SharedEntity> getSharedWidgets() throws RestEndpointIOException;

	/**
	 * @param widgetId
	 * @throws RestEndpointIOException
	 */
	Iterable<Iterable<String>> getChartData(String widgetId) throws RestEndpointIOException;

	/**
	 * @param widgetId
	 * @param updateRQ
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS updateWidget(String widgetId, WidgetRQ updateRQ) throws RestEndpointIOException;

	/**
	 * 
	 * @param createDashboardRQ
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS createDashboard(CreateDashboardRQ createDashboardRQ) throws RestEndpointIOException;

	/**
	 * 
	 * @throws RestEndpointIOException
	 */
	List<DashboardResource> getProjectDashboards() throws RestEndpointIOException;

	/**
	 * 
	 * @param dashboardID
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteDashboard(String dashboardID) throws RestEndpointIOException;

	/**
	 * 
	 * @param dashboardID
	 * @param updateDashboardRQ
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS updateDashboard(String dashboardID, UpdateDashboardRQ updateDashboardRQ) throws RestEndpointIOException;

	/**
	 * @return shared dashboards on the project and they owners
	 * @throws RestEndpointIOException
	 */
	Map<String, SharedEntity> getSharedDashboards() throws RestEndpointIOException;

	/**
	 * get user info
	 * 
	 * @param userName
	 * @throws RestEndpointIOException
	 */
	UserResource getUser(String userName) throws RestEndpointIOException;

	/**
	 * 
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS synchronizeUser() throws RestEndpointIOException;

	/**
	 * 
	 * @param username
	 * @param editUserRQ
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS editUser(String username, EditUserRQ editUserRQ) throws RestEndpointIOException;

	/**
	 * Create new user
	 * 
	 * @param createUserRQ
	 * @throws RestEndpointIOException
	 */
	CreateUserBidRS createUser(CreateUserRQ createUserRQ) throws RestEndpointIOException;

	/**
	 * Set user password, etc.
	 * 
	 * @param registrationUuid
	 * @param rq
	 * @throws RestEndpointIOException
	 */
	CreateUserRS confirmUser(String registrationUuid, CreateUserRQConfirm rq) throws RestEndpointIOException;

	/**
	 * Delete user with specified login
	 * 
	 * @param login
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteUser(String login) throws RestEndpointIOException;

	/**
	 * Returns photo of current logged in user
	 * 
	 * @throws RestEndpointIOException
	 */
	byte[] getMyPhoto() throws RestEndpointIOException;

	/**
	 * 
	 * @param value
	 * @throws RestEndpointIOException
	 */
	List<String> getAllTags(String value) throws RestEndpointIOException;

	/**
	 * 
	 * @throws RestEndpointIOException
	 */
	List<String> getAllTags() throws RestEndpointIOException;

	/**
	 * 
	 * @param createProjectRQ
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS createProject(CreateProjectRQ createProjectRQ) throws RestEndpointIOException;

	/**
	 * 
	 * @param updateProjectRQ
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS updateProject(UpdateProjectRQ updateProjectRQ) throws RestEndpointIOException;

	/**
	 * 
	 * @param project
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteProject(String project) throws RestEndpointIOException;

	/**
	 * 
	 * @param projectName
	 * @param unassignUsersRQ
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS unassignProjectUsers(String projectName, UnassignUsersRQ unassignUsersRQ) throws RestEndpointIOException;

	/**
	 * 
	 * @param projectName
	 * @param assignUsersRQ
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS assignProjectUsers(String projectName, AssignUsersRQ assignUsersRQ) throws RestEndpointIOException;

	/**
	 * Adds a resource to favorites
	 * 
	 * @param favoriteResource
	 *            a resource which will be added to favorites
	 * @throws RestEndpointIOException
	 */
	DashboardResource addFavoriteResource(AddFavoriteResourceRQ favoriteResource) throws RestEndpointIOException;

	/**
	 * Retrieves all activities for the given test item
	 * 
	 * @param testItemId
	 * @throws RestEndpointIOException
	 */
	List<ActivityResource> getAllTestItemActivities(String testItemId) throws RestEndpointIOException;

	/**
	 * Updates issue according to the request
	 * 
	 * @param defineIssueRQ
	 * @throws RestEndpointIOException
	 */
	List<Issue> defineItemIssue(DefineIssueRQ defineIssueRQ) throws RestEndpointIOException;

	/**
	 * Creates a stub Jira ticket
	 *
	 * @param externalSystemName
	 * @param postTicketRQ
	 * @throws RestEndpointIOException
	 */
	Ticket createTicket(String externalSystemName, PostTicketRQ postTicketRQ) throws RestEndpointIOException;

	/**
	 * Gets ticket by id
	 *
	 * @param externalSystemName
	 * @param ticketId
	 * @throws RestEndpointIOException
	 */
	Ticket getTicket(String externalSystemName, String ticketId) throws RestEndpointIOException;

	/**
	 * Merge launches
	 *
	 * @param projectName
	 * @param mergeLaunchesRQ
	 * @throws RestEndpointIOException
	 */
	LaunchResource mergeLaunches(String projectName, MergeLaunchesRQ mergeLaunchesRQ) throws RestEndpointIOException;

	/**
	 * Create external system
	 *
	 * @param createRQ
	 * @param projectName
	 * @throws RestEndpointIOException
	 */
	EntryCreatedRS createExternalSystem(CreateExternalSystemRQ createRQ, String projectName) throws RestEndpointIOException;

	/**
	 * Get external system
	 *
	 * @param projectName
	 * @param systemId
	 * @throws RestEndpointIOException
	 */
	ExternalSystemResource getExternalSystem(String projectName, String systemId) throws RestEndpointIOException;

	/**
	 * Delete external system
	 *
	 * @param projectName
	 * @param systemId
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteExternalSystem(String projectName, String systemId) throws RestEndpointIOException;

	/**
	 * Delete all external systems
	 *
	 * @param projectName
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS deleteAllExternalSystems(String projectName) throws RestEndpointIOException;

	/**
	 * Update external system
	 *
	 * @param rq
	 * @param project
	 * @param systemId
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ rq, String project, String systemId) throws RestEndpointIOException;

	/**
	 * Check connection to external system
	 *
	 * @param project
	 * @param systemId
	 * @param rq
	 * @throws RestEndpointIOException
	 */
	OperationCompletionRS checkConnection(String project, String systemId, UpdateExternalSystemRQ rq) throws RestEndpointIOException;

	/**
	 * Create ticket
	 *
	 * @param ticketRQ
	 * @param project
	 * @param systemId
	 * @throws RestEndpointIOException
	 */
	Ticket createTicket(PostTicketRQ ticketRQ, String project, String systemId) throws RestEndpointIOException;

	/**
	 * Get ticket by id
	 *
	 * @param ticketId
	 * @param project
	 * @param systemId
	 * @throws RestEndpointIOException
	 */
	Ticket getTicket(String ticketId, String project, String systemId) throws RestEndpointIOException;

	Map<String, List<ChartObject>> compareLaunches(String projectName, String[] ids) throws RestEndpointIOException;

	OperationCompletionRS updateProjectEmailConfig(String project, UpdateProjectRQ rq) throws RestEndpointIOException;
}
