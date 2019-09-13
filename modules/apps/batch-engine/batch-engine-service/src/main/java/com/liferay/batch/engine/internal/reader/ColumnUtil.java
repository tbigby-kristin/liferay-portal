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

package com.liferay.batch.engine.internal.reader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivica Cardic
 */
public class ColumnUtil {

	public static void handleLocalizationColumn(
		String columnName, Map<String, Object> columnValues,
		int lastDelimiterIndex, String value) {

		String languageId = columnName.substring(lastDelimiterIndex + 1);

		columnName = columnName.substring(0, lastDelimiterIndex);

		Map<String, String> localizationMap =
			(Map<String, String>)columnValues.get(columnName);

		if (localizationMap == null) {
			localizationMap = new HashMap<>();

			columnValues.put(columnName, localizationMap);
		}

		localizationMap.put(languageId, value);
	}

}