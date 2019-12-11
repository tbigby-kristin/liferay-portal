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

import ClayButton from '@clayui/button';
import ClayForm, {ClaySelectWithOption} from '@clayui/form';
import React from 'react';

/**
 * Renders Layout Background Image Configuration Panel.
 * *WARNING:* This component is unfinished since this is just used
 * to test FloatingToolbar ConfigurationPanel mechanism.
 */
export const LayoutBackgroundImageConfigurationPanel = () => {
	return (
		<div className="floating-toolbar-layout-background-image-panel">
			<ClayForm.Group>
				<label htmlFor="floatingToolbarLayoutBackgroundImagePanelImageSourceTypeSelect">
					{Liferay.Language.get('image-source')}
				</label>
				<ClaySelectWithOption
					aria-label={Liferay.Language.get('image-source')}
					defaultValue="manual_selection"
					id="floatingToolbarLayoutBackgroundImagePanelImageSourceTypeSelect"
					options={[
						{
							label: Liferay.Language.get('manual-selection'),
							value: 'manual_selection'
						},
						{
							label: Liferay.Language.get('content-mapping'),
							value: 'content_mapping'
						}
					]}
				/>
			</ClayForm.Group>
			<ClayForm.Group />
			<ClayForm.Group>
				<ClayButton.Group>
					<div className="btn-group-item">
						<ClayButton displayType="secondary" small>
							{Liferay.Language.get('select')}
						</ClayButton>
					</div>
					<div className="btn-group-item">
						<ClayButton borderless displayType="secondary" small>
							{Liferay.Language.get('clear')}
						</ClayButton>
					</div>
				</ClayButton.Group>
			</ClayForm.Group>
		</div>
	);
};
