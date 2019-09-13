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

package com.liferay.account.model;

import com.liferay.account.service.persistence.AccountEntryUserRelPK;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by SOAP remote services, specifically {@link com.liferay.account.service.http.AccountEntryUserRelServiceSoap}.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class AccountEntryUserRelSoap implements Serializable {

	public static AccountEntryUserRelSoap toSoapModel(
		AccountEntryUserRel model) {

		AccountEntryUserRelSoap soapModel = new AccountEntryUserRelSoap();

		soapModel.setAccountEntryUserRelId(model.getAccountEntryUserRelId());
		soapModel.setCompanyId(model.getCompanyId());
		soapModel.setUserId(model.getUserId());
		soapModel.setAccountEntryId(model.getAccountEntryId());

		return soapModel;
	}

	public static AccountEntryUserRelSoap[] toSoapModels(
		AccountEntryUserRel[] models) {

		AccountEntryUserRelSoap[] soapModels =
			new AccountEntryUserRelSoap[models.length];

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModel(models[i]);
		}

		return soapModels;
	}

	public static AccountEntryUserRelSoap[][] toSoapModels(
		AccountEntryUserRel[][] models) {

		AccountEntryUserRelSoap[][] soapModels = null;

		if (models.length > 0) {
			soapModels =
				new AccountEntryUserRelSoap[models.length][models[0].length];
		}
		else {
			soapModels = new AccountEntryUserRelSoap[0][0];
		}

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModels(models[i]);
		}

		return soapModels;
	}

	public static AccountEntryUserRelSoap[] toSoapModels(
		List<AccountEntryUserRel> models) {

		List<AccountEntryUserRelSoap> soapModels =
			new ArrayList<AccountEntryUserRelSoap>(models.size());

		for (AccountEntryUserRel model : models) {
			soapModels.add(toSoapModel(model));
		}

		return soapModels.toArray(
			new AccountEntryUserRelSoap[soapModels.size()]);
	}

	public AccountEntryUserRelSoap() {
	}

	public AccountEntryUserRelPK getPrimaryKey() {
		return new AccountEntryUserRelPK(
			_accountEntryUserRelId, _userId, _accountEntryId);
	}

	public void setPrimaryKey(AccountEntryUserRelPK pk) {
		setAccountEntryUserRelId(pk.accountEntryUserRelId);
		setUserId(pk.userId);
		setAccountEntryId(pk.accountEntryId);
	}

	public long getAccountEntryUserRelId() {
		return _accountEntryUserRelId;
	}

	public void setAccountEntryUserRelId(long accountEntryUserRelId) {
		_accountEntryUserRelId = accountEntryUserRelId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public long getUserId() {
		return _userId;
	}

	public void setUserId(long userId) {
		_userId = userId;
	}

	public long getAccountEntryId() {
		return _accountEntryId;
	}

	public void setAccountEntryId(long accountEntryId) {
		_accountEntryId = accountEntryId;
	}

	private long _accountEntryUserRelId;
	private long _companyId;
	private long _userId;
	private long _accountEntryId;

}