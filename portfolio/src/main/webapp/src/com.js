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
 * Server response statuses.
 */
const NO_LOGIN_STATUS = 'no-login';

/**
 * Gets comments data from server by submitting a GET
 * request to /data. Returns the comments as a JavaScript
 * value promise. maxComments constrains the number of comments
 * returned by the server, provided it is a valid integer.
 * Otherwise, all comments are returned.
 *
 * @param {string} maxComments
 * @return {Promise<Array<Object>>}
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
 * Adds comment data to the server by submitting a POST
 * request to /data. commentText contains the string that
 * will be stored in the comment. Returns a promise with
 * an object containing the field 'success', which contains
 * 'ok' or a reason for failure.
 *
 * @param {string} commentText
 * @return {Promise<Object>}
 */
function submitCommentToServer(commentText) {
  // Package POST arguments
  const args = new URLSearchParams();
  args.append('comment-text', commentText);
  // Submit POST request
  return fetch('/data', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: args,
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
 * Deletes all comments data from server by submitting a
 * POST request to /delete-data. Returns a promise with
 * an object containing the field 'success', which contains
 * 'ok' or a reason for failure.
 *
 * @return {Promise<Object>}
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
 * Creates a comment card with the given comment poster, comment text
 * and sentiment score if it is provided.
 *
 * @param {string} commentText
 * @param {any} commentPoster
 * @param {any} sentimentScore Potential sentiment score,
 * or undefined if no score to be displayed.
 * @return {Element}
 */
function createCommentCard(
  commentText,
  commentPoster = undefined,
  sentimentScore = undefined,
) {
  const cardElement = document.createElement('div');
  cardElement.classList.add('port-card');

  if (commentPoster) {
    const cardTitle = document.createElement('div');
    cardTitle.classList.add('port-card-title');
    cardTitle.innerHTML = commentPoster + ' writes:';
    cardElement.appendChild(cardTitle);
  }

  const cardContents = document.createElement('div');
  cardContents.classList.add('port-card-contents');
  cardContents.innerHTML = commentText;

  cardElement.appendChild(cardContents);

  if (typeof sentimentScore === 'number') {
    const cardActions = document.createElement('div');
    cardActions.classList.add('port-card-actions');
    const sentimentCard = document.createElement('div');
    sentimentCard.classList.add('port-card-action-right');
    sentimentCard.innerHTML = convertSentimentScoreToEmoji(sentimentScore);
    cardActions.appendChild(sentimentCard);
    cardElement.appendChild(cardActions);
  }

  return cardElement;
}

/**
 * Adds all of the comment messages in the passed
 * object to the page as cards. The passed object
 * should be of type Array<Comment>, otherwise,
 * addCommentsToPage will throw an exception.
 *
 * @param {Array<Object>} comments
 */
function addCommentsToPage(comments) {
  // Clear existing HTML
  document.getElementById('comment-list').innerHTML = '';
  comments.forEach((comment) => {
    const newCard = createCommentCard(
      comment.commentText,
      comment.commentPoster,
      comment.sentimentScore,
    );
    document.getElementById('comment-list').appendChild(newCard);
  });
}

/**
 * Converts sentiment score in the interval
 * [-1.0, 1.0] to an appropriate emoji.
 * (Note = -1.0 is extremely negative,
 *  1.0 is extremely positive). Numbers outside
 * this interval will be rounded towards zero
 * to either -1.0 or 1.0.
 *
 * @param {number} sentimentScore
 * @return {string} A string containing a single
 * emoji character.
 */
function convertSentimentScoreToEmoji(sentimentScore) {
  // Emotions are scaled to 10 different emojis.
  const EMOJI_SENTIMENTS = [
    '😭',
    '😢',
    '😥',
    '😞',
    '😕',
    '😐',
    '😊',
    '😀',
    '😁',
    '😇',
  ];
  // Clamp to [-1, 1]
  const score = Math.max(-1, Math.min(1, sentimentScore));

  // Scale score to an integer in 0..9.
  const emojiIndex = Math.min(9, Math.floor((score + 1.0) * 5));
  return EMOJI_SENTIMENTS[emojiIndex];
}

/**
 * Handles comment page errors by logging them and
 * notifying the user.
 *
 * @param {Error} error
 * @param {string} userMessage
 */
function handleCommentError(error, userMessage) {
  const errorCard = createCommentCard('<b>' + userMessage + '</b>');
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
    .catch((error) =>
      handleCommentError(error, 'Unable to fetch comments from server'),
    );
}

/**
 * Adds a comment to the server, taking text from the input
 * form.
 *
 */
function handleAddComment() {
  const el = document.getElementById('comment-text');
  const commentText = el.value;
  el.value = '';
  submitCommentToServer(commentText)
    .then((response) => {
      // Displays login error message.
      if (response.status === NO_LOGIN_STATUS) {
        document.getElementById('no-login-modal-message').innerHTML =
          'You must be logged in to submit a comment.';
        // Show message.
        document.getElementById('no-login-modal').style.display = 'flex';
      }
      handleUpdateComments();
    })
    .catch((error) =>
      handleCommentError(error, 'Unable to add comment to server'),
    );
}

/**
 * Deletes all comments from the server and reloads the page
 *
 */
function handleDeleteAllComments() {
  deleteAllCommentsFromServer()
    .then((response) => {
      // Displays login error message.
      if (response.status === NO_LOGIN_STATUS) {
        document.getElementById('no-login-modal-message').innerHTML =
          'You must be logged in to delete all comments.';
        // Show message.
        document.getElementById('no-login-modal').removeAttribute('hidden');
      }
      handleUpdateComments();
    })
    .catch((error) =>
      handleCommentError(error, 'Unable to delete comments from server'),
    );
}

/** Add handlers to button and select elements */
document
  .getElementById('comment-delete')
  .addEventListener('click', handleDeleteAllComments);

document
  .getElementById('comment-submit')
  .addEventListener('click', handleAddComment);

/** Add 'enter' keystroke listener to comment input field */
document.getElementById('comment-text').addEventListener('keyup', (event) => {
  if (event.key === 'Enter') {
    handleAddComment();
  }
});

/** Change in number of comments displayed per page */
document
  .getElementById('comment-number')
  .addEventListener('change', handleUpdateComments);

/** Not Logged In modal close */
document
  .getElementById('no-login-modal-close')
  .addEventListener(
    'click',
    () => (document.getElementById('no-login-modal').setAttribute('hidden', '')),
  );

/** Once the page loads, request comments */
handleUpdateComments();
