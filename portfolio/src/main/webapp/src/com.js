// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// File Modified by Cody Rivera June 2020

/**
 * @fileoverview Javascript for home page. Loads the top bar
 * and Javascript components
 *
 * @package
 */

import { MDCTopAppBar } from '@material/top-app-bar';
import { MDCRipple } from '@material/ripple';
import { createTopBar, createFloatingLinks } from './__common';

const pageName = 'com';

/** Inserts the top bar */
const topBarElement = createTopBar(pageName);
document.getElementById('header').replaceWith(topBarElement);
new MDCTopAppBar(document.getElementsByTagName('header')[0]);

/** Inserts the floating link buttons */
const floatingElement = createFloatingLinks();
document.getElementById('links').replaceWith(floatingElement);

/** Adds ripple effect to buttons */
new MDCRipple(document.querySelector('.mdc-button'));
new MDCRipple(document.querySelector('.mdc-fab'));

/**
 * Gets comments data from server by submitting a GET
 * request to /data. Returns the comments as a JavaScript
 * value promise. maxComments constrains the number of comments
 * returned by the server, provided it is a valid integer.
 * Otherwise, all comments are returned.
 *
 * @param {String} maxComments
 * @return {Promise<any>}
 */
function getCommentsFromServer(maxComments) {
  return fetch('/data' + '?max-comments=' + maxComments).then((response) => {
    if (response.ok) {
      return response.json();
    } else {
      return Promise.reject(
        new Error(response.status + ': ' + response.statusText),
      );
    }
  });
}

/**
 * Deletes all comments data from server by submitting a
 * POST request to /delete-data. Returns a promise with
 * an undefined value on success.
 *
 * @return {Promise<any>}
 */
function deleteAllCommentsFromServer() {
  return fetch('/delete-data', {
    method: 'POST',
  }).then((response) => {
    if (response.ok) {
      return response.json();
    } else {
      return Promise.reject(
        new Error(response.status + ': ' + response.statusText),
      );
    }
  });
}

/**
 * Creates a comment card with the given comment text
 *
 * @param {String} commentText
 * @return {Element}
 */
function createCommentCard(commentText) {
  const cardElement = document.createElement('div');
  cardElement.classList.add('port-card');

  const cardContents = document.createElement('div');
  cardContents.classList.add('port-card-contents');
  cardContents.innerHTML = commentText;

  cardElement.appendChild(cardContents);
  return cardElement;
}

/**
 * Adds all of the comment messages in the passed
 * object to the page as cards. The passed object
 * should be of type Array<String>, otherwise,
 * addCommentsToPage will throw an exception.
 *
 * @param {any} comments
 */
function addCommentsToPage(comments) {
  // Clear existing HTML
  document.getElementById('comment-list').innerHTML = '';
  comments.forEach((comment) => {
    const newCard = createCommentCard(comment);
    document.getElementById('comment-list').appendChild(newCard);
  });
}

/**
 * Handles comment page errors by logging them and
 * notifying the user.
 *
 * @param error {any}
 * @param userMessage {String}
 */
function handleCommentError(error, userMessage) {
  const errorCard = createCommentCard(
    '<b>' + userMessage + '</b>',
  );
  document.getElementById('comment-list').innerHTML = '';
  document.getElementById('comment-list').appendChild(errorCard);
  console.error(error);
}

/**
 * Fetches comments from the server and places them on the page
 * as cards. Otherwise, puts an error card on the page.
 *
 */
function handleUpdateComments() {
  // Extract max comments per page from drop-down
  const el = document.getElementById('comment-number');
  const maxComments = el.options[el.selectedIndex].value;

  getCommentsFromServer(maxComments)
    .then(addCommentsToPage)
    .catch((error) => handleCommentError(
      error,
      "Unable to fetch comments from server"
    ));
}

/**
 * Deletes all comments from the server and reloads the page
 *
 */
function handleDeleteAllComments() {
  deleteAllCommentsFromServer()
    .then(handleUpdateComments)
    .catch((error) => handleCommentError(
      error,
      "Unable to delete comments from server"
    ));
}

/** Add handlers to button and select elements */
document
  .getElementById('comment-delete')
  .addEventListener('click', handleDeleteAllComments);
/** Change in number of comments displayed per page */
document
  .getElementById('comment-number')
  .addEventListener('change', handleUpdateComments);

/** Once the page loads, request comments */
handleUpdateComments();
