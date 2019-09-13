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

package com.liferay.app.builder.rest.internal.resource.v1_0;

import com.liferay.app.builder.constants.AppBuilderAppConstants;
import com.liferay.app.builder.exception.AppBuilderAppStatusException;
import com.liferay.app.builder.model.AppBuilderApp;
import com.liferay.app.builder.rest.dto.v1_0.App;
import com.liferay.app.builder.rest.internal.jaxrs.exception.InvalidAppException;
import com.liferay.app.builder.rest.internal.jaxrs.exception.NoSuchDataListViewException;
import com.liferay.app.builder.rest.internal.odata.entity.v1_0.AppBuilderAppEntityModel;
import com.liferay.app.builder.rest.internal.resource.v1_0.util.LocalizedValueUtil;
import com.liferay.app.builder.rest.resource.v1_0.AppResource;
import com.liferay.app.builder.service.AppBuilderAppLocalService;
import com.liferay.app.builder.util.comparator.AppBuilderAppCreateDateComparator;
import com.liferay.app.builder.util.comparator.AppBuilderAppModifiedDateComparator;
import com.liferay.app.builder.util.comparator.AppBuilderAppNameComparator;
import com.liferay.data.engine.rest.client.resource.v1_0.DataListViewResource;
import com.liferay.dynamic.data.mapping.exception.NoSuchStructureLayoutException;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureLayout;
import com.liferay.dynamic.data.mapping.service.DDMStructureLayoutLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.auth.AuthTokenUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.CookieKeys;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.resource.EntityModelResource;
import com.liferay.portal.vulcan.util.SearchUtil;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Gabriel Albuquerque
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/app.properties",
	scope = ServiceScope.PROTOTYPE, service = AppResource.class
)
public class AppResourceImpl
	extends BaseAppResourceImpl implements EntityModelResource {

	@Override
	public void deleteApp(Long appId) throws Exception {
		_appBuilderAppLocalService.deleteAppBuilderApp(appId);
	}

	@Override
	public App getApp(Long appId) throws Exception {
		return _toApp(_appBuilderAppLocalService.getAppBuilderApp(appId));
	}

	public Page<App> getDataDefinitionAppsPage(
			Long dataDefinitionId, String keywords, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		if (pagination.getPageSize() > 250) {
			throw new BadRequestException(
				LanguageUtil.format(
					contextAcceptLanguage.getPreferredLocale(),
					"page-size-is-greater-than-x", 250));
		}

		if (ArrayUtil.isEmpty(sorts)) {
			sorts = new Sort[] {
				new Sort(
					Field.getSortableFieldName(Field.MODIFIED_DATE),
					Sort.STRING_TYPE, true)
			};
		}

		DDMStructure ddmStructure = _ddmStructureLocalService.getStructure(
			dataDefinitionId);

		if (Validator.isNull(keywords)) {
			return Page.of(
				transform(
					_appBuilderAppLocalService.getAppBuilderApps(
						ddmStructure.getGroupId(),
						contextCompany.getCompanyId(),
						ddmStructure.getStructureId(),
						pagination.getStartPosition(),
						pagination.getEndPosition(),
						_toOrderByComparator(
							(Sort)ArrayUtil.getValue(sorts, 0))),
					this::_toApp),
				pagination,
				_appBuilderAppLocalService.getAppBuilderAppsCount(
					ddmStructure.getGroupId(), contextCompany.getCompanyId(),
					ddmStructure.getStructureId()));
		}

		return SearchUtil.search(
			booleanQuery -> {
			},
			null, AppBuilderApp.class, keywords, pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> {
				searchContext.setAttribute("ddmStructureId", dataDefinitionId);
				searchContext.setAttribute(
					Field.CLASS_NAME_ID,
					_portal.getClassNameId(AppBuilderApp.class));
				searchContext.setAttribute(Field.NAME, keywords);
				searchContext.setCompanyId(contextCompany.getCompanyId());
				searchContext.setGroupIds(
					new long[] {ddmStructure.getGroupId()});
			},
			document -> _toApp(
				_appBuilderAppLocalService.getAppBuilderApp(
					GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)))),
			sorts);
	}

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap)
		throws Exception {

		return _entityModel;
	}

	@Override
	public Page<App> getSiteAppsPage(
			Long siteId, String keywords, Pagination pagination, Sort[] sorts)
		throws Exception {

		if (pagination.getPageSize() > 250) {
			throw new BadRequestException(
				LanguageUtil.format(
					contextAcceptLanguage.getPreferredLocale(),
					"page-size-is-greater-than-x", 250));
		}

		if (ArrayUtil.isEmpty(sorts)) {
			sorts = new Sort[] {
				new Sort(
					Field.getSortableFieldName(Field.MODIFIED_DATE),
					Sort.STRING_TYPE, true)
			};
		}

		if (Validator.isNull(keywords)) {
			return Page.of(
				transform(
					_appBuilderAppLocalService.getAppBuilderApps(
						siteId, pagination.getStartPosition(),
						pagination.getEndPosition(),
						_toOrderByComparator(
							(Sort)ArrayUtil.getValue(sorts, 0))),
					this::_toApp),
				pagination,
				_appBuilderAppLocalService.getAppBuilderAppsCount(siteId));
		}

		return SearchUtil.search(
			booleanQuery -> {
			},
			null, AppBuilderApp.class, keywords, pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> {
				searchContext.setAttribute(
					Field.CLASS_NAME_ID,
					_portal.getClassNameId(AppBuilderApp.class));
				searchContext.setAttribute(Field.NAME, keywords);
				searchContext.setCompanyId(contextCompany.getCompanyId());
				searchContext.setGroupIds(new long[] {siteId});
			},
			document -> _toApp(
				_appBuilderAppLocalService.getAppBuilderApp(
					GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)))),
			sorts);
	}

	@Override
	public App postDataDefinitionApp(Long dataDefinitionId, App app)
		throws Exception {

		_validate(
			app.getDataLayoutId(), app.getDataListViewId(), app.getStatus());

		DDMStructure ddmStructure = _ddmStructureLocalService.getStructure(
			dataDefinitionId);
		AppBuilderAppConstants.Status appBuilderAppConstantsStatus =
			AppBuilderAppConstants.Status.parse(app.getStatus());

		return _toApp(
			_appBuilderAppLocalService.addAppBuilderApp(
				ddmStructure.getGroupId(), contextCompany.getCompanyId(),
				PrincipalThreadLocal.getUserId(), dataDefinitionId,
				GetterUtil.getLong(app.getDataLayoutId()),
				GetterUtil.getLong(app.getDataListViewId()),
				LocalizedValueUtil.toLocaleStringMap(app.getName()),
				_toJSON(app.getSettings()),
				appBuilderAppConstantsStatus.getValue()));
	}

	@Override
	public App putApp(Long appId, App app) throws Exception {
		_validate(
			app.getDataLayoutId(), app.getDataListViewId(), app.getStatus());

		DDMStructure ddmStructure = _ddmStructureLocalService.getStructure(
			app.getDataDefinitionId());
		AppBuilderAppConstants.Status appBuilderAppConstantsStatus =
			AppBuilderAppConstants.Status.parse(app.getStatus());

		return _toApp(
			_appBuilderAppLocalService.updateAppBuilderApp(
				PrincipalThreadLocal.getUserId(), appId,
				ddmStructure.getStructureId(), app.getDataLayoutId(),
				app.getDataListViewId(),
				LocalizedValueUtil.toLocaleStringMap(app.getName()),
				_toJSON(app.getSettings()),
				appBuilderAppConstantsStatus.getValue()));
	}

	private App _toApp(AppBuilderApp appBuilderApp) throws Exception {
		AppBuilderAppConstants.Status appBuilderAppConstantsStatus =
			AppBuilderAppConstants.Status.parse(appBuilderApp.getStatus());

		return new App() {
			{
				dataDefinitionId = appBuilderApp.getDdmStructureId();
				dataLayoutId = appBuilderApp.getDdmStructureLayoutId();
				dataListViewId = appBuilderApp.getDeDataListViewId();
				dateCreated = appBuilderApp.getCreateDate();
				dateModified = appBuilderApp.getModifiedDate();
				id = appBuilderApp.getAppBuilderAppId();
				name = LocalizedValueUtil.toStringObjectMap(
					appBuilderApp.getNameMap());
				settings = _toSettings(appBuilderApp.getSettings());
				siteId = appBuilderApp.getGroupId();
				status = appBuilderAppConstantsStatus.getLabel();
				userId = appBuilderApp.getUserId();
			}
		};
	}

	private String _toJSON(Map<String, Object> settings) {
		return JSONUtil.put(
			"deploymentTypes", settings.get("deploymentTypes")
		).toString();
	}

	private OrderByComparator<AppBuilderApp> _toOrderByComparator(Sort sort) {
		boolean ascending = !sort.isReverse();

		String sortFieldName = sort.getFieldName();

		if (StringUtil.startsWith(sortFieldName, "createDate")) {
			return new AppBuilderAppCreateDateComparator(ascending);
		}
		else if (StringUtil.startsWith(sortFieldName, "localized_name")) {
			return new AppBuilderAppNameComparator(ascending);
		}

		return new AppBuilderAppModifiedDateComparator(ascending);
	}

	private Map<String, Object> _toSettings(String settings) throws Exception {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(settings);

		return new HashMap<String, Object>() {
			{
				put(
					"deploymentTypes",
					JSONUtil.toObjectList(
						(JSONArray)jsonObject.get("deploymentTypes")));
			}
		};
	}

	private void _validate(
			Long dataLayoutId, Long dataListViewId, String status)
		throws Exception {

		if ((dataLayoutId == null) && (dataListViewId == null)) {
			throw new InvalidAppException(
				"An app must have one data engine data layout or one data " +
					"engine data list view");
		}

		if (dataLayoutId != null) {
			DDMStructureLayout ddmStructureLayout =
				_ddmStructureLayoutLocalService.fetchStructureLayout(
					dataLayoutId);

			if (ddmStructureLayout == null) {
				throw new NoSuchStructureLayoutException(
					"Data layout " + dataLayoutId + " does not exist");
			}
		}

		if (dataListViewId != null) {
			String sessionId = CookieKeys.getCookie(
				contextHttpServletRequest, CookieKeys.JSESSIONID);

			DataListViewResource dataListViewResource =
				DataListViewResource.builder(
				).endpoint(
					_portal.getHost(contextHttpServletRequest),
					contextHttpServletRequest.getServerPort(),
					contextHttpServletRequest.getScheme()
				).header(
					"Cookie", "JSESSIONID=" + sessionId
				).parameter(
					"p_auth", AuthTokenUtil.getToken(contextHttpServletRequest)
				).build();

			try {
				dataListViewResource.getDataListView(dataListViewId);
			}
			catch (Exception e) {
				throw new NoSuchDataListViewException(
					"Data list view " + dataListViewId + " does not exist", e);
			}
		}

		if (Validator.isNull(AppBuilderAppConstants.Status.parse(status))) {
			throw new AppBuilderAppStatusException("Invalid status " + status);
		}
	}

	private static final EntityModel _entityModel =
		new AppBuilderAppEntityModel();

	@Reference
	private AppBuilderAppLocalService _appBuilderAppLocalService;

	@Reference
	private DDMStructureLayoutLocalService _ddmStructureLayoutLocalService;

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private Portal _portal;

}