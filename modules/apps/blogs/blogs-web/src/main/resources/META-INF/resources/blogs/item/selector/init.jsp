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

<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.blogs.web.internal.display.context.BlogEntriesItemSelectorDisplayContext" %><%@
page import="com.liferay.blogs.web.internal.display.context.BlogEntriesItemSelectorManagementToolbarDisplayContext" %><%@
page import="com.liferay.blogs.web.internal.servlet.taglib.clay.BlogsEntryItemSelectorVerticalCard" %><%@
page import="com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType" %><%@
page import="com.liferay.portal.kernel.json.JSONObject" %><%@
page import="com.liferay.portal.kernel.json.JSONUtil" %>

<%@ page import="java.util.Objects" %>

<%@ include file="/blogs_admin/init-ext.jsp" %>