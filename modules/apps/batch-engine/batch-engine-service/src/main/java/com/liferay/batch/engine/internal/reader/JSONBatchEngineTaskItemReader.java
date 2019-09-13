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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ivica Cardic
 */
public class JSONBatchEngineTaskItemReader<T>
	implements BatchEngineTaskItemReader<T> {

	public JSONBatchEngineTaskItemReader(
			InputStream inputStream, Class<? extends T> itemClass)
		throws IOException {

		_inputStream = inputStream;
		_itemClass = itemClass;

		_jsonParser = _jsonFactory.createParser(_inputStream);

		_jsonParser.nextToken();
	}

	@Override
	public void close() throws IOException {
		_inputStream.close();
		_jsonParser.close();
	}

	@Override
	public T read() throws IOException {
		if (_jsonParser.nextToken() == JsonToken.START_OBJECT) {
			return _objectMapper.readValue(_jsonParser, _itemClass);
		}

		return null;
	}

	private static final JsonFactory _jsonFactory = new JsonFactory();
	private static final ObjectMapper _objectMapper = new ObjectMapper();

	private final InputStream _inputStream;
	private final Class<? extends T> _itemClass;
	private final JsonParser _jsonParser;

}