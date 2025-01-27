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

import React from 'react';
import {HashRouter as Router, Route, Switch} from 'react-router-dom';

import {AppContextProvider} from './AppContext.es';
import NavigationBar from './pages/NavigationBar.es';
import EditAnswer from './pages/answers/EditAnswer.es';
import Keywords from './pages/keywords/Keywords.es';
import EditQuestion from './pages/questions/EditQuestion.es';
import NewQuestion from './pages/questions/NewQuestion.es';
import Question from './pages/questions/Question.es';
import Questions from './pages/questions/Questions.es';

export default props => {
	return (
		<AppContextProvider {...props}>
			<Router>
				<div>
					<NavigationBar />

					<Switch>
						<Route
							exact
							path="/"
							render={props => (
								<Questions {...props} keyword={''} />
							)}
						/>
						<Route
							component={EditAnswer}
							path="/answers/:answerId/edit"
						/>
						<Route
							path="/questions/keyword/:keyword"
							render={props => (
								<Questions
									{...props}
									keyword={props.match.params.keyword}
								/>
							)}
						/>
						<Route
							path="/questions/creator/:creatorId"
							render={props => (
								<Questions
									{...props}
									keyword={props.match.params.creatorId}
								/>
							)}
						/>
						<Route
							component={EditQuestion}
							exact
							path="/questions/:questionId/edit"
						/>
						<Route component={Questions} exact path="/questions" />
						<Route
							component={NewQuestion}
							exact
							path="/questions/new"
						/>
						<Route
							component={Question}
							path="/questions/:questionId"
						/>
						<Route component={Keywords} path="/keywords" />
					</Switch>
				</div>
			</Router>
		</AppContextProvider>
	);
};
