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

package com.liferay.app.builder.web.internal.deploy;

import com.liferay.app.builder.constants.AppBuilderAppConstants;
import com.liferay.app.builder.deploy.AppDeployer;
import com.liferay.app.builder.model.AppBuilderApp;
import com.liferay.app.builder.service.AppBuilderAppLocalService;
import com.liferay.app.builder.web.internal.application.list.ProductMenuAppPanelApp;
import com.liferay.app.builder.web.internal.application.list.ProductMenuAppPanelCategory;
import com.liferay.app.builder.web.internal.constants.AppBuilderPanelCategoryKeys;
import com.liferay.app.builder.web.internal.constants.AppBuilderPortletKeys;
import com.liferay.app.builder.web.internal.portlet.ProductMenuAppPortlet;
import com.liferay.application.list.PanelApp;
import com.liferay.application.list.PanelCategory;
import com.liferay.application.list.constants.PanelCategoryKeys;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.LocaleThreadLocal;

import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.Portlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jeyvison Nascimento
 */
@Component(
	immediate = true, property = "com.app.builder.deploy.type=productMenu",
	service = AppDeployer.class
)
public class ProductMenuAppDeployer implements AppDeployer {

	@Activate
	public void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	@Override
	public void deploy(long appId) throws Exception {
		String portletName = _getPortletName(appId);
		String panelCategoryKey = _getPanelCategoryKey(appId);

		AppBuilderApp appBuilderApp =
			_appBuilderAppLocalService.getAppBuilderApp(appId);

		String appName = appBuilderApp.getName(
			LocaleThreadLocal.getDefaultLocale());

		_serviceRegistrationsMap.computeIfAbsent(
			appId,
			key -> new ServiceRegistration<?>[] {
				_deployAppPanelApp(portletName, panelCategoryKey),
				_deployAppPanelCategory(appName, panelCategoryKey),
				_deployAppPortlet(appName, portletName)
			});

		appBuilderApp.setStatus(
			AppBuilderAppConstants.Status.DEPLOYED.getValue());

		_appBuilderAppLocalService.updateAppBuilderApp(appBuilderApp);
	}

	@Override
	public void undeploy(long appId) throws Exception {
		ServiceRegistration<?>[] serviceRegistrations =
			_serviceRegistrationsMap.remove(appId);

		if (serviceRegistrations == null) {
			return;
		}

		for (ServiceRegistration serviceRegistration : serviceRegistrations) {
			serviceRegistration.unregister();
		}

		AppBuilderApp appBuilderApp =
			_appBuilderAppLocalService.getAppBuilderApp(appId);

		appBuilderApp.setStatus(
			AppBuilderAppConstants.Status.UNDEPLOYED.getValue());

		_appBuilderAppLocalService.updateAppBuilderApp(appBuilderApp);
	}

	private ServiceRegistration<?> _deployAppPanelApp(
		String portletName, String panelCategoryKey) {

		return _bundleContext.registerService(
			PanelApp.class, new ProductMenuAppPanelApp(portletName),
			new HashMapDictionary<String, Object>() {
				{
					put("panel.app.order:Integer", 100);
					put("panel.category.key", panelCategoryKey);
				}
			});
	}

	private ServiceRegistration<?> _deployAppPanelCategory(
		String appName, String panelCategoryKey) {

		Dictionary<String, Object> properties =
			new HashMapDictionary<String, Object>() {
				{
					put("key", panelCategoryKey);
					put("label", appName);
					put("panel.category.key", PanelCategoryKeys.CONTROL_PANEL);
					put("panel.category.order:Integer", 600);
				}
			};

		return _bundleContext.registerService(
			PanelCategory.class, new ProductMenuAppPanelCategory(properties),
			properties);
	}

	private ServiceRegistration<?> _deployAppPortlet(
		String appName, String portletName) {

		return _bundleContext.registerService(
			Portlet.class, new ProductMenuAppPortlet(),
			new HashMapDictionary<String, Object>() {
				{
					put("com.liferay.portlet.add-default-resource", true);
					put(
						"com.liferay.portlet.display-category",
						"category.hidden");
					put("com.liferay.portlet.use-default-template", "true");
					put("javax.portlet.display-name", appName);
					put("javax.portlet.name", portletName);
					put(
						"javax.portlet.init-param.template-path",
						"/META-INF/resources/");
					put("javax.portlet.init-param.view-template", "/view.jsp");
					put(
						"javax.portlet.security-role-ref",
						"administrator,guest,power-user,user ");
					put("javax.portlet.supports.mime-type", "text/html ");
				}
			});
	}

	private String _getPanelCategoryKey(long appId) {
		return AppBuilderPanelCategoryKeys.CONTROL_PANEL_APP_BUILDER_APP +
			appId;
	}

	private String _getPortletName(long appId) {
		return AppBuilderPortletKeys.PRODUCT_MENU_APP + "_" + appId;
	}

	@Reference
	private AppBuilderAppLocalService _appBuilderAppLocalService;

	private BundleContext _bundleContext;
	private final ConcurrentHashMap<Long, ServiceRegistration<?>[]>
		_serviceRegistrationsMap = new ConcurrentHashMap<>();

}