/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import React from 'react';

import Panel from '../../../src/main/resources/META-INF/resources/js/shared/components/Panel.es';

describe('Panel', () => {
	let component;

	afterEach(() => {
		if (component) {
			component.unmount();
		}
	});

	it('does not render component child components without content', () => {
		component = shallow(
			<Panel>
				<Panel.Body />
				<Panel.Footer />
			</Panel>
		);

		expect(component.render()).toMatchSnapshot();
	});

	it('renders component', () => {
		component = mount(
			<Panel>
				<Panel.Header>{'Header'}</Panel.Header>
				<Panel.Body>{'Body'}</Panel.Body>
				<Panel.Footer label="Footer Label">{'Footer'}</Panel.Footer>
			</Panel>
		);

		expect(component).toMatchSnapshot();
	});

	it('renders class passed by props', () => {
		component = shallow(
			<Panel elementClass={'custom-class'}>
				<Panel.Header elementClass={'custom-class-header'}>
					{'Header'}
				</Panel.Header>
				<Panel.Body elementClass={'custom-class-body'}>
					{'Body'}
				</Panel.Body>
				<Panel.Footer elementClass={'custom-class-footer'}>
					{'Footer'}
				</Panel.Footer>
			</Panel>
		);

		expect(component.find(Panel.Header).render()).toMatchSnapshot();
	});

	it('renders header with title', () => {
		component = shallow(
			<Panel>
				<Panel.Header title={'Lorem Ipsum'}>{'Header'}</Panel.Header>
			</Panel>
		);

		expect(component.find(Panel.Header).render()).toMatchSnapshot();
	});
});
