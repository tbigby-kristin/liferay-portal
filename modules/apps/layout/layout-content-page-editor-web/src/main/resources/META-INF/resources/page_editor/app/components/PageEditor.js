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

import classNames from 'classnames';
import {useIsMounted} from 'frontend-js-react-web';
import React, {useContext, useEffect, useRef} from 'react';

import FloatingToolbar from '../components/FloatingToolbar';
import {LAYOUT_DATA_FLOATING_TOOLBAR_BUTTONS} from '../config/constants/layoutDataFloatingToolbarButtons';
import {LAYOUT_DATA_ITEM_TYPES} from '../config/constants/layoutDataItemTypes';
import {ConfigContext} from '../config/index';
import {DispatchContext} from '../reducers/index';
import {StoreContext} from '../store/index';
import updateLayoutData from '../thunks/updateLayoutData';
import {useIsActive} from './Controls';
import Topper from './Topper';
import UnsafeHTML from './UnsafeHTML';

const Root = React.forwardRef(({canDrop, children, isOver}, ref) => (
	<div
		className={classNames('page-editor__root', {
			'page-editor__root--active': isOver && canDrop
		})}
		ref={ref}
	>
		{React.Children.count(children) ? (
			children
		) : (
			<div className="taglib-empty-result-message">
				<div className="taglib-empty-result-message-header"></div>
				<div className="text-center text-muted">
					{Liferay.Language.get('place-fragments-here')}
				</div>
			</div>
		)}
	</div>
));

const Container = React.forwardRef(({children, item}, ref) => {
	const {
		backgroundColorCssClass,
		backgroundImage,
		paddingHorizontal,
		paddingVertical,
		type
	} = item.config;

	return (
		<div
			className={classNames(
				`container page-editor__container py-${paddingVertical}`,
				{
					[`bg-${backgroundColorCssClass}`]: !!backgroundColorCssClass,
					container: type === 'fixed',
					'container-fluid': type === 'fluid',
					empty: !item.children.length,
					[`px-${paddingHorizontal}`]: paddingHorizontal !== 3
				}
			)}
			ref={ref}
			style={
				backgroundImage
					? {
							backgroundImage: `url(${backgroundImage})`,
							backgroundPosition: '50% 50%',
							backgroundRepeat: 'no-repeat',
							backgroundSize: 'cover'
					  }
					: {}
			}
		>
			<div className="page-editor__container-outline">{children}</div>
		</div>
	);
});

const Row = React.forwardRef(({children, item, layoutData}, ref) => {
	const parent = layoutData.items[item.parentId];

	const rowContent = (
		<div className="page-editor__row-outline" ref={ref}>
			<div
				className={classNames('page-editor__row row', {
					empty: !item.children.some(
						childId => layoutData.items[childId].children.length
					),
					'no-gutters': !item.config.gutters
				})}
			>
				{children}
			</div>
		</div>
	);

	return !parent || parent.type === LAYOUT_DATA_ITEM_TYPES.root ? (
		<div className="container-fluid p-0">{rowContent}</div>
	) : (
		rowContent
	);
});

const Column = React.forwardRef(({children, className, item}, ref) => {
	const {size} = item.config;

	return (
		<div
			className={classNames(className, 'col', {[`col-${size}`]: size})}
			ref={ref}
		>
			{children}
		</div>
	);
});

const Fragment = React.forwardRef(({item}, ref) => {
	const {fragmentEntryLinks} = useContext(StoreContext);

	const fragmentEntryLink =
		fragmentEntryLinks[item.config.fragmentEntryLinkId];

	let markup = '';

	if (typeof fragmentEntryLink.content === 'string') {
		markup = fragmentEntryLink.content;
	} else if (
		fragmentEntryLink.content.value &&
		fragmentEntryLink.content.value.content
	) {
		markup = fragmentEntryLink.content.value.content;
	} else {
		markup = `<div>No markup from ${item.config.fragmentEntryLinkId}</div>`;
	}

	return (
		<UnsafeHTML
			className="page-editor__fragment"
			markup={markup}
			ref={ref}
		/>
	);
});

const LAYOUT_DATA_ITEMS = {
	[LAYOUT_DATA_ITEM_TYPES.column]: Column,
	[LAYOUT_DATA_ITEM_TYPES.container]: Container,
	[LAYOUT_DATA_ITEM_TYPES.fragment]: Fragment,
	[LAYOUT_DATA_ITEM_TYPES.root]: Root,
	[LAYOUT_DATA_ITEM_TYPES.row]: Row
};

