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

package com.liferay.depot.web.internal.display.context;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.model.DepotEntryGroupRel;
import com.liferay.depot.service.DepotEntryGroupRelLocalServiceUtil;
import com.liferay.depot.web.internal.constants.DepotAdminWebKeys;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.item.selector.criteria.URLItemSelectorReturnType;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.service.GroupServiceUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.site.item.selector.criterion.SiteItemSelectorCriterion;

import java.util.List;
import java.util.Locale;

import javax.portlet.PortletURL;

/**
 * @author Cristina González
 */
public class DepotAdminSitesDisplayContext {

	public DepotAdminSitesDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse) {

		_liferayPortletRequest = liferayPortletRequest;
		_liferayPortletResponse = liferayPortletResponse;
	}

	public List<DepotEntryGroupRel> getDepotEntryGroupRels() {
		DepotEntry depotEntry = (DepotEntry)_liferayPortletRequest.getAttribute(
			DepotAdminWebKeys.DEPOT_ENTRY);

		return DepotEntryGroupRelLocalServiceUtil.getDepotEntryGroupRels(
			depotEntry);
	}

	public PortletURL getItemSelectorURL() {
		ItemSelector itemSelector =
			(ItemSelector)_liferayPortletRequest.getAttribute(
				DepotAdminWebKeys.ITEM_SELECTOR);

		ItemSelectorCriterion itemSelectorCriterion =
			new SiteItemSelectorCriterion();

		itemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new URLItemSelectorReturnType());

		return itemSelector.getItemSelectorURL(
			RequestBackedPortletURLFactoryUtil.create(_liferayPortletRequest),
			_liferayPortletResponse.getNamespace() + "selectSite",
			itemSelectorCriterion);
	}

	public String getSiteName(DepotEntryGroupRel depotEntryGroupRel)
		throws PortalException {

		Locale locale = LocaleUtil.fromLanguageId(
			LanguageUtil.getLanguageId(_liferayPortletRequest));

		Group group = GroupServiceUtil.getGroup(
			depotEntryGroupRel.getToGroupId());

		return group.getDescriptiveName(locale);
	}

	private final LiferayPortletRequest _liferayPortletRequest;
	private final LiferayPortletResponse _liferayPortletResponse;

}