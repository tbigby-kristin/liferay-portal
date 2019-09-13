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

package com.liferay.dynamic.data.mapping.internal.io.exporter;

import com.liferay.dynamic.data.mapping.io.exporter.DDMFormInstanceRecordWriter;
import com.liferay.dynamic.data.mapping.io.exporter.DDMFormInstanceRecordWriterRequest;
import com.liferay.dynamic.data.mapping.io.exporter.DDMFormInstanceRecordWriterResponse;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.CSVUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

/**
 * @author Leonardo Barros
 */
@Component(
	immediate = true, property = "ddm.form.instance.record.writer.type=csv",
	service = DDMFormInstanceRecordWriter.class
)
public class DDMFormInstanceRecordCSVWriter
	implements DDMFormInstanceRecordWriter {

	@Override
	public DDMFormInstanceRecordWriterResponse write(
			DDMFormInstanceRecordWriterRequest
				ddmFormInstanceRecordWriterRequest)
		throws Exception {

		StringBundler sb = new StringBundler(3);

		Map<String, String> ddmFormFieldsLabel =
			ddmFormInstanceRecordWriterRequest.getDDMFormFieldsLabel();

		List<String> labels = _getDistinctLabels(ddmFormFieldsLabel);

		sb.append(writeValues(labels));

		sb.append(StringPool.NEW_LINE);

		sb.append(
			writeRecords(
				ddmFormFieldsLabel,
				ddmFormInstanceRecordWriterRequest.getDDMFormFieldValues(),
				labels));

		String csv = sb.toString();

		DDMFormInstanceRecordWriterResponse.Builder builder =
			DDMFormInstanceRecordWriterResponse.Builder.newBuilder(
				csv.getBytes());

		return builder.build();
	}

	protected String writeRecord(
		Map<String, String> ddmFormFieldsLabel,
		Map<String, String> ddmFormFieldValue, List<String> labels) {

		Set<Map.Entry<String, String>> entrySet = ddmFormFieldsLabel.entrySet();

		Stream<Map.Entry<String, String>> stream = entrySet.stream();

		return stream.filter(
			entry ->
				labels.contains(entry.getValue()) &&
				ddmFormFieldValue.containsKey(entry.getKey())
		).map(
			entry -> CSVUtil.encode(ddmFormFieldValue.get(entry.getKey()))
		).collect(
			Collectors.joining(StringPool.COMMA)
		);
	}

	protected String writeRecords(
		Map<String, String> ddmFormFieldsLabel,
		List<Map<String, String>> ddmFormFieldsValue, List<String> labels) {

		Stream<Map<String, String>> stream = ddmFormFieldsValue.stream();

		return stream.map(
			values -> writeRecord(ddmFormFieldsLabel, values, labels)
		).collect(
			Collectors.joining(StringPool.NEW_LINE)
		);
	}

	protected String writeValues(Collection<String> values) {
		Stream<String> stream = values.stream();

		return stream.map(
			CSVUtil::encode
		).collect(
			Collectors.joining(StringPool.COMMA)
		);
	}

	private List<String> _getDistinctLabels(
		Map<String, String> ddmFormFieldsLabel) {

		List<String> labels = new ArrayList<>();

		for (String label : ddmFormFieldsLabel.values()) {
			if (!labels.contains(label)) {
				labels.add(label);
			}
		}

		return labels;
	}

}