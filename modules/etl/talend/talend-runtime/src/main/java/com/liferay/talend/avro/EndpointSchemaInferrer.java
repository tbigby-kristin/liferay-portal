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

package com.liferay.talend.avro;

import com.liferay.talend.avro.constants.AvroConstants;
import com.liferay.talend.common.json.JsonFinder;
import com.liferay.talend.common.oas.OASFormat;
import com.liferay.talend.common.oas.OASType;
import com.liferay.talend.common.oas.constants.OASConstants;
import com.liferay.talend.common.util.StringUtil;
import com.liferay.talend.tliferayoutput.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.avro.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.talend.components.common.SchemaProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.NameUtil;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * @author Zoltán Takács
 * @author Igor Beslic
 */
public class EndpointSchemaInferrer {

	public String extractEndpointSchemaName(
		String endpoint, String operation, JsonObject oasJsonObject) {

		String schemaName = null;

		if (Objects.equals(operation, OASConstants.OPERATION_GET)) {
			String jsonFinderPath = StringUtil.replace(
				OASConstants.
					PATH_RESPONSES_CONTENT_APPLICATION_JSON_SCHEMA_PATTERN,
				"ENDPOINT_TPL", endpoint, "OPERATION_TPL", operation);

			JsonObject schemaJsonObject = _jsonFinder.getDescendantJsonObject(
				jsonFinderPath, oasJsonObject);

			schemaName = _stripSchemaName(
				schemaJsonObject.getString(OASConstants.REF));

			JsonObject schemaDefinitionJsonObject = _extractSchemaJsonObject(
				schemaName, oasJsonObject);

			JsonObject itemsPropertiesJsonObject =
				_jsonFinder.getDescendantJsonObject(
					OASConstants.PATH_PROPERTIES_ITEMS_ITEMS,
					schemaDefinitionJsonObject);

			if (!itemsPropertiesJsonObject.isEmpty() &&
				itemsPropertiesJsonObject.containsKey(OASConstants.REF)) {

				schemaName = _stripSchemaName(
					itemsPropertiesJsonObject.getString(OASConstants.REF));
			}

			return schemaName;
		}

		if (!Objects.equals(operation, OASConstants.OPERATION_PATCH) &&
			!Objects.equals(operation, OASConstants.OPERATION_POST)) {

			return null;
		}

		String jsonFinderPath = StringUtil.replace(
			OASConstants.
				PATH_REQUEST_BODY_CONTENT_APPLICATION_JSON_SCHEMA_PATTERN,
			"ENDPOINT_TPL", endpoint, "OPERATION_TPL", operation);

		JsonObject schemaJsonObject = _jsonFinder.getDescendantJsonObject(
			jsonFinderPath, oasJsonObject);

		schemaName = _stripSchemaName(
			schemaJsonObject.getString(OASConstants.REF));

		return schemaName;
	}

	public Schema inferSchema(
		String endpoint, String operation, JsonObject apiSpecJsonObject) {

		operation = operation.toLowerCase(Locale.US);

		Schema schema = SchemaProperties.EMPTY_SCHEMA;

		if (operation.equals(Action.Delete.getMethodName())) {
			schema = _getDeleteSchema();
		}
		else {
			schema = _getSchema(endpoint, operation, apiSpecJsonObject);
		}

		return schema;
	}

	private static JsonObject _extractSchemaJsonObject(
		String schemaName, JsonObject oasJsonObject) {

		String jsonFinderPath = StringUtil.replace(
			OASConstants.PATH_COMPONENTS_SCHEMAS_PATTERN, "SCHEMA_TPL",
			schemaName);

		return _jsonFinder.getDescendantJsonObject(
			jsonFinderPath, oasJsonObject);
	}

	private static Schema _getDeleteSchema() {
		List<Schema.Field> schemaFields = new ArrayList<>(1);

		Schema.Field designField = new Schema.Field(
			AvroConstants.ID, AvroUtils._long(), null, (Object)null);

		designField.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");

		schemaFields.add(designField);

		return Schema.createRecord("Runtime", null, null, false, schemaFields);
	}

