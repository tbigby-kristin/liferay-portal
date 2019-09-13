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

import moment from 'moment';
import React from 'react';
import {Link} from 'react-router-dom';
import Button from '../../components/button/Button.es';
import ListView from '../../components/list-view/ListView.es';
import {confirmDelete} from '../../utils/client.es';

const ACTIONS = [
	{
		link: item =>
			`#/custom-object/${item.dataDefinitionId}/table-views/${item.id}`,
		name: Liferay.Language.get('edit')
	},
	{
		callback: confirmDelete('/o/data-engine/v1.0/data-list-views/'),
		name: Liferay.Language.get('delete')
	}
];

const COLUMNS = [
	{
		key: 'name',
		sortable: true,
		value: Liferay.Language.get('name')
	},
	{
		key: 'dateCreated',
		sortable: true,
		value: Liferay.Language.get('create-date')
	},
	{
		asc: false,
		key: 'dateModified',
		sortable: true,
		value: Liferay.Language.get('modified-date')
	}
];

export default ({
	match: {
		params: {dataDefinitionId},
		url
	}
}) => {
	return (
		<ListView
			actions={ACTIONS}
			addButton={() => (
				<Button
					className="nav-btn nav-btn-monospaced navbar-breakpoint-down-d-none"
					href={`${url}/add`}
					symbol="plus"
					tooltip={Liferay.Language.get('new-table-view')}
				/>
			)}
			columns={COLUMNS}
			emptyState={{
				button: () => (
					<Button displayType="secondary" href={`${url}/add`}>
						{Liferay.Language.get('new-table-view')}
					</Button>
				),
				description: Liferay.Language.get(
					'create-one-or-more-tables-to-display-the-data-held-in-your-data-object'
				),
				title: Liferay.Language.get('there-are-no-table-views-yet')
			}}
			endpoint={`/o/data-engine/v1.0/data-definitions/${dataDefinitionId}/data-list-views`}
		>
			{item => ({
				...item,
				dateCreated: moment(item.dateCreated).fromNow(),
				dateModified: moment(item.dateModified).fromNow(),
				id: item.id,
				name: <Link to={`${url}/${item.id}`}>{item.name.en_US}</Link>
			})}
		</ListView>
	);
};
