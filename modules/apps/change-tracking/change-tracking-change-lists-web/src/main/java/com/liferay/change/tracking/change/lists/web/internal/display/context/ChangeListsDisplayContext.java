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

package com.liferay.change.tracking.change.lists.web.internal.display.context;

import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.change.tracking.constants.CTPortletKeys;
import com.liferay.change.tracking.engine.CTEngineManager;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTEntry;
import com.liferay.change.tracking.model.CTPreferences;
import com.liferay.change.tracking.model.CTProcess;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTEntryLocalService;
import com.liferay.change.tracking.service.CTPreferencesLocalService;
import com.liferay.change.tracking.service.CTProcessLocalService;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemList;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.ViewTypeItemList;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryDefinition;
import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.template.soy.util.SoyContext;
import com.liferay.portal.template.soy.util.SoyContextFactoryUtil;

import java.text.Format;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Máté Thurzó
 */
public class ChangeListsDisplayContext {

	public ChangeListsDisplayContext(
		ClassNameLocalService classNameLocalService,
		CTCollectionLocalService ctCollectionLocalService,
		CTEntryLocalService ctEntryLocalService,
		CTEngineManager ctEngineManager,
		CTPreferencesLocalService ctPreferencesLocalService,
		CTProcessLocalService ctProcessLocalService,
		HttpServletRequest httpServletRequest, RenderRequest renderRequest,
		RenderResponse renderResponse, ResourceActions resourceActions,
		UserLocalService userLocalService) {

		_classNameLocalService = classNameLocalService;
		_ctCollectionLocalService = ctCollectionLocalService;
		_ctEntryLocalService = ctEntryLocalService;
		_ctEngineManager = ctEngineManager;
		_ctPreferencesLocalService = ctPreferencesLocalService;
		_ctProcessLocalService = ctProcessLocalService;
		_httpServletRequest = httpServletRequest;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_resourceActions = resourceActions;
		_userLocalService = userLocalService;

		_themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		CTPreferences ctPreferences =
			_ctPreferencesLocalService.fetchCTPreferences(
				_themeDisplay.getCompanyId(), 0);

		long ctCollectionId = CTConstants.CT_COLLECTION_ID_PRODUCTION;
		boolean confirmationEnabled = false;

		if (ctPreferences != null) {
			ctPreferences = _ctPreferencesLocalService.fetchCTPreferences(
				_themeDisplay.getCompanyId(), _themeDisplay.getUserId());

			if (ctPreferences != null) {
				ctCollectionId = ctPreferences.getCtCollectionId();
				confirmationEnabled = ctPreferences.isConfirmationEnabled();
			}
		}

		_ctCollectionId = ctCollectionId;
		_confirmationEnabled = confirmationEnabled;
	}

	public SoyContext getChangeListsContext() throws Exception {
		SoyContext soyContext = SoyContextFactoryUtil.createSoyContext();

		soyContext.put(
			"activeCTCollection", _getActiveCTCollectionJSONObject()
		).put(
			"changeEntries", _getCTEntriesJSONArray()
		).put(
			"changeListsDropdownMenu", _getChangeListsDropdownMenuJSONArray()
		).put(
			"latestCTProcess", _getLatestCTProcessJSONObject()
		).put(
			"spritemap",
			_themeDisplay.getPathThemeImages() + "/lexicon/icons.svg"
		).put(
			"urlCheckoutProduction",
			getCheckoutURL(
				CTConstants.CT_COLLECTION_ID_PRODUCTION, "work-on-production",
				true)
		).put(
			"urlProductionView", _themeDisplay.getPortalURL()
		);

		PortletURL portletURL = PortletURLFactoryUtil.create(
			_renderRequest, CTPortletKeys.CHANGE_LISTS,
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter("select", "true");

		soyContext.put("urlSelectChangeList", portletURL.toString());

		return soyContext;
	}

	public String getCheckoutURL(
		long ctCollectionId, String confirmationMessageArg,
		boolean translateArg) {

		PortletURL checkoutURL = PortletURLFactoryUtil.create(
			_httpServletRequest, CTPortletKeys.CHANGE_LISTS,
			PortletRequest.ACTION_PHASE);

		checkoutURL.setParameter(
			ActionRequest.ACTION_NAME, "/change_lists/checkout_ct_collection");
		checkoutURL.setParameter(
			"ctCollectionId", String.valueOf(ctCollectionId));

		if (_confirmationEnabled) {
			return StringBundler.concat(
				"javascript:confirm('",
				LanguageUtil.format(
					_httpServletRequest,
					"do-you-want-to-switch-to-x-change-list",
					confirmationMessageArg, translateArg),
				"') && Liferay.Util.navigate('", checkoutURL, "')");
		}

		return StringBundler.concat(
			"javascript:Liferay.Util.navigate('", checkoutURL, "');");
	}

	public String getConfirmationMessage(String ctCollectionName) {
		return LanguageUtil.format(
			_httpServletRequest, "do-you-want-to-switch-to-x-change-list",
			ctCollectionName, true);
	}

	public CreationMenu getCreationMenu() {
		CreationMenu creationMenu = new CreationMenu();

		creationMenu.addDropdownItem(
			dropdownItem -> {
				PortletURL portletURL = PortletURLFactoryUtil.create(
					_httpServletRequest, CTPortletKeys.CHANGE_LISTS,
					PortletRequest.RENDER_PHASE);

				portletURL.setParameter(
					"mvcRenderCommandName", "/change_lists/add_ct_collection");
				portletURL.setParameter(
					"backURL", _themeDisplay.getURLCurrent());

				dropdownItem.setHref(portletURL);

				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "add-change-list"));
			});