	private static Schema.Field _getDesignField(
		String fieldName, JsonObject propertyJsonObject) {

		Schema.Field designField = new Schema.Field(
			fieldName, AvroUtils.wrapAsNullable(AvroUtils._string()), null,
			(Object)null);

		OASType oasType = OASType.fromDefinition(
			propertyJsonObject.getString(OASConstants.TYPE));

		if (oasType == OASType.ARRAY) {
			return designField;
		}

		String openAPIFormatDefinition = null;

		if (propertyJsonObject.containsKey(OASConstants.FORMAT)) {
			openAPIFormatDefinition = propertyJsonObject.getString(
				OASConstants.FORMAT);
		}
		else if ((oasType == OASType.OBJECT) &&
				 propertyJsonObject.containsKey(
					 OASConstants.ADDITIONAL_PROPERTIES)) {

			JsonObject additionalPropertiesJsonObject =
				propertyJsonObject.getJsonObject(
					OASConstants.ADDITIONAL_PROPERTIES);

			if (additionalPropertiesJsonObject.containsKey(OASConstants.TYPE)) {
				openAPIFormatDefinition =
					additionalPropertiesJsonObject.getString(OASConstants.TYPE);
			}
		}

		OASFormat oasFormat = OASFormat.fromOpenAPITypeAndFormat(
			oasType, openAPIFormatDefinition);

		if (oasFormat == OASFormat.BIGDECIMAL) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._decimal()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.BOOLEAN) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._boolean()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.BINARY) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._bytes()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.DATE) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._date()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.DATE_TIME) {
			designField = new Schema.Field(
				fieldName,
				AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.DICTIONARY) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._string()), null,
				(Object)null);

			designField.addProp("oas.dictionary", "true");
		}
		else if (oasFormat == OASFormat.DOUBLE) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._double()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.FLOAT) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._float()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.INT32) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._int()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.INT64) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._long()), null,
				(Object)null);
		}
		else if (oasFormat == OASFormat.STRING) {
			designField = new Schema.Field(
				fieldName, AvroUtils.wrapAsNullable(AvroUtils._string()), null,
				(Object)null);
		}

		return designField;
	}

	private static String _stripSchemaName(String reference) {
		return reference.replaceAll(OASConstants.PATH_SCHEMA_REFERENCE, "");
	}

	private Set<String> _asSet(JsonArray jsonArray) {
		if ((jsonArray == null) || jsonArray.isEmpty()) {
			return Collections.emptySet();
		}

		List<JsonString> jsonStrings = jsonArray.getValuesAs(JsonString.class);

		Set<String> strings = new HashSet<>();

		for (JsonString jsonString : jsonStrings) {
			strings.add(jsonString.getString());
		}

		return strings;
	}

	private Schema _getSchema(
		String endpoint, String operation, JsonObject apiSpecJsonObject) {

		AtomicInteger index = new AtomicInteger();
		List<Schema.Field> schemaFields = new ArrayList<>();
		Set<String> previousFieldNames = new HashSet<>();

		String schemaName = extractEndpointSchemaName(
			endpoint, operation, apiSpecJsonObject);

		if (_logger.isDebugEnabled()) {
			_logger.debug("Schema name: {}", schemaName);
		}

		if (StringUtil.isEmpty(schemaName)) {
			throw TalendRuntimeException.createUnexpectedException(
				"Unable to determine the Schema for the selected endpoint");
		}

		JsonObject schemaJsonObject = _extractSchemaJsonObject(
			schemaName, apiSpecJsonObject);

		_processSchemaJsonObject(
			null, schemaJsonObject, index, previousFieldNames, schemaFields,
			apiSpecJsonObject);

		return Schema.createRecord("Runtime", null, null, false, schemaFields);
	}

	private void _processSchemaJsonObject(
		String parentPropertyName, JsonObject schemaJsonObject,
		AtomicInteger index, Set<String> previousFieldNames,
		List<Schema.Field> schemaFields, JsonObject apiSpecJsonObject) {

		Set<String> required = _asSet(
			schemaJsonObject.getJsonArray(OASConstants.REQUIRED));

		JsonObject schemaPropertiesJsonObject = schemaJsonObject.getJsonObject(
			OASConstants.PROPERTIES);

		Set<Map.Entry<String, JsonValue>> entries =
			schemaPropertiesJsonObject.entrySet();

		for (Iterator<Map.Entry<String, JsonValue>> it = entries.iterator();
			 it.hasNext(); index.incrementAndGet()) {

			Map.Entry<String, JsonValue> propertyEntry = it.next();

			JsonValue propertyJsonValue = propertyEntry.getValue();

			JsonObject propertyJsonObject = propertyJsonValue.asJsonObject();

			if (propertyJsonObject.containsKey(OASConstants.REF) &&
				(parentPropertyName == null)) {

				String referenceSchemaName = _stripSchemaName(
					propertyJsonObject.getString(OASConstants.REF));

				JsonObject referenceSchemaJsonObject = _extractSchemaJsonObject(
					referenceSchemaName, apiSpecJsonObject);

				_processSchemaJsonObject(
					propertyEntry.getKey(), referenceSchemaJsonObject, index,
					previousFieldNames, schemaFields, apiSpecJsonObject);

				continue;
			}

			String fieldName = NameUtil.correct(
				propertyEntry.getKey(), index.get(), previousFieldNames);

			if (parentPropertyName != null) {
				fieldName = NameUtil.correct(
					parentPropertyName + "_" + propertyEntry.getKey(),
					index.get(), previousFieldNames);
			}

			previousFieldNames.add(fieldName);

			Schema.Field designField = _getDesignField(
				fieldName, propertyJsonValue.asJsonObject());

			if (required.contains(fieldName)) {
				designField.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
			}

			schemaFields.add(designField);
		}
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		EndpointSchemaInferrer.class);

	private static final JsonFinder _jsonFinder = new JsonFinder();

}