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

package com.liferay.data.engine.spi.dto;

import com.liferay.petra.lang.HashUtil;
import com.liferay.portal.kernel.util.ArrayUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Leonardo Barros
 */
public class SPIDataDefinitionField {

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SPIDataDefinitionField)) {
			return false;
		}

		SPIDataDefinitionField spiDataDefinitionField =
			(SPIDataDefinitionField)obj;

		if (Objects.equals(
				_customProperties, spiDataDefinitionField._customProperties) &&
			Objects.equals(
				_defaultValue, spiDataDefinitionField._defaultValue) &&
			Objects.equals(_fieldType, spiDataDefinitionField._fieldType) &&
			Objects.equals(_id, spiDataDefinitionField._id) &&
			Objects.equals(_indexable, spiDataDefinitionField._indexable) &&
			Objects.equals(_indexType, spiDataDefinitionField._indexType) &&
			Objects.equals(_label, spiDataDefinitionField._label) &&
			Objects.equals(_localizable, spiDataDefinitionField._localizable) &&
			Objects.equals(_name, spiDataDefinitionField._name) &&
			Objects.equals(
				_nestedSPIDataDefinitionFields,
				spiDataDefinitionField._nestedSPIDataDefinitionFields) &&
			Objects.equals(_readOnly, spiDataDefinitionField._readOnly) &&
			Objects.equals(_repeatable, spiDataDefinitionField._repeatable) &&
			Objects.equals(_required, spiDataDefinitionField._required) &&
			Objects.equals(_showlabel, spiDataDefinitionField._showlabel) &&
			Objects.equals(_tip, spiDataDefinitionField._tip) &&
			Objects.equals(_visible, spiDataDefinitionField._visible)) {

			return true;
		}

		return false;
	}

	public Map<String, Object> getCustomProperties() {
		return _customProperties;
	}

	public Map<String, Object> getDefaultValue() {
		return _defaultValue;
	}

	public String getFieldType() {
		return _fieldType;
	}

	public long getId() {
		return _id;
	}

	public boolean getIndexable() {
		return _indexable;
	}

	public String getIndexType() {
		return _indexType;
	}

	public Map<String, Object> getLabel() {
		return _label;
	}

	public boolean getLocalizable() {
		return _localizable;
	}

	public String getName() {
		return _name;
	}

	public SPIDataDefinitionField[] getNestedSPIDataDefinitionFields() {
		return _nestedSPIDataDefinitionFields;
	}

	public boolean getReadOnly() {
		return _readOnly;
	}

	public boolean getRepeatable() {
		return _repeatable;
	}

	public boolean getRequired() {
		return _required;
	}

	public boolean getShowLabel() {
		return _showlabel;
	}

	public Map<String, Object> getTip() {
		return _tip;
	}

	public boolean getVisible() {
		return _visible;
	}

	@Override
	public int hashCode() {
		int hash = HashUtil.hash(0, _customProperties);

		hash = HashUtil.hash(hash, _defaultValue);
		hash = HashUtil.hash(hash, _fieldType);
		hash = HashUtil.hash(hash, _id);
		hash = HashUtil.hash(hash, _indexable);
		hash = HashUtil.hash(hash, _indexType);
		hash = HashUtil.hash(hash, _label);
		hash = HashUtil.hash(hash, _localizable);
		hash = HashUtil.hash(hash, _name);
		hash = HashUtil.hash(hash, _nestedSPIDataDefinitionFields);
		hash = HashUtil.hash(hash, _readOnly);
		hash = HashUtil.hash(hash, _repeatable);
		hash = HashUtil.hash(hash, _required);
		hash = HashUtil.hash(hash, _showlabel);
		hash = HashUtil.hash(hash, _tip);

		return HashUtil.hash(hash, _visible);
	}

	public void setCustomProperties(Map<String, Object> customProperties) {
		if (customProperties != null) {
			_customProperties.putAll(customProperties);
		}
	}

	public void setDefaultValue(Map<String, Object> defaultValue) {
		if (defaultValue != null) {
			_defaultValue.putAll(defaultValue);
		}
	}

	public void setFieldType(String fieldType) {
		_fieldType = fieldType;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setIndexable(boolean indexable) {
		_indexable = indexable;
	}

	public void setIndexType(String indexType) {
		_indexType = indexType;
	}

	public void setLabel(Map<String, Object> label) {
		if (label != null) {
			_label.putAll(label);
		}
	}

	public void setLocalizable(boolean localizable) {
		_localizable = localizable;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setNestedSPIDataDefinitionFields(
		SPIDataDefinitionField[] nestedSPIDataDefinitionFields) {

		if (!ArrayUtil.isEmpty(nestedSPIDataDefinitionFields)) {
			_nestedSPIDataDefinitionFields = nestedSPIDataDefinitionFields;
		}
	}

	public void setReadOnly(boolean readOnly) {
		_readOnly = readOnly;
	}

	public void setRepeatable(boolean repeatable) {
		_repeatable = repeatable;
	}

	public void setRequired(boolean required) {
		_required = required;
	}

	public void setShowlabel(boolean showlabel) {
		_showlabel = showlabel;
	}

	public void setTip(Map<String, Object> tip) {
		if (tip != null) {
			_tip.putAll(tip);
		}
	}

	public void setVisible(boolean visible) {
		_visible = visible;
	}

	private final Map<String, Object> _customProperties = new HashMap<>();
	private final Map<String, Object> _defaultValue = new HashMap<>();
	private String _fieldType;
	private long _id;
	private boolean _indexable;
	private String _indexType;
	private final Map<String, Object> _label = new HashMap<>();
	private boolean _localizable;
	private String _name;
	private SPIDataDefinitionField[] _nestedSPIDataDefinitionFields =
		new SPIDataDefinitionField[0];
	private boolean _readOnly;
	private boolean _repeatable;
	private boolean _required;
	private boolean _showlabel;
	private final Map<String, Object> _tip = new HashMap<>();
	private boolean _visible;

}