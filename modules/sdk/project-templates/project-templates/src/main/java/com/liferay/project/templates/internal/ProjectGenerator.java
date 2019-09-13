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

package com.liferay.project.templates.internal;

import aQute.bnd.version.Version;
import aQute.bnd.version.VersionRange;

import com.liferay.project.templates.FileUtil;
import com.liferay.project.templates.ProjectTemplateCustomizer;
import com.liferay.project.templates.ProjectTemplates;
import com.liferay.project.templates.ProjectTemplatesArgs;
import com.liferay.project.templates.WorkspaceUtil;
import com.liferay.project.templates.internal.util.ProjectTemplatesUtil;
import com.liferay.project.templates.internal.util.Validator;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;

/**
 * @author Gregory Amerson
 */
public class ProjectGenerator {

	public ArchetypeGenerationResult generateProject(
			ProjectTemplatesArgs projectTemplatesArgs, File destinationDir)
		throws Exception {

		List<File> archetypesDirs = projectTemplatesArgs.getArchetypesDirs();
		String artifactId = projectTemplatesArgs.getName();
		String author = projectTemplatesArgs.getAuthor();
		String className = projectTemplatesArgs.getClassName();
		String dependencyInjector =
			projectTemplatesArgs.getDependencyInjector();
		boolean dependencyManagementEnabled =
			projectTemplatesArgs.isDependencyManagementEnabled();
		String framework = projectTemplatesArgs.getFramework();
		String frameworkDependencies =
			projectTemplatesArgs.getFrameworkDependencies();
		String groupId = projectTemplatesArgs.getGroupId();
		String liferayVersion = projectTemplatesArgs.getLiferayVersion();
		String packageName = projectTemplatesArgs.getPackageName();
		String template = projectTemplatesArgs.getTemplate();
		String viewType = projectTemplatesArgs.getViewType();

		if (template.equals("portlet")) {
			projectTemplatesArgs.setTemplate("mvc-portlet");
		}

		File templateFile = ProjectTemplatesUtil.getTemplateFile(
			projectTemplatesArgs);

		String liferayVersions = FileUtil.getManifestProperty(
			templateFile, "Liferay-Versions");

		if ((liferayVersions != null) &&
			!_isInVersionRange(liferayVersion, liferayVersions)) {

			if (template.startsWith("npm-")) {
				throw new IllegalArgumentException(
					"NPM portlet project templates generated from this tool " +
						"are not supported for specified Liferay version. " +
							"See LPS-97950 for full details.");
			}

			throw new IllegalArgumentException(
				"Specified Liferay version is invalid. Must be in range " +
					liferayVersions);
		}

		if (Objects.isNull(groupId)) {
			groupId = packageName;
		}

		File workspaceDir = WorkspaceUtil.getWorkspaceDir(destinationDir);

		String projectType = "standalone";

		if (workspaceDir != null) {
			projectType = WorkspaceUtil.WORKSPACE;
		}

		ArchetypeGenerationRequest archetypeGenerationRequest =
			new ArchetypeGenerationRequest();

		String archetypeArtifactId =
			ProjectTemplates.TEMPLATE_BUNDLE_PREFIX +
				template.replace('-', '.');

		if (archetypeArtifactId.equals(
				"com.liferay.project.templates.portlet")) {

			archetypeArtifactId = "com.liferay.project.templates.mvc.portlet";
		}

		archetypeGenerationRequest.setArchetypeArtifactId(archetypeArtifactId);

		archetypeGenerationRequest.setArchetypeGroupId("com.liferay");
		archetypeGenerationRequest.setArchetypeVersion(
			FileUtil.getManifestProperty(templateFile, "Bundle-Version"));
		archetypeGenerationRequest.setArtifactId(artifactId);
		archetypeGenerationRequest.setGroupId(groupId);
		archetypeGenerationRequest.setInteractiveMode(false);
		archetypeGenerationRequest.setOutputDirectory(destinationDir.getPath());
		archetypeGenerationRequest.setPackage(packageName);

		String buildType = "gradle";

		if (projectTemplatesArgs.isMaven()) {
			buildType = "maven";
		}

		Properties properties = new Properties();

		_setProperty(properties, "author", author);
		_setProperty(properties, "buildType", buildType);
		_setProperty(properties, "className", className);
		_setProperty(properties, "dependencyInjector", dependencyInjector);
		_setProperty(
			properties, "dependencyManagementEnabled",
			String.valueOf(dependencyManagementEnabled));
		_setProperty(properties, "framework", framework);
		_setProperty(
			properties, "frameworkDependencies", frameworkDependencies);
		_setProperty(properties, "liferayVersion", liferayVersion);
		_setProperty(properties, "package", packageName);
		_setProperty(properties, "projectType", projectType);
		_setProperty(properties, "viewType", viewType);

		archetypeGenerationRequest.setProperties(properties);

		archetypeGenerationRequest.setVersion("1.0.0");

		Archetyper archetyper = new Archetyper(archetypesDirs);

		ArchetypeArtifactManager archetypeArtifactManager =
			archetyper.createArchetypeArtifactManager();

		ProjectTemplateCustomizer projectTemplateCustomizer =
			_getProjectTemplateCustomizer(
				archetypeArtifactManager.getArchetypeFile(
					archetypeGenerationRequest.getArchetypeGroupId(),
					archetypeGenerationRequest.getArchetypeArtifactId(),
					archetypeGenerationRequest.getArchetypeVersion(), null,
					null, null));

		if (projectTemplateCustomizer != null) {
			projectTemplateCustomizer.onBeforeGenerateProject(
				projectTemplatesArgs, archetypeGenerationRequest);
		}

		ArchetypeManager archetypeManager = archetyper.createArchetypeManager();

		ArchetypeGenerationResult archetypeGenerationResult =
			archetypeManager.generateProjectFromArchetype(
				archetypeGenerationRequest);

		if (projectTemplateCustomizer != null) {
			projectTemplateCustomizer.onAfterGenerateProject(
				projectTemplatesArgs, destinationDir,
				archetypeGenerationResult);
		}

		return archetypeGenerationResult;
	}

	private static boolean _isInVersionRange(
		String versionString, String range) {

		Version version = new Version(versionString);

		VersionRange versionRange = new VersionRange(range);

		return versionRange.includes(version);
	}

	private ProjectTemplateCustomizer _getProjectTemplateCustomizer(
			File archetypeFile)
		throws MalformedURLException {

		URI uri = archetypeFile.toURI();

		URLClassLoader urlClassLoader = new URLClassLoader(
			new URL[] {uri.toURL()});

		ServiceLoader<ProjectTemplateCustomizer> serviceLoader =
			ServiceLoader.load(ProjectTemplateCustomizer.class, urlClassLoader);

		Iterator<ProjectTemplateCustomizer> iterator = serviceLoader.iterator();

		if (iterator.hasNext()) {
			return iterator.next();
		}

		return null;
	}

	private void _setProperty(
		Properties properties, String name, String value) {

		if (Validator.isNotNull(value)) {
			properties.setProperty(name, value);
		}
	}

}