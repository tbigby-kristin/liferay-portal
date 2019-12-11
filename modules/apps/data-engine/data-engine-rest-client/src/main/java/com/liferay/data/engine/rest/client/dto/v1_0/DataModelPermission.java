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

package com.liferay.data.engine.rest.client.dto.v1_0;

import com.liferay.data.engine.rest.client.function.UnsafeSupplier;
import com.liferay.data.engine.rest.client.serdes.v1_0.DataModelPermissionSerDes;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Jeyvison Nascimento
 * @generated
 */
@Generated("")
public class DataModelPermission {

	public String[] getActionIds() {
		return actionIds;
	}

	public void setActionIds(String[] actionIds) {
		this.actionIds = actionIds;
	}

	public void setActionIds(
		UnsafeSupplier<String[], Exception> actionIdsUnsafeSupplier) {

		try {
			actionIds = actionIdsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String[] actionIds;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public void setRoleName(
		UnsafeSupplier<String, Exception> roleNameUnsafeSupplier) {

		try {
			roleName = roleNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String roleName;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DataModelPermission)) {
			return false;
		}

		DataModelPermission dataModelPermission = (DataModelPermission)object;

		return Objects.equals(toString(), dataModelPermission.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DataModelPermissionSerDes.toJSON(this);
	}

}