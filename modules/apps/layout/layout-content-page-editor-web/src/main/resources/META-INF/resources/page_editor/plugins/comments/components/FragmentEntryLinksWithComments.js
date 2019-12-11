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

import React, {useContext} from 'react';

import {LAYOUT_DATA_ITEM_TYPES} from '../../../app/config/constants/layoutDataItemTypes';
import {StoreContext} from '../../../app/store/index';
import SidebarPanelContent from '../../../common/components/SidebarPanelContent';
import SidebarPanelHeader from '../../../common/components/SidebarPanelHeader';
import AppContext from '../../../core/AppContext';
import NoCommentsMessage from './NoCommentsMessage';
import ResolvedCommentsToggle from './ResolvedCommentsToggle';

export default function FragmentEntryLinksWithComments() {
	const {dispatch} = useContext(AppContext);

	const {fragmentEntryLinks, layoutData, showResolvedComments} = useContext(
		StoreContext
	);

	const fragmentEntryLinksWithComments = Object.values(layoutData.items)
		.filter(item => item.type === LAYOUT_DATA_ITEM_TYPES.fragment)
		.map(item => fragmentEntryLinks[item.config.fragmentEntryLinkId])
		.filter(
			({comments}) =>
				comments &&
				comments.length &&
				(showResolvedComments ||
					comments.some(({resolved}) => !resolved))
		);

	const setActiveFragmentEntryLink = fragmentEntryLinkId => () => {
		dispatch({
			itemId: Object.values(layoutData.items).find(
				item => item.config.fragmentEntryLinkId === fragmentEntryLinkId
			),
			type: 'updateActiveItem'
		});

		const fragmentEntryLinkElement = null;

		if (fragmentEntryLinkElement) {
			fragmentEntryLinkElement.scrollIntoView({
				behavior: 'smooth',
				block: 'center'
			});
		}
	};

	const setHoveredFragmentEntryLink = fragmentEntryLinkId => () => {
		dispatch({
			itemId: Object.values(layoutData.items).find(
				item => item.config.fragmentEntryLinkId === fragmentEntryLinkId
			),
			type: 'updateHoveredItem'
		});
	};

	const getFragmentEntryLinkItem = ({
		comments,
		fragmentEntryLinkId,
		name
	}) => {
		const commentCount = (showResolvedComments
			? comments
			: comments.filter(({resolved}) => !resolved)
		).length;

		return (
			<a
				className="border-0 list-group-item list-group-item-action"
				href={`#${fragmentEntryLinkId}`}
				key={fragmentEntryLinkId}
				onClick={setActiveFragmentEntryLink(fragmentEntryLinkId)}
				onFocus={setHoveredFragmentEntryLink(fragmentEntryLinkId)}
				onMouseOver={setHoveredFragmentEntryLink(fragmentEntryLinkId)}
			>
				<strong className="d-block text-dark">{name}</strong>

				<span className="text-secondary">
					{Liferay.Util.sub(
						commentCount === 1
							? Liferay.Language.get('x-comment')
							: Liferay.Language.get('x-comments'),
						commentCount
					)}
				</span>
			</a>
		);
	};

	return (
		<>
			<SidebarPanelHeader>
				{Liferay.Language.get('comments')}
			</SidebarPanelHeader>

			<SidebarPanelContent padded={false}>
				<ResolvedCommentsToggle />

				{fragmentEntryLinksWithComments.length ? (
					<nav className="list-group">
						{fragmentEntryLinksWithComments.map(
							getFragmentEntryLinkItem
						)}
					</nav>
				) : (
					<NoCommentsMessage />
				)}
			</SidebarPanelContent>
		</>
	);
}
