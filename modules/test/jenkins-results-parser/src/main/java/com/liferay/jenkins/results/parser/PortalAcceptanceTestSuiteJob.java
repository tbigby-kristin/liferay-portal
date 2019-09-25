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

package com.liferay.jenkins.results.parser;

import java.util.Properties;
import java.util.Set;

/**
 * @author Yi-Chen Tsai
 */
public abstract class PortalAcceptanceTestSuiteJob
	extends PortalGitRepositoryJob implements TestSuiteJob {

	public PortalAcceptanceTestSuiteJob(String jobName) {
		this(jobName, "default");
	}

	public PortalAcceptanceTestSuiteJob(String jobName, String testSuiteName) {
		super(jobName);

		_testSuiteName = testSuiteName;
	}

	@Override
	public Set<String> getBatchNames() {
		Properties jobProperties = getJobProperties();

		String testBatchNames = JenkinsResultsParserUtil.getProperty(
			jobProperties, "test.batch.names[" + _testSuiteName + "]");

		if (testBatchNames == null) {
			testBatchNames = JenkinsResultsParserUtil.getProperty(
				jobProperties, "test.batch.names");
		}

		return getSetFromString(testBatchNames);
	}

	@Override
	public Set<String> getDistTypes() {
		Properties jobProperties = getJobProperties();

		String testBatchDistAppServers = JenkinsResultsParserUtil.getProperty(
			jobProperties,
			"test.batch.dist.app.servers[" + _testSuiteName + "]");

		if (testBatchDistAppServers == null) {
			testBatchDistAppServers = JenkinsResultsParserUtil.getProperty(
				jobProperties, "test.batch.dist.app.servers");
		}

		return getSetFromString(testBatchDistAppServers);
	}

	@Override
	public String getTestSuiteName() {
		return _testSuiteName;
	}

	@Override
	public boolean isUsePreBuiltBundles() {
		String preBuiltBundles = JenkinsResultsParserUtil.getProperty(
			getJobProperties(), "test.batch.dist.pre.built.bundles",
			getTestSuiteName());

		if ((preBuiltBundles != null) && preBuiltBundles.equals("true")) {
			return true;
		}

		return false;
	}

	private final String _testSuiteName;

}