const LAYOUT_DATA_ACCEPT_DROP_TYPES = {
	[LAYOUT_DATA_ITEM_TYPES.column]: [LAYOUT_DATA_ITEM_TYPES.fragment],
	[LAYOUT_DATA_ITEM_TYPES.container]: [
		LAYOUT_DATA_ITEM_TYPES.container,
		LAYOUT_DATA_ITEM_TYPES.fragment,
		LAYOUT_DATA_ITEM_TYPES.row
	],
	[LAYOUT_DATA_ITEM_TYPES.fragment]: [
		LAYOUT_DATA_ITEM_TYPES.fragment,
		LAYOUT_DATA_ITEM_TYPES.container,
		LAYOUT_DATA_ITEM_TYPES.row
	],
	[LAYOUT_DATA_ITEM_TYPES.root]: [
		LAYOUT_DATA_ITEM_TYPES.fragment,
		LAYOUT_DATA_ITEM_TYPES.container,
		LAYOUT_DATA_ITEM_TYPES.row
	],
	[LAYOUT_DATA_ITEM_TYPES.row]: [
		LAYOUT_DATA_ITEM_TYPES.container,
		LAYOUT_DATA_ITEM_TYPES.fragment,
		LAYOUT_DATA_ITEM_TYPES.row
	]
};

const LAYOUT_DATA_TOPPER_ACTIVE = {
	[LAYOUT_DATA_ITEM_TYPES.column]: false,
	[LAYOUT_DATA_ITEM_TYPES.container]: true,
	[LAYOUT_DATA_ITEM_TYPES.fragment]: true,
	[LAYOUT_DATA_ITEM_TYPES.root]: false,
	[LAYOUT_DATA_ITEM_TYPES.row]: true
};

const LAYOUT_DATA_FLOATING_TOOLBAR_TYPES = {
	[LAYOUT_DATA_ITEM_TYPES.column]: [],
	[LAYOUT_DATA_ITEM_TYPES.container]: [
		LAYOUT_DATA_FLOATING_TOOLBAR_BUTTONS.backgroundColor,
		LAYOUT_DATA_FLOATING_TOOLBAR_BUTTONS.layoutBackgroundImage,
		LAYOUT_DATA_FLOATING_TOOLBAR_BUTTONS.spacing
	],
	[LAYOUT_DATA_ITEM_TYPES.fragment]: [
		LAYOUT_DATA_FLOATING_TOOLBAR_BUTTONS.fragmentConfiguration
	],
	[LAYOUT_DATA_ITEM_TYPES.root]: [],
	[LAYOUT_DATA_ITEM_TYPES.row]: [LAYOUT_DATA_FLOATING_TOOLBAR_BUTTONS.spacing]
};

const LayoutDataItem = ({fragmentEntryLinks, item, layoutData}) => {
	const Component = LAYOUT_DATA_ITEMS[item.type];
	const floatingToolbarButtons =
		LAYOUT_DATA_FLOATING_TOOLBAR_TYPES[item.type];
	const isActive = useIsActive()(item.itemId);
	const isActiveTopper = LAYOUT_DATA_TOPPER_ACTIVE[item.type];
	const isMounted = useIsMounted();
	const componentRef = useRef(null);

	const fragmentEntryLink = fragmentEntryLinks[
		item.config.fragmentEntryLinkId
	] || {name: item.type};

	useEffect(() => {
		if (isActive && componentRef.current && isMounted()) {
			componentRef.current.scrollIntoView({
				behavior: 'smooth',
				block: 'nearest',
				inline: 'nearest'
			});
		}
	}, [componentRef, isActive, isMounted]);

	return (
		<Topper
			acceptDrop={LAYOUT_DATA_ACCEPT_DROP_TYPES[item.type]}
			active={isActiveTopper}
			item={item}
			layoutData={layoutData}
			name={fragmentEntryLink.name}
		>
			{({canDrop, isOver}) => (
				<>
					{floatingToolbarButtons.length > 0 && (
						<FloatingToolbar
							buttons={floatingToolbarButtons}
							item={item}
							itemRef={componentRef}
						/>
					)}

					<Component
						canDrop={canDrop}
						isOver={isOver}
						item={item}
						layoutData={layoutData}
						ref={componentRef}
					>
						{item.children.map(childId => {
							return (
								<LayoutDataItem
									fragmentEntryLinks={fragmentEntryLinks}
									item={layoutData.items[childId]}
									key={childId}
									layoutData={layoutData}
								/>
							);
						})}
					</Component>
				</>
			)}
		</Topper>
	);
};

export default function PageEditor() {
	const config = useContext(ConfigContext);
	const dispatch = useContext(DispatchContext);
	const {fragmentEntryLinks, layoutData, segmentsExperienceId} = useContext(
		StoreContext
	);

	const mainItem = layoutData.items[layoutData.rootItems.main];

	const isMounted = useIsMounted();
	const shouldUpdateLayoutData = useRef();

	useEffect(() => {
		if (isMounted()) {
			// Only run this after first render
			if (!shouldUpdateLayoutData.current) {
				shouldUpdateLayoutData.current = true;
			} else {
				dispatch(
					updateLayoutData({
						config,
						layoutData,
						segmentsExperienceId
					})
				);
			}
		}
	}, [config, dispatch, isMounted, layoutData, segmentsExperienceId]);

	return (
		<LayoutDataItem
			fragmentEntryLinks={fragmentEntryLinks}
			item={mainItem}
			layoutData={layoutData}
		/>
	);
}
