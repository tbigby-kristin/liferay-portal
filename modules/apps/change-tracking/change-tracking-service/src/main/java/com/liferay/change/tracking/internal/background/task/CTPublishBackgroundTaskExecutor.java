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

package com.liferay.change.tracking.internal.background.task;

import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.change.tracking.engine.CTEngineManager;
import com.liferay.change.tracking.internal.CTPersistenceHelperThreadLocal;
import com.liferay.change.tracking.internal.CTServiceRegistry;
import com.liferay.change.tracking.internal.background.task.display.CTPublishBackgroundTaskDisplay;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTEntry;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTEntryLocalService;
import com.liferay.change.tracking.service.CTProcessLocalService;
import com.liferay.petra.lang.SafeClosable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatusRegistry;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Zoltan Csaszi
 * @author Daniel Kocsis
 */
@Component(
	immediate = true,
	property = "background.task.executor.class.name=com.liferay.change.tracking.internal.background.task.CTPublishBackgroundTaskExecutor",
	service = AopService.class
)
public class CTPublishBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor implements AopService {

	public CTPublishBackgroundTaskExecutor() {
		setBackgroundTaskStatusMessageTranslator(
			new CTPublishBackgroundTaskStatusMessageTranslator());
		setIsolationLevel(BackgroundTaskConstants.ISOLATION_LEVEL_COMPANY);
	}

	@Override
	public BackgroundTaskExecutor clone() {
		return _backgroundTaskExecutor;
	}

	@Override
	@Transactional(
		propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class
	)
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		long ctCollectionId = GetterUtil.getLong(
			taskContextMap.get("ctCollectionId"));
		boolean ignoreCollision = GetterUtil.getBoolean(
			taskContextMap.get("ignoreCollision"));

		CTCollection ctCollection = _ctCollectionLocalService.getCTCollection(
			ctCollectionId);

		List<CTEntry> ctEntries = _ctEntryLocalService.getCTCollectionCTEntries(
			ctCollectionId);

		Map<Long, CTPublisher> ctPublishers = new HashMap<>();

		for (CTEntry ctEntry : ctEntries) {
			CTPublisher ctPublisher = ctPublishers.computeIfAbsent(
				ctEntry.getModelClassNameId(),
				modelClassNameId -> {
					CTService<?> ctService = _ctServiceRegistry.getCTService(
						modelClassNameId);

					if (ctService != null) {
						return new CTServicePublisher<>(
							_ctEntryLocalService, ctService, ctCollectionId,
							CTConstants.CT_COLLECTION_ID_PRODUCTION);
					}

					if (_ctEngineManager.isChangeTrackingSupported(
							ctCollection.getCompanyId(), modelClassNameId)) {

						return new CTDefinitionPublisher(
							_ctEntryLocalService, ignoreCollision);
					}

					throw new SystemException(
						StringBundler.concat(
							"Unable to publish ", ctCollectionId,
							" because service for ", modelClassNameId,
							" is missing"));
				});

			ctPublisher.addCTEntry(ctEntry);
		}

		try (SafeClosable safeClosable =
				CTPersistenceHelperThreadLocal.setEnabled(false)) {

			for (CTPublisher ctPublisher : ctPublishers.values()) {
				ctPublisher.publish();
			}
		}

		_ctCollectionLocalService.updateStatus(
			backgroundTask.getUserId(), ctCollection,
			WorkflowConstants.STATUS_APPROVED);

		return BackgroundTaskResult.SUCCESS;
	}

	@Override
	public Class<?>[] getAopInterfaces() {
		return new Class<?>[] {BackgroundTaskExecutor.class};
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return new CTPublishBackgroundTaskDisplay(backgroundTask);
	}

	@Override
	public void setAopProxy(Object aopProxy) {
		_backgroundTaskExecutor = (BackgroundTaskExecutor)aopProxy;
	}

	private BackgroundTaskExecutor _backgroundTaskExecutor;

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private BackgroundTaskStatusRegistry _backgroundTaskStatusRegistry;

	@Reference
	private CTCollectionLocalService _ctCollectionLocalService;

	@Reference
	private CTEngineManager _ctEngineManager;

	@Reference
	private CTEntryLocalService _ctEntryLocalService;

	@Reference
	private CTProcessLocalService _ctProcessLocalService;

	@Reference
	private CTServiceRegistry _ctServiceRegistry;

}