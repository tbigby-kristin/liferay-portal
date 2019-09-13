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

package com.liferay.segments.asah.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.segments.asah.rest.dto.v1_0.ExperimentRun;
import com.liferay.segments.asah.rest.dto.v1_0.ExperimentVariant;
import com.liferay.segments.asah.rest.resource.v1_0.ExperimentRunResource;
import com.liferay.segments.constants.SegmentsExperimentConstants;
import com.liferay.segments.model.SegmentsExperiment;
import com.liferay.segments.model.SegmentsExperimentRel;
import com.liferay.segments.service.SegmentsExperimentRelService;
import com.liferay.segments.service.SegmentsExperimentService;

import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/experiment-run.properties",
	scope = ServiceScope.PROTOTYPE, service = ExperimentRunResource.class
)
public class ExperimentRunResourceImpl extends BaseExperimentRunResourceImpl {

	@Override
	public ExperimentRun postExperimentRun(
			Long segmentsExperimentKey, ExperimentRun experimentRun)
		throws Exception {

		ExperimentVariant[] experimentVariants =
			experimentRun.getExperimentVariants();

		Map<String, Double> segmentsExperienceKeySplitMap = new HashMap<>();

		for (ExperimentVariant experimentVariant : experimentVariants) {
			segmentsExperienceKeySplitMap.put(
				String.valueOf(experimentVariant.getId()),
				BigDecimalUtil.divide(
					experimentVariant.getTrafficSplit(), 100, 2,
					RoundingMode.HALF_DOWN));
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.popServiceContext();

		if (serviceContext == null) {
			serviceContext = new ServiceContext();
		}

		serviceContext.setAttribute("updateAsah", Boolean.FALSE);

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		return _toRunExperiment(
			_segmentsExperimentService.runSegmentsExperiment(
				String.valueOf(segmentsExperimentKey),
				BigDecimalUtil.divide(
					experimentRun.getConfidenceLevel(), 100, 2,
					RoundingMode.HALF_DOWN),
				segmentsExperienceKeySplitMap));
	}

	private ExperimentVariant _toExperimentVariant(
		SegmentsExperimentRel segmentsExperimentRel) {

		return new ExperimentVariant() {
			{
				id = segmentsExperimentRel.getSegmentsExperienceKey();
				trafficSplit = segmentsExperimentRel.getSplit();
			}
		};
	}

	private ExperimentVariant[] _toExperimentVariants(
		List<SegmentsExperimentRel> segmentsExperimentRels) {

		List<ExperimentVariant> experimentVariants = new ArrayList<>();

		for (SegmentsExperimentRel segmentsExperimentRel :
				segmentsExperimentRels) {

			ExperimentVariant experimentVariant = _toExperimentVariant(
				segmentsExperimentRel);

			experimentVariants.add(experimentVariant);
		}

		return experimentVariants.toArray(new ExperimentVariant[0]);
	}

	private ExperimentRun _toRunExperiment(
			SegmentsExperiment segmentsExperiment)
		throws PortalException {

		SegmentsExperimentConstants.Status segmentsExperimentConstantsStatus =
			SegmentsExperimentConstants.Status.valueOf(
				segmentsExperiment.getStatus());

		List<SegmentsExperimentRel> segmentsExperimentRels =
			_segmentsExperimentRelService.getSegmentsExperimentRels(
				segmentsExperiment.getSegmentsExperimentId());

		return new ExperimentRun() {
			{
				confidenceLevel = segmentsExperiment.getConfidenceLevel();
				experimentVariants = _toExperimentVariants(
					segmentsExperimentRels);
				status = segmentsExperimentConstantsStatus.toString();
			}
		};
	}

	@Reference
	private SegmentsExperimentRelService _segmentsExperimentRelService;

	@Reference
	private SegmentsExperimentService _segmentsExperimentService;

}