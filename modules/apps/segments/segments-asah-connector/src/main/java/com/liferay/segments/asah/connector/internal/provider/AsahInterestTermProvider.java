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

package com.liferay.segments.asah.connector.internal.provider;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.segments.asah.connector.internal.cache.AsahInterestTermCache;
import com.liferay.segments.asah.connector.internal.constants.SegmentsAsahDestinationNames;
import com.liferay.segments.constants.SegmentsEntryConstants;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sarai Díaz
 */
@Component(
	immediate = true,
	property = "segments.entry.provider.source=" + SegmentsEntryConstants.SOURCE_ASAH_FARO_BACKEND,
	service = AsahInterestTermProvider.class
)
public class AsahInterestTermProvider {

	public String[] getInterestTerms(String userId) {
		String[] cachedInterestTerms = _asahInterestTermCache.getInterestTerms(
			userId);

		if (cachedInterestTerms == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Asah interest terms cache not found for user ID " +
						userId);
			}

			_sendMessage(userId);

			return new String[0];
		}

		return cachedInterestTerms;
	}

	@Activate
	protected void activate() {
		DestinationConfiguration destinationConfiguration =
			new DestinationConfiguration(
				DestinationConfiguration.DESTINATION_TYPE_PARALLEL,
				SegmentsAsahDestinationNames.INTEREST_TERMS);

		Destination destination = _destinationFactory.createDestination(
			destinationConfiguration);

		_messageBus.addDestination(destination);
	}

	@Deactivate
	protected void deactivate() {
		_messageBus.removeDestination(
			SegmentsAsahDestinationNames.INTEREST_TERMS);
	}

	private void _sendMessage(String userId) {
		Message message = new Message();

		message.setPayload(userId);

		_messageBus.sendMessage(
			SegmentsAsahDestinationNames.INTEREST_TERMS, message);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AsahInterestTermProvider.class);

	@Reference
	private AsahInterestTermCache _asahInterestTermCache;

	@Reference
	private DestinationFactory _destinationFactory;

	@Reference
	private MessageBus _messageBus;

}