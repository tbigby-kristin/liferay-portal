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

import com.liferay.fragment.constants.FragmentEntryLinkConstants;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.processor.FragmentEntryProcessorRegistry;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.fragment.renderer.FragmentPortletRenderer;
import com.liferay.fragment.renderer.FragmentRendererController;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructure;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructureUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.PortletIdException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.segments.util.SegmentsExperiencePortletUtil;

import java.util.concurrent.Callable;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
		"mvc.command.name=/content_layout/add_portlet_react"
	},
	service = MVCActionCommand.class
)
public class AddPortletReactMVCActionCommand extends BaseMVCActionCommand {

	protected JSONObject addFragmentEntryLinkToLayoutData(
			ActionRequest actionRequest, FragmentEntryLink fragmentEntryLink)
		throws PortalException {

		String itemConfig = ParamUtil.getString(actionRequest, "config");
		String parentItemId = ParamUtil.getString(actionRequest, "parentId");
		String itemType = ParamUtil.getString(actionRequest, "type");
		int position = ParamUtil.getInteger(actionRequest, "position");

		return LayoutStructureUtil.updateLayoutPageTemplateData(
			actionRequest,
			layoutStructure -> {
				LayoutStructure.Item item = LayoutStructure.Item.create(
					JSONFactoryUtil.createJSONObject(itemConfig),
					String.valueOf(fragmentEntryLink.getFragmentEntryLinkId()),
					parentItemId, itemType);

				layoutStructure.addItem(item, parentItemId, position);
			});
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		JSONObject jsonObject = _processAddPortletAction(
			actionRequest, actionResponse);

		hideDefaultSuccessMessage(actionRequest);

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	protected JSONObject processAddPortlet(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long classPK = ParamUtil.getLong(actionRequest, "classPK");

		Layout layout = _layoutLocalService.getLayout(classPK);

		String portletId = PortletIdCodec.decodePortletName(
			ParamUtil.getString(actionRequest, "portletId"));

		PortletPermissionUtil.check(
			themeDisplay.getPermissionChecker(), layout.getGroupId(), layout,
			portletId, ActionKeys.ADD_TO_PAGE);

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			actionRequest);

		long classNameId = ParamUtil.getLong(actionRequest, "classNameId");

		String instanceId = _getPortletInstanceId(
			layout, portletId,
			ParamUtil.getLong(actionRequest, "segmentsExperienceId"));

		String html = _getPortletFragmentEntryLinkHTML(
			serviceContext.getRequest(),
			_portal.getHttpServletResponse(actionResponse), portletId,
			instanceId);

		JSONObject editableValueJSONObject =
			_fragmentEntryProcessorRegistry.getDefaultEditableValuesJSONObject(
				html, StringPool.BLANK);

		editableValueJSONObject.put(
			"instanceId", instanceId
		).put(
			"portletId", portletId
		);

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.addFragmentEntryLink(
				serviceContext.getUserId(), serviceContext.getScopeGroupId(), 0,
				0, classNameId, classPK, StringPool.BLANK, html,
				StringPool.BLANK, StringPool.BLANK,
				editableValueJSONObject.toString(), StringPool.BLANK, 0, null,
				serviceContext);

		DefaultFragmentRendererContext defaultFragmentRendererContext =
			new DefaultFragmentRendererContext(fragmentEntryLink);

		defaultFragmentRendererContext.setMode(FragmentEntryLinkConstants.EDIT);

		JSONObject jsonObject = JSONUtil.put(
			"content",
			_fragmentRendererController.render(
				defaultFragmentRendererContext,
				_portal.getHttpServletRequest(actionRequest),
				_portal.getHttpServletResponse(actionResponse))
		).put(
			"editableValues", fragmentEntryLink.getEditableValues()
		).put(
			"fragmentEntryLinkId", fragmentEntryLink.getFragmentEntryLinkId()
		).put(
			"name", _portal.getPortletTitle(portletId, themeDisplay.getLocale())
		);

		JSONObject layoutDataJSONObject = addFragmentEntryLinkToLayoutData(
			actionRequest, fragmentEntryLink);

		return JSONUtil.put(
			"fragmentEntryLink", jsonObject
		).put(
			"layoutData", layoutDataJSONObject
		);
	}

	private String _getPortletFragmentEntryLinkHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String portletId,
			String instanceId)
		throws Exception {

		return _fragmentPortletRenderer.renderPortlet(
			httpServletRequest, httpServletResponse, portletId, instanceId,
			StringPool.BLANK);
	}

	private String _getPortletInstanceId(
			Layout layout, String portletId, long segmentsExperienceId)
		throws PortletIdException {

		Portlet portlet = _portletLocalService.getPortletById(portletId);

		if (portlet.isInstanceable()) {
			return SegmentsExperiencePortletUtil.setSegmentsExperienceId(
				PortletIdCodec.generateInstanceId(), segmentsExperienceId);
		}

		String instanceId =
			SegmentsExperiencePortletUtil.setSegmentsExperienceId(
				String.valueOf(CharPool.NUMBER_0), segmentsExperienceId);

		String checkPortletId =
			SegmentsExperiencePortletUtil.setSegmentsExperienceId(
				PortletIdCodec.encode(portletId, instanceId),
				segmentsExperienceId);

		long count = _portletPreferencesLocalService.getPortletPreferencesCount(
			PortletKeys.PREFS_OWNER_TYPE_LAYOUT, layout.getPlid(),
			checkPortletId);

		if (count > 0) {
			throw new PortletIdException(
				"Unable to add uninstanceable portlet more than once");
		}

		return instanceId;
	}

	private JSONObject _processAddPortletAction(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		Callable<JSONObject> callable = new AddPortletLinkCallable(
			actionRequest, actionResponse);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {
			jsonObject = TransactionInvokerUtil.invoke(
				_transactionConfig, callable);

			if (SessionErrors.contains(
					actionRequest, "fragmentEntryContentInvalid")) {

				jsonObject.put("error", true);
			}

			SessionMessages.add(actionRequest, "fragmentEntryLinkAdded");
		}
		catch (Throwable t) {
			if (_log.isWarnEnabled()) {
				_log.warn(t, t);
			}

			String errorMessage = "an-unexpected-error-occurred";

			jsonObject.put(
				"error",
				LanguageUtil.get(
					_portal.getHttpServletRequest(actionRequest),
					errorMessage));
		}

		return jsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AddPortletReactMVCActionCommand.class);

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {Exception.class});

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private FragmentEntryProcessorRegistry _fragmentEntryProcessorRegistry;

	@Reference
	private FragmentPortletRenderer _fragmentPortletRenderer;

	@Reference
	private FragmentRendererController _fragmentRendererController;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private PortletLocalService _portletLocalService;

	@Reference
	private PortletPreferencesLocalService _portletPreferencesLocalService;

	private class AddPortletLinkCallable implements Callable<JSONObject> {

		@Override
		public JSONObject call() throws Exception {
			return processAddPortlet(_actionRequest, _actionResponse);
		}

		private AddPortletLinkCallable(
			ActionRequest actionRequest, ActionResponse actionResponse) {

			_actionRequest = actionRequest;
			_actionResponse = actionResponse;
		}

		private final ActionRequest _actionRequest;
		private final ActionResponse _actionResponse;

	}

}