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

import React, {useContext, useEffect, useState} from 'react';
import SearchContext from './SearchContext.es';
import Button from '../../../components/button/Button.es';

export default ({disabled}) => {
	const {
		dispatch,
		state: {query}
	} = useContext(SearchContext);

	const [keywords, setKeywords] = useState('');

	useEffect(() => {
		setKeywords(query.keywords);
	}, [query.keywords]);

	const onChange = event => {
		setKeywords(event.target.value);
	};

	const handleSubmit = () => {
		dispatch({keywords: keywords.trim(), type: 'SEARCH'});
	};

	return (
		<div className="navbar-form navbar-form-autofit navbar-overlay navbar-overlay-sm-down">
			<div className="container-fluid container-fluid-max-xl">
				<form
					onSubmit={event => {
						event.preventDefault();

						handleSubmit();
					}}
				>
					<div className="input-group">
						<div className="input-group-item">
							<input
								aria-label={Liferay.Language.get('search-for')}
								className="form-control input-group-inset input-group-inset-after"
								disabled={disabled}
								onChange={onChange}
								placeholder={Liferay.Language.get('search-for')}
								type="text"
								value={keywords}
							/>

							<div className="input-group-inset-item input-group-inset-item-after">
								<Button
									disabled={disabled}
									displayType="unstyled"
									onClick={handleSubmit}
									symbol="search"
								/>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	);
};
