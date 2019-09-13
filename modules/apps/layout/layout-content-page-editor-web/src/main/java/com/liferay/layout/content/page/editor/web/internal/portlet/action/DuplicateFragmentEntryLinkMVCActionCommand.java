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

package com.liferay.layout.content.page.editor.web.internal.portlet.action;

import com.liferay.fragment.contributor.FragmentCollectionContributorTracker;
import com.liferay.fragment.exception.NoSuchEntryLinkException;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererController;
import com.liferay.fragment.renderer.FragmentRendererTracker;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.util.FragmentEntryConfigUtil;
import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
		"mvc.command.name=/content_layout/duplicate_fragment_entry_link"
	},
	service = MVCActionCommand.class
)
public class DuplicateFragmentEntryLinkMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		JSONObject jsonObject = _duplicateFragmentEntryLink(actionRequest);

		hideDefaultSuccessMessage(actionRequest);

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	private JSONObject _duplicateFragmentEntryLink(
		ActionRequest actionRequest) {

		long fragmentEntryLinkId = ParamUtil.getLong(
			actionRequest, "fragmentEntryLinkId");

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {
			FragmentEntryLink fragmentEntryLink =
				_fragmentEntryLinkLocalService.getFragmentEntryLink(
					fragmentEntryLinkId);

			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				actionRequest);

			FragmentEntryLink duplicateFragmentEntryLink =
				_fragmentEntryLinkLocalService.addFragmentEntryLink(
					serviceContext.getUserId(), fragmentEntryLink.getGroupId(),
					fragmentEntryLink.getOriginalFragmentEntryLinkId(),
					fragmentEntryLink.getFragmentEntryId(),
					fragmentEntryLink.getClassNameId(),
					fragmentEntryLink.getClassPK(), fragmentEntryLink.getCss(),
					fragmentEntryLink.getHtml(), fragmentEntryLink.getJs(),
					fragmentEntryLink.getConfiguration(),
					fragmentEntryLink.getEditableValues(),
					fragmentEntryLink.getNamespace(), 0,
					fragmentEntryLink.getRendererKey(), serviceContext);

			DefaultFragmentRendererContext fragmentRendererContext =
				new DefaultFragmentRendererContext(fragmentEntryLink);

			fragmentRendererContext.setLocale(serviceContext.getLocale());

			jsonObject.put(
				"configuration",
				JSONFactoryUtil.createJSONObject(
					_fragmentRendererController.getConfiguration(
						fragmentRendererContext))
			).put(
				"defaultConfigurationValues",
				FragmentEntryConfigUtil.getConfigurationDefaultValuesJSONObject(
					duplicateFragmentEntryLink.getConfiguration())
			).put(
				"editableValues",
				JSONFactoryUtil.createJSONObject(
					duplicateFragmentEntryLink.getEditableValues())
			).put(
				"fragmentEntryLinkId",
				duplicateFragmentEntryLink.getFragmentEntryLinkId()
			);

			FragmentEntry fragmentEntry = _getFragmentEntry(
				fragmentEntryLink.getFragmentEntryId(),
				fragmentEntryLink.getRendererKey(), serviceContext);

			String fragmentEntryKey = null;
			String name = null;

			if (fragmentEntry != null) {
				fragmentEntryKey = fragmentEntry.getFragmentEntryKey();
				name = fragmentEntry.getName();
			}
			else {
				FragmentRenderer fragmentRenderer =
					_fragmentRendererTracker.getFragmentRenderer(
						fragmentEntryLink.getRendererKey());

				fragmentEntryKey = fragmentRenderer.getKey();
				name = fragmentRenderer.getLabel(serviceContext.getLocale());
			}

			jsonObject.put(
				"fragmentEntryKey", fragmentEntryKey
			).put(
				"name", name
			);

			SessionMessages.add(actionRequest, "fragmentEntryLinkDuplicated");
		}
		catch (PortalException pe) {
			String errorMessage = "an-unexpected-error-occurred";

			if (pe instanceof NoSuchEntryLinkException) {
				errorMessage =
					"the-section-could-not-be-duplicated-because-it-has-been-" +
						"deleted";
			}

			ThemeDisplay themeDisplay =
				(ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

			jsonObject.put(
				"error",
				LanguageUtil.get(themeDisplay.getRequest(), errorMessage));
		}

		return jsonObject;
	}

	private FragmentEntry _getContributedFragmentEntry(
		String fragmentEntryKey, Locale locale) {

		Map<String, FragmentEntry> fragmentEntries =
			_fragmentCollectionContributorTracker.getFragmentEntries(locale);

		return fragmentEntries.get(fragmentEntryKey);
	}

	private FragmentEntry _getFragmentEntry(
		long fragmentEntryId, String rendererKey,
		ServiceContext serviceContext) {

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(fragmentEntryId);

		if (fragmentEntry != null) {
			return fragmentEntry;
		}

		return _getContributedFragmentEntry(
			rendererKey, serviceContext.getLocale());
	}

	@Reference
	private FragmentCollectionContributorTracker
		_fragmentCollectionContributorTracker;

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private FragmentRendererController _fragmentRendererController;

	@Reference
	private FragmentRendererTracker _fragmentRendererTracker;

}