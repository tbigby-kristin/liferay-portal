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

package com.liferay.account.model.impl;

import com.liferay.account.model.AccountEntryUserRel;
import com.liferay.account.service.persistence.AccountEntryUserRelPK;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The cache model class for representing AccountEntryUserRel in entity cache.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class AccountEntryUserRelCacheModel
	implements CacheModel<AccountEntryUserRel>, Externalizable {

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof AccountEntryUserRelCacheModel)) {
			return false;
		}

		AccountEntryUserRelCacheModel accountEntryUserRelCacheModel =
			(AccountEntryUserRelCacheModel)obj;

		if (accountEntryUserRelPK.equals(
				accountEntryUserRelCacheModel.accountEntryUserRelPK)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return HashUtil.hash(0, accountEntryUserRelPK);
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(9);

		sb.append("{accountEntryUserRelId=");
		sb.append(accountEntryUserRelId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", accountEntryId=");
		sb.append(accountEntryId);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public AccountEntryUserRel toEntityModel() {
		AccountEntryUserRelImpl accountEntryUserRelImpl =
			new AccountEntryUserRelImpl();

		accountEntryUserRelImpl.setAccountEntryUserRelId(accountEntryUserRelId);
		accountEntryUserRelImpl.setCompanyId(companyId);
		accountEntryUserRelImpl.setUserId(userId);
		accountEntryUserRelImpl.setAccountEntryId(accountEntryId);

		accountEntryUserRelImpl.resetOriginalValues();

		return accountEntryUserRelImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		accountEntryUserRelId = objectInput.readLong();

		companyId = objectInput.readLong();

		userId = objectInput.readLong();

		accountEntryId = objectInput.readLong();

		accountEntryUserRelPK = new AccountEntryUserRelPK(
			accountEntryUserRelId, userId, accountEntryId);
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(accountEntryUserRelId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(userId);

		objectOutput.writeLong(accountEntryId);
	}

	public long accountEntryUserRelId;
	public long companyId;
	public long userId;
	public long accountEntryId;
	public transient AccountEntryUserRelPK accountEntryUserRelPK;

}