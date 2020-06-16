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
 * @fileoverview This module provides functions and data used throughout the
 * frontend, including creation of elements common across all pages such as
 * the top bar.
 *
 * @module
 */

/** Social media icon imports */
import github from 'simple-icons/icons/github';
import linkedin from 'simple-icons/icons/linkedin';

import { Globals } from './__globals';

export { createTopBar, createFloatingLinks };

/**
 * Creates a top bar tailored for the specific page.
 * This is returned as a DOM element, that is then
 * put into the page and activated
 * by the calling script.
 *
 * The bar will be based on the Material top-app-bar.
 * @param {string} currentPage
 * @return {Element}
 */
function createTopBar(currentPage) {
  const newTopBar = document.createElement('header');
  newTopBar.classList.add('mdc-top-app-bar', 'mdc-top-app-bar--fixed');

  const newTopBarDiv = document.createElement('div');
  newTopBarDiv.classList.add('mdc-top-app-bar__row');

  // Generate top bar title and contents separately
  const topBarTitle = createTopBarTitle(Globals.pageTitle);
  const topBarContents = createTopBarContents(currentPage);
  newTopBarDiv.appendChild(topBarTitle);
  newTopBarDiv.appendChild(topBarContents);

  newTopBar.appendChild(newTopBarDiv);
  return newTopBar;
}

/**
 * Constructs the title element of the top bar.
 *
 * @param {string} title
 * @return {Element}
 */
function createTopBarTitle(title) {
  const topBarTitle = document.createElement('section');
  topBarTitle.classList.add(
    'mdc-top-app-bar__section',
    'mdc-top-app-bar__section--align-start',
  );

  // Creates actual title element -- with link to homepage
  const titleElement = createButtonLink(title, 'index.html');
  titleElement.classList.add('mdc-top-app-bar__action-item');

  topBarTitle.appendChild(titleElement);
  return topBarTitle;
}

/**
 * Constructs the remainder elements of the top bar.
 * If the current page is linked to by one of these
 * elements, that particular element will be styled
 * differently from the other elements.
 *
 * @param {string} currentPage
 * @return {Element}
 */
function createTopBarContents(currentPage) {
  const topBarTitle = document.createElement('section');
  topBarTitle.classList.add(
    'mdc-top-app-bar__section',
    'mdc-top-app-bar__section--align-end',
  );

  // Adds top bar elements for each page in the website
  Globals.pageNames.forEach((name) => {
    const newElement = createButtonLink(
      Globals.pageNameMap[name],
      name + '.html',
    );
    newElement.classList.add('mdc-top-app-bar__action-item');
    // Extra styling to current page's icon
    if (name === currentPage) {
      newElement.classList.add('mdc-top-app-bar--active');
    }
    topBarTitle.appendChild(newElement);
  });

  // Determines whether the user is logged in or logged out,
  // and displays the appropriate top bar element.
  topBarTitle.appendChild(createLoginStatusBox());

  return topBarTitle;
}

/**
 * Constructs a Material button element that is not
 * a link.
 *
 * @param {string} buttonLabel
 * @return {Element}
 */
function createButton(buttonLabel) {
  const buttonElement = document.createElement('div');
  buttonElement.classList.add('mdc-button');
  const rippleElement = document.createElement('div');
  rippleElement.classList.add('mdc-button__ripple');
  const textElement = document.createElement('span');
  textElement.classList.add('mdc-button__label');
  textElement.innerHTML = buttonLabel;
  buttonElement.appendChild(rippleElement);
  buttonElement.appendChild(textElement);
  return buttonElement;
}

/**
 * Constructs a Material button element with a given
 * label that is also a link to another page.
 *
 * @param {string} buttonLabel
 * @param {string} buttonLink
 * @return {Element}
 */
function createButtonLink(buttonLabel, buttonLink) {
  const buttonElement = document.createElement('a');
  buttonElement.classList.add('mdc-button');
  buttonElement.setAttribute('href', buttonLink);
  const rippleElement = document.createElement('div');
  rippleElement.classList.add('mdc-button__ripple');
  const textElement = document.createElement('span');
  textElement.classList.add('mdc-button__label');
  textElement.innerHTML = buttonLabel;
  buttonElement.appendChild(rippleElement);
  buttonElement.appendChild(textElement);
  return buttonElement;
}

/**
 * Gets the user's login status, username, and
 * login API url from the server.
 *
 * @return {Promise<Object>}
 */
function getLoginStatus() {
  return fetch('/login').then((response) => {
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
 * Constructs a login status box, based on login information
 * It will contain a link to either a login or logout page
 * and the user's nickname if logged in.
 *
 * @return {Element}
 */
function createLoginStatusBox() {
  const boxElement = document.createElement('div');
  boxElement.classList.add('mdc-top-app-bar__action-item');
  getLoginStatus()
    .then((status) => {
      // If user is logged in, display "Hi, {nickname}"
      if (status.loggedInFlag) {
        const loggedInGreeting = createButton('Hi, ' + status.nickname);
        loggedInGreeting.classList.add('mdc-top-app-bar__action-item');
        boxElement.appendChild(loggedInGreeting);
      }
      // Either a login or logout button
      const buttonElement = createButtonLink(
        status.loggedInFlag ? Globals.logoutIcon : Globals.loginIcon,
        status.actionURL,
      );
      buttonElement.children.item(1).classList.add('svg-icon');
      const buttonAnnotation = status.loggedInFlag ? 'Log out' : 'Log in';
      // Puts tooltip and screen-reader label on button icon element.
      buttonElement.setAttribute('aria-label', buttonAnnotation);
      buttonElement.setAttribute('title', buttonAnnotation);
      buttonElement.classList.add('mdc-top-app-bar__action-item');
      // Tag underneath button
      boxElement.appendChild(buttonElement);
    })
    .catch((error) => {
      boxElement.innerHTML = 'Login service unavailable';
      console.error(error);
    });
  return boxElement;
}

/**
 * Constructs floating GitHub and LinkedIn Links.
 * Returns these elements as a DOM element --
 * the calling page will initialize these elements.
 *
 * @return {Element}
 */
function createFloatingLinks() {
  const newElement = document.createElement('div');
  newElement.id = 'links';
  newElement.classList.add('port-float-button-links');

  // Add each link
  // Github Link
  const githubLink = document.createElement('a');
  githubLink.classList.add('mdc-fab');
  githubLink.setAttribute('href', Globals.githubURL);
  githubLink.setAttribute('aria-label', 'GitHub');
  githubLink.innerHTML = github.svg;
  // Center icon in button
  githubLink.firstElementChild.setAttribute('viewBox', '-12 -12 48 48');

  // LinkedIn Link
  const linkedinLink = document.createElement('a');
  linkedinLink.classList.add('mdc-fab');
  linkedinLink.setAttribute('href', Globals.linkedinURL);
  linkedinLink.setAttribute('aria-label', 'LinkedIn');
  linkedinLink.innerHTML = linkedin.svg;
  // Center icon in button
  linkedinLink.firstElementChild.setAttribute('viewBox', '-12 -12 48 48');

  newElement.appendChild(githubLink);
  newElement.appendChild(document.createElement('br'));
  newElement.appendChild(linkedinLink);
  return newElement;
}