		return creationMenu;
	}

	public long getCTCollectionAgeTime(CTCollection ctCollection) {
		if (ctCollection == null) {
			return 0L;
		}

		Date modifiedDate = ctCollection.getModifiedDate();

		return System.currentTimeMillis() - modifiedDate.getTime();
	}

	public Map<Integer, Long> getCTCollectionChangeTypeCounts(
		long ctCollectionId) {

		List<CTEntry> ctEntries = _ctEntryLocalService.getCTCollectionCTEntries(
			ctCollectionId);

		Stream<CTEntry> ctEntriesStream = ctEntries.stream();

		return ctEntriesStream.collect(
			Collectors.groupingBy(
				CTEntry::getChangeType, Collectors.counting()));
	}

	public String getDeleteURL(long ctCollectionId, String name) {
		PortletURL deleteURL = PortletURLFactoryUtil.create(
			_httpServletRequest, CTPortletKeys.CHANGE_LISTS,
			PortletRequest.ACTION_PHASE);

		deleteURL.setParameter(
			ActionRequest.ACTION_NAME, "/change_lists/delete_ct_collection");

		deleteURL.setParameter(
			"ctCollectionId", String.valueOf(ctCollectionId));

		return StringBundler.concat(
			"javascript:confirm('",
			HtmlUtil.escapeJS(
				LanguageUtil.format(
					_httpServletRequest,
					"are-you-sure-you-want-to-delete-x-change-list", name,
					false)),
			"') && Liferay.Util.navigate('", deleteURL, "')");
	}

	public String getDisplayStyle() {
		if (_displayStyle != null) {
			return _displayStyle;
		}

		_displayStyle = ParamUtil.getString(
			_httpServletRequest, "displayStyle", "icon");

		return _displayStyle;
	}

	public List<DropdownItem> getFilterDropdownItems() {
		return new DropdownItemList() {
			{
				addGroup(
					dropdownGroupItem -> {
						dropdownGroupItem.setDropdownItems(
							_getFilterUserDropdownItems());
						dropdownGroupItem.setLabel(
							LanguageUtil.get(
								_httpServletRequest, "filter-by-user"));
					});

				addGroup(
					dropdownGroupItem -> {
						dropdownGroupItem.setDropdownItems(
							_getFilterStatusDropdownItems());
						dropdownGroupItem.setLabel(
							LanguageUtil.get(
								_httpServletRequest, "filter-by-status"));
					});

				addGroup(
					dropdownGroupItem -> {
						dropdownGroupItem.setDropdownItems(
							_getOrderByDropdownItems());
						dropdownGroupItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "order-by"));
					});
			}
		};
	}

	public String getOrderByType() {
		if (_orderByType != null) {
			return _orderByType;
		}

		_orderByType = ParamUtil.getString(
			_httpServletRequest, "orderByType", "asc");

		return _orderByType;
	}

	public CTCollection getProductionCTCollection() {
		Optional<CTCollection> productionCTCollectionOptional =
			_ctEngineManager.getProductionCTCollectionOptional(
				_themeDisplay.getCompanyId());

		return productionCTCollectionOptional.orElse(null);
	}

	public String getPublishURL(long ctCollectionId, String name) {
		PortletURL publishURL = PortletURLFactoryUtil.create(
			_httpServletRequest, CTPortletKeys.CHANGE_LISTS,
			PortletRequest.ACTION_PHASE);

		publishURL.setParameter(
			ActionRequest.ACTION_NAME, "/change_lists/publish_ct_collection");

		publishURL.setParameter(
			"ctCollectionId", String.valueOf(ctCollectionId));
		publishURL.setParameter("name", name);

		return StringBundler.concat(
			"javascript:confirm('",
			HtmlUtil.escapeJS(
				LanguageUtil.format(
					_httpServletRequest,
					"are-you-sure-you-want-to-publish-x-change-list", name,
					false)),
			"') && Liferay.Util.navigate('", publishURL, "')");
	}

	public SearchContainer<CTCollection> getSearchContainer() {
		SearchContainer<CTCollection> searchContainer = new SearchContainer<>(
			_renderRequest, new DisplayTerms(_renderRequest), null,
			SearchContainer.DEFAULT_CUR_PARAM, 0, SearchContainer.DEFAULT_DELTA,
			_getIteratorURL(), null, "there-are-no-change-lists");

		QueryDefinition<CTCollection> queryDefinition = new QueryDefinition<>();

		DisplayTerms displayTerms = searchContainer.getDisplayTerms();

		String keywords = displayTerms.getKeywords();

		queryDefinition.setAttribute("keywords", keywords);

		int count = (int)_ctEngineManager.countByKeywords(
			_themeDisplay.getCompanyId(), queryDefinition);

		List<CTCollection> ctCollections = new ArrayList<>();

		Optional<CTCollection> productionCTCollection =
			_ctEngineManager.getProductionCTCollectionOptional(
				_themeDisplay.getCompanyId());

		if (productionCTCollection.isPresent() && Validator.isNull(keywords)) {
			if (searchContainer.getCur() == 1) {
				ctCollections.add(productionCTCollection.get());
			}

			count += 1;
		}

		if (searchContainer.getEnd() < count) {
			queryDefinition.setEnd(searchContainer.getEnd() - 1);
		}
		else {
			queryDefinition.setEnd(searchContainer.getEnd());
		}

		queryDefinition.setOrderByComparator(
			OrderByComparatorFactoryUtil.create(
				"CTCollection", _getOrderByCol(),
				getOrderByType().equals("asc")));

		if (searchContainer.getStart() > 0) {
			queryDefinition.setStart(searchContainer.getStart() - 1);
		}
		else {
			queryDefinition.setStart(searchContainer.getStart());
		}

		ctCollections.addAll(
			_ctEngineManager.searchByKeywords(
				_themeDisplay.getCompanyId(), queryDefinition));

		searchContainer.setResults(ctCollections);

		searchContainer.setTotal(count);

		return searchContainer;
	}

	public String getSortingURL() {
		PortletURL sortingURL = _getPortletURL();

		sortingURL.setParameter(
			"orderByType",
			Objects.equals(getOrderByType(), "desc") ? "desc" : "asc");

		return sortingURL.toString();
	}

	public String getViewSearchActionURL() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter("mvcRenderCommandName", "/change_lists/view");
		portletURL.setParameter("select", "true");
		portletURL.setParameter("displayStyle", getDisplayStyle());

		return portletURL.toString();
	}

	public List<ViewTypeItem> getViewTypeItems() {
		return new ViewTypeItemList(_getPortletURL(), _displayStyle) {
			{
				addCardViewTypeItem();
				addTableViewTypeItem();
			}
		};
	}

	public boolean hasCTEntries(long ctCollectionId) {
		QueryDefinition<CTEntry> queryDefinition = new QueryDefinition<>();

		queryDefinition.setStatus(WorkflowConstants.STATUS_DRAFT);

		int ctEntriesCount = _ctEngineManager.getCTEntriesCount(
			ctCollectionId, queryDefinition);

		if (ctEntriesCount > 0) {
			return true;
		}

		return false;
	}

	public boolean isChangeListActive(long ctCollectionId) {
		long recentCTCollectionId = _ctEngineManager.getRecentCTCollectionId(
			_themeDisplay.getUserId());

		if (recentCTCollectionId == ctCollectionId) {
			return true;
		}

		return false;
	}

	public boolean isCheckoutCtCollectionConfirmationEnabled() {
		return _confirmationEnabled;
	}

	private JSONObject _getActiveCTCollectionJSONObject() throws Exception {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		if (_ctCollectionId <= 0) {
			return jsonObject;
		}

		CTCollection ctCollection = _ctCollectionLocalService.getCTCollection(
			_ctCollectionId);
		Map<Integer, Long> ctCollectionChangeTypeCounts =
			getCTCollectionChangeTypeCounts(_ctCollectionId);

		return jsonObject.put(
			"additionCount",
			ctCollectionChangeTypeCounts.getOrDefault(
				CTConstants.CT_CHANGE_TYPE_ADDITION, 0L)
		).put(
			"deletionCount",
			ctCollectionChangeTypeCounts.getOrDefault(
				CTConstants.CT_CHANGE_TYPE_DELETION, 0L)
		).put(
			"description", ctCollection.getDescription()
		).put(
			"deleteURL", getDeleteURL(_ctCollectionId, ctCollection.getName())
		).put(
			"name", ctCollection.getName()
		).put(
			"modifiedCount",
			ctCollectionChangeTypeCounts.getOrDefault(
				CTConstants.CT_CHANGE_TYPE_MODIFICATION, 0L)
		).put(
			"publishURL", getPublishURL(_ctCollectionId, ctCollection.getName())
		);
	}

	private JSONArray _getChangeListsDropdownMenuJSONArray() {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		List<CTCollection> ctCollections =
			_ctCollectionLocalService.getCTCollections(
				_themeDisplay.getCompanyId(), WorkflowConstants.STATUS_DRAFT, 0,
				6, _modifiedDateDescendingOrderByComparator);

		for (CTCollection ctCollection : ctCollections) {
			if (ctCollection.getCtCollectionId() != _ctCollectionId) {
				JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

				jsonArray.put(
					jsonObject.put(
						"checkoutURL",
						getCheckoutURL(
							ctCollection.getCtCollectionId(),
							ctCollection.getName(), false)
					).put(
						"label", ctCollection.getName()
					));
			}

			if (jsonArray.length() == 5) {
				return jsonArray;
			}
		}

		return jsonArray;
	}

	private JSONArray _getCTEntriesJSONArray() throws Exception {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		if (_ctCollectionId != CTConstants.CT_COLLECTION_ID_PRODUCTION) {
			for (CTEntry ctEntry :
					_ctEntryLocalService.getCTCollectionCTEntries(
						_ctCollectionId)) {

				jsonArray.put(_getCTEntryJSONObject(ctEntry));
			}
		}

		return jsonArray;
	}

	private JSONObject _getCTEntryJSONObject(CTEntry ctEntry) throws Exception {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String changeTypeKey = "added";

		if (ctEntry.getChangeType() == 1) {
			changeTypeKey = "deleted";
		}
		else if (ctEntry.getChangeType() == 2) {
			changeTypeKey = "modified";
		}

		Format format = FastDateFormatFactoryUtil.getDateTime(
			_themeDisplay.getLocale());

		ClassName className = _classNameLocalService.getClassName(
			ctEntry.getModelClassNameId());

		return jsonObject.put(
			"changeType",
			LanguageUtil.get(_themeDisplay.getLocale(), changeTypeKey)
		).put(
			"contentType",
			_resourceActions.getModelResource(
				_httpServletRequest, className.getClassName())
		).put(
			"lastEdited", format.format(ctEntry.getModifiedDate())
		).put(
			"userName", ctEntry.getUserName()
		);
	}

	private String _getFilterByStatus() {
		if (_filterByStatus != null) {
			return _filterByStatus;
		}

		_filterByStatus = ParamUtil.getString(
			_httpServletRequest, "status", "all");

		return _filterByStatus;
	}

	private List<DropdownItem> _getFilterStatusDropdownItems() {
		return new DropdownItemList() {
			{
				add(
					dropdownItem -> {
						dropdownItem.setActive(
							Objects.equals(_getFilterByStatus(), "all"));
						dropdownItem.setHref(_getPortletURL(), "status", "all");
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "all"));
					});
				add(
					dropdownItem -> {
						dropdownItem.setActive(
							Objects.equals(_getFilterByStatus(), "active"));
						dropdownItem.setHref(
							_getPortletURL(), "status", "active");
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "active"));
					});
			}
		};
	}

	private List<DropdownItem> _getFilterUserDropdownItems() {
		return new DropdownItemList() {
			{
				add(
					dropdownItem -> {
						dropdownItem.setHref(_getPortletURL());
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "all"));
					});
			}
		};
	}

	private PortletURL _getIteratorURL() {
		PortletURL currentURL = PortletURLUtil.getCurrent(
			_renderRequest, _renderResponse);

		PortletURL iteratorURL = _renderResponse.createRenderURL();

		iteratorURL.setParameter("mvcPath", "/view.jsp");
		iteratorURL.setParameter("redirect", currentURL.toString());
		iteratorURL.setParameter("displayStyle", getDisplayStyle());
		iteratorURL.setParameter("select", "true");

		return iteratorURL;
	}

	private JSONObject _getLatestCTProcessJSONObject() throws PortalException {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		CTProcess ctProcess = _ctProcessLocalService.fetchLatestCTProcess(
			_themeDisplay.getCompanyId());

		if (ctProcess == null) {
			return jsonObject;
		}

		CTCollection ctCollection = _ctCollectionLocalService.getCTCollection(
			ctProcess.getCtCollectionId());
		Format format = FastDateFormatFactoryUtil.getDateTime(
			_themeDisplay.getLocale());
		User user = _userLocalService.getUser(ctProcess.getUserId());

		return jsonObject.put(
			"description", ctCollection.getDescription()
		).put(
			"dateTime", format.format(ctProcess.getCreateDate())
		).put(
			"name", ctCollection.getName()
		).put(
			"userInitials", user.getInitials()
		).put(
			"userName", user.getFullName()
		).put(
			"userPortraitURL", user.getPortraitURL(_themeDisplay)
		);
	}

	private String _getOrderByCol() {
		if (_orderByCol != null) {
			return _orderByCol;
		}

		_orderByCol = ParamUtil.getString(
			_httpServletRequest, "orderByCol", "name");

		return _orderByCol;
	}

	private List<DropdownItem> _getOrderByDropdownItems() {
		return new DropdownItemList() {
			{
				add(
					dropdownItem -> {
						dropdownItem.setActive(
							Objects.equals(_getOrderByCol(), "modifiedDate"));
						dropdownItem.setHref(
							_getPortletURL(), "orderByCol", "modifiedDate");
						dropdownItem.setLabel(
							LanguageUtil.get(
								_httpServletRequest, "modified-date"));
					});
				add(
					dropdownItem -> {
						dropdownItem.setActive(
							Objects.equals(_getOrderByCol(), "name"));
						dropdownItem.setHref(
							_getPortletURL(), "orderByCol", "name");
						dropdownItem.setLabel(
							LanguageUtil.get(_httpServletRequest, "name"));
					});
			}
		};
	}

	private PortletURL _getPortletURL() {
		PortletURL portletURL = PortletURLFactoryUtil.create(
			_renderRequest, CTPortletKeys.CHANGE_LISTS,
			PortletRequest.RENDER_PHASE);

		String displayStyle = ParamUtil.getString(
			_httpServletRequest, "displayStyle");

		if (Validator.isNotNull(displayStyle)) {
			portletURL.setParameter("displayStyle", getDisplayStyle());
		}

		String orderByCol = _getOrderByCol();

		if (Validator.isNotNull(orderByCol)) {
			portletURL.setParameter("orderByCol", orderByCol);
		}

		String orderByType = getOrderByType();

		if (Validator.isNotNull(orderByType)) {
			portletURL.setParameter("orderByType", orderByType);
		}

		portletURL.setParameter("select", "true");

		return portletURL;
	}

	private static final OrderByComparator<CTCollection>
		_modifiedDateDescendingOrderByComparator =
			OrderByComparatorFactoryUtil.create(
				"CTCollection", "modifiedDate", false);

	private final ClassNameLocalService _classNameLocalService;
	private final boolean _confirmationEnabled;
	private final long _ctCollectionId;
	private final CTCollectionLocalService _ctCollectionLocalService;
	private final CTEngineManager _ctEngineManager;
	private final CTEntryLocalService _ctEntryLocalService;
	private final CTPreferencesLocalService _ctPreferencesLocalService;
	private final CTProcessLocalService _ctProcessLocalService;
	private String _displayStyle;
	private String _filterByStatus;
	private final HttpServletRequest _httpServletRequest;
	private String _orderByCol;
	private String _orderByType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ResourceActions _resourceActions;
	private final ThemeDisplay _themeDisplay;
	private final UserLocalService _userLocalService;

}