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

import {openToast} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React, {useState, useContext} from 'react';

import AppContext from '../../../core/AppContext';
import CommentForm from './CommentForm';

function addFragmentEntryLinkComment() {
	throw new Error('Not implemented');
}

export default function AddCommentForm({fragmentEntryLinkId}) {
	const [addingComment, setAddingComment] = useState(false);
	const [showButtons, setShowButtons] = useState(false);
	const [textareaContent, setTextareaContent] = useState('');
	const dispatch = useContext(AppContext);

	const _handleCancelButtonClick = () => {
		setShowButtons(false);
		setTextareaContent('');
	};

	const _handleFormFocus = () => {
		setShowButtons(true);
	};

	const _handleCommentButtonClick = () => {
		setAddingComment(true);

		addFragmentEntryLinkComment(fragmentEntryLinkId, textareaContent)
			.then(comment => {
				dispatch({
					comment,
					fragmentEntryLinkId,
					type: 'addComment'
				});

				setAddingComment(false);
				setShowButtons(false);
				setTextareaContent('');
			})
			.catch(() => {
				openToast({
					message: Liferay.Language.get(
						'the-comment-could-not-be-saved'
					),
					title: Liferay.Language.get('error'),
					type: 'danger'
				});

				setAddingComment(false);
			});
	};

	const _handleTextareaChange = content => {
		if (content) {
			setTextareaContent(content);
		}
	};

	return (
		<div className="px-3">
			<CommentForm
				id="pageEditorCommentEditor"
				loading={addingComment}
				onCancelButtonClick={_handleCancelButtonClick}
				onFormFocus={_handleFormFocus}
				onSubmitButtonClick={_handleCommentButtonClick}
				onTextareaChange={_handleTextareaChange}
				showButtons={showButtons}
				submitButtonLabel={Liferay.Language.get('comment')}
				textareaContent={textareaContent}
			/>
		</div>
	);
}

AddCommentForm.propTypes = {
	fragmentEntryLinkId: PropTypes.string.isRequired
};
