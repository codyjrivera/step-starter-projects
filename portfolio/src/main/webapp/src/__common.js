// Copyright 2019-2020 Google LLC
// Author -- Cody Rivera
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

/**
 * @fileoverview This module provides functions and data used throughout the 
 * frontend, including creation of elements common across all pages such as 
 * the top bar.
 *
 * @module
 */

import { Globals } from "./__globals";

export { createTopBar };

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
  let newTopBar = document.createElement("header");
  newTopBar.classList.add("mdc-top-app-bar", "mdc-top-app-bar--fixed");

  let newTopBarDiv = document.createElement("div");
  newTopBarDiv.classList.add("mdc-top-app-bar__row");

  // Generate top bar title and contents separately
  let topBarTitle = createTopBarTitle(Globals.pageTitle);
  let topBarContents = createTopBarContents(currentPage);
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
  let topBarTitle = document.createElement("section");
  topBarTitle.classList.add("mdc-top-app-bar__section", 
                            "mdc-top-app-bar__section--align-start");

  // TODO -- This should be in its own function
  let titleElement = document.createElement("a");
  titleElement.classList.add("mdc-button", "mdc-top-app-bar__action-item");
  titleElement.setAttribute("href", "index.html");
  let rippleElement = document.createElement("div");
  rippleElement.classList.add("mdc-button__ripple");
  let textElement = document.createElement("span");
  textElement.classList.add("mdc-button__label");
  textElement.innerHTML = title;
  titleElement.appendChild(rippleElement);
  titleElement.appendChild(textElement);

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
  let topBarTitle = document.createElement("section");
  topBarTitle.classList.add("mdc-top-app-bar__section", 
                            "mdc-top-app-bar__section--align-end");

  // Adds top bar elements
  Globals.pageNames.forEach(name => {
    let newElement = document.createElement("a");
    newElement.classList.add("mdc-button", "mdc-top-app-bar__action-item");
    // Extra styling to current page's icon
    if (name === currentPage) {
      newElement.classList.add("mdc-top-app-bar--active");
    }
    newElement.setAttribute("href", name + ".html");
    let rippleElement = document.createElement("div");
    rippleElement.classList.add("mdc-button__ripple");
    let textElement = document.createElement("span");
    textElement.classList.add("mdc-button__label");
    textElement.innerHTML = Globals.pageNameMap[name];
    newElement.appendChild(rippleElement);
    newElement.appendChild(textElement);
    topBarTitle.appendChild(newElement);
  });

  return topBarTitle;
}