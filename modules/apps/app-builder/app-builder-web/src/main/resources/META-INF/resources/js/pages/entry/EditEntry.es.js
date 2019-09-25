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

import {PagesVisitor} from 'dynamic-data-mapping-form-renderer/js/util/visitors.es';
import React, {useContext} from 'react';
import {AppContext} from '../../AppContext.es';
import Button from '../../components/button/Button.es';
import {addItem} from '../../utils/client.es';

export default ({dataDefinitionId, editEntryContainerElementId}) => {
	const {basePortletURL} = useContext(AppContext);

	const onCancel = () => {
		Liferay.Util.navigate(basePortletURL);
	};

	const onSave = () => {
		const {pages} = Liferay.component(editEntryContainerElementId);
		const visitor = new PagesVisitor(pages);

		const dataRecords = {
			dataRecordValues: {}
		};

		visitor.mapFields(({fieldName, value}) => {
			dataRecords.dataRecordValues[fieldName] = value;
		});

		addItem(
			`/o/data-engine/v1.0/data-definitions/${dataDefinitionId}/data-records`,
			dataRecords
		).then(onCancel);
	};

	return <Button onClick={onSave}>{Liferay.Language.get('save')}</Button>;
};
