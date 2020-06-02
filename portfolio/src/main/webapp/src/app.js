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
import { createTopBar, createFloatingLinks } from "./__common";

const pageName = "index";

/** Inserts the top bar */
const topBarElement = createTopBar(pageName);
document.getElementById("header").replaceWith(topBarElement);
const topBar = new MDCTopAppBar(document.getElementsByTagName("header")[0]);

/** Inserts the floating link buttons */
const floatingElement = createFloatingLinks();
document.getElementById("links").replaceWith(floatingElement);

/** Adds ripple effect to buttons */
const buttonRipple = new MDCRipple(document.querySelector(".mdc-button"));
const floatingRipple = new MDCRipple(document.querySelector(".mdc-fab"));
