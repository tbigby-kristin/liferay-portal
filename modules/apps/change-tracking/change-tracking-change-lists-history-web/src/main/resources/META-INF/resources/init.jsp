<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/clay" prefix="clay" %><%@
taglib uri="http://liferay.com/tld/site-navigation" prefix="liferay-site-navigation" %><%@
taglib uri="http://liferay.com/tld/soy" prefix="soy" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.change.tracking.change.lists.history.web.internal.constants.CTHistoryConstants" %><%@
page import="com.liferay.change.tracking.change.lists.history.web.internal.display.context.ChangeListsHistoryDetailsDisplayContext" %><%@
page import="com.liferay.change.tracking.change.lists.history.web.internal.display.context.ChangeListsHistoryDisplayContext" %><%@
page import="com.liferay.change.tracking.constants.CTWebKeys" %><%@
page import="com.liferay.change.tracking.model.CTCollection" %><%@
page import="com.liferay.change.tracking.model.CTEntry" %><%@
page import="com.liferay.petra.string.StringPool" %><%@
page import="com.liferay.portal.kernel.dao.search.SearchContainer" %><%@
page import="com.liferay.portal.kernel.security.permission.ResourceActionsUtil" %><%@
page import="com.liferay.portal.kernel.util.HtmlUtil" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %><%@
page import="com.liferay.portal.kernel.util.PortalUtil" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
portletDisplay.setShowStagingIcon(false);

ChangeListsHistoryDisplayContext changeListsHistoryDisplayContext = (ChangeListsHistoryDisplayContext)request.getAttribute(CTHistoryConstants.CHANGE_LISTS_HISTORY_DISPLAY_CONTEXT);
%>

<%@ include file="/init-ext.jsp" %>