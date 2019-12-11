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

package com.liferay.depot.web.internal.portlet.action;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.depot.web.internal.constants.DepotPortletKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.GroupService;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropertiesParamUtil;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	property = {
		"javax.portlet.name=" + DepotPortletKeys.DEPOT_ADMIN,
		"mvc.command.name=/depot_entry/edit"
	},
	service = MVCActionCommand.class
)
public class EditDepotEntryMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long depotEntryId = ParamUtil.getLong(actionRequest, "depotEntryId");

		DepotEntry depotEntry = _depotEntryLocalService.getDepotEntry(
			depotEntryId);

		Group group = _groupService.getGroup(depotEntry.getGroupId());

		try {
			_depotEntryLocalService.updateDepotEntry(
				depotEntryId,
				LocalizationUtil.getLocalizationMap(
					actionRequest, "name", group.getNameMap()),
				LocalizationUtil.getLocalizationMap(
					actionRequest, "description", group.getDescriptionMap()),
				PropertiesParamUtil.getProperties(
					actionRequest, "TypeSettingsProperties--"),
				ServiceContextFactory.getInstance(
					DepotEntry.class.getName(), actionRequest));
		}
		catch (PortalException pe) {
			_log.error(pe, pe);

			SessionErrors.add(actionRequest, pe.getClass(), pe);

			actionResponse.sendRedirect(
				ParamUtil.getString(actionRequest, "redirect"));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		EditDepotEntryMVCActionCommand.class);

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private GroupService _groupService;

	@Reference
	private Portal _portal;

}