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

package com.liferay.batch.engine.internal;

import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskExecutor;
import com.liferay.batch.engine.internal.reader.BatchEngineTaskItemReader;
import com.liferay.batch.engine.internal.reader.BatchEngineTaskItemReaderFactory;
import com.liferay.batch.engine.internal.writer.BatchEngineTaskItemWriter;
import com.liferay.batch.engine.internal.writer.BatchEngineTaskItemWriterFactory;
import com.liferay.batch.engine.model.BatchEngineTask;
import com.liferay.batch.engine.service.BatchEngineTaskLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Ivica Cardic
 */
@Component(service = BatchEngineTaskExecutor.class)
public class BatchEngineTaskExecutorImpl implements BatchEngineTaskExecutor {

	@Activate
	public void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_batchEngineTaskMethodServiceRegistry =
			new BatchEngineTaskMethodRegistry(bundleContext);

		_batchEngineTaskItemReaderFactory =
			new BatchEngineTaskItemReaderFactory(
				_batchEngineTaskMethodServiceRegistry);

		_batchEngineTaskItemWriterFactory =
			new BatchEngineTaskItemWriterFactory(
				_batchEngineTaskMethodServiceRegistry, _companyLocalService,
				_userLocalService);
	}

	@Deactivate
	public void deactivate() {
		_batchEngineTaskMethodServiceRegistry.destroy();
	}

	@Override
	public void execute(BatchEngineTask batchEngineTask) {
		try {
			batchEngineTask.setExecuteStatus(
				BatchEngineTaskExecuteStatus.STARTED.toString());
			batchEngineTask.setStartTime(new Date());

			_batchEngineTaskLocalService.updateBatchEngineTask(batchEngineTask);

			_execute(batchEngineTask);

			batchEngineTask.setEndTime(new Date());
			batchEngineTask.setExecuteStatus(
				BatchEngineTaskExecuteStatus.COMPLETED.toString());

			_batchEngineTaskLocalService.updateBatchEngineTask(batchEngineTask);

			BatchEngineTaskCallbackUtil.sendCallback(batchEngineTask);
		}
		catch (Throwable t) {
			_log.error(
				"Unable to update batch engine task " + batchEngineTask, t);

			batchEngineTask.setEndTime(new Date());
			batchEngineTask.setErrorMessage(t.getMessage());
			batchEngineTask.setExecuteStatus(
				BatchEngineTaskExecuteStatus.FAILED.toString());

			_batchEngineTaskLocalService.updateBatchEngineTask(batchEngineTask);

			BatchEngineTaskCallbackUtil.sendCallback(batchEngineTask);
		}
	}

	private void _commitItems(
			BatchEngineTaskItemWriter batchEngineTaskItemWriter,
			List<Object> items)
		throws Throwable {

		TransactionInvokerUtil.invoke(
			_transactionConfig,
			() -> {
				batchEngineTaskItemWriter.write(items);

				return null;
			});
	}

	private void _execute(BatchEngineTask batchEngineTask) throws Throwable {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		User user = _userLocalService.getUser(batchEngineTask.getUserId());

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));

		String name = PrincipalThreadLocal.getName();

		PrincipalThreadLocal.setName(user.getUserId());

		try (BatchEngineTaskItemReader batchEngineTaskItemReader =
				_batchEngineTaskItemReaderFactory.create(batchEngineTask);
			BatchEngineTaskItemWriter batchEngineTaskItemWriter =
				_batchEngineTaskItemWriterFactory.create(batchEngineTask)) {

			List<Object> items = new ArrayList<>();

			Object item = null;

			while ((item = batchEngineTaskItemReader.read()) != null) {
				items.add(item);

				if (items.size() == batchEngineTask.getBatchSize()) {
					_commitItems(batchEngineTaskItemWriter, items);

					items.clear();
				}
			}

			if (!items.isEmpty()) {
				_commitItems(batchEngineTaskItemWriter, items);
			}
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineTaskExecutorImpl.class);

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRES_NEW, new Class<?>[] {Exception.class});

	private BatchEngineTaskItemReaderFactory _batchEngineTaskItemReaderFactory;
	private BatchEngineTaskItemWriterFactory _batchEngineTaskItemWriterFactory;

	@Reference
	private BatchEngineTaskLocalService _batchEngineTaskLocalService;

	private BatchEngineTaskMethodRegistry _batchEngineTaskMethodServiceRegistry;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private UserLocalService _userLocalService;

}