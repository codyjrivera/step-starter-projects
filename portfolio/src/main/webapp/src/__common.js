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

  return topBarTitle;
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
