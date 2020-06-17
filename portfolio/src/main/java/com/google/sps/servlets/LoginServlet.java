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

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that returns whether a user is logged in or logged out, as well as an appropriate login
 * or logout link, on a GET request.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  static final long serialVersionUID = 3L;

  /** Structure that results are returned in JSON form in. */
  private class LoginData {
    private boolean loggedInFlag;
    private String actionURL;
    private String nickname;
  }

  /**
   * Processes HTTP GET requests for the /login servlet. This servlet returns a flag that indicates
   * whether the user has logged out, as well as a URL to the appropriate login or logout page.
   *
   * @param request Information about the GET Request
   * @param response Information about the servlet's response
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LoginData data = new LoginData();

    // Obtains login data and link
    UserService userService = UserServiceFactory.getUserService();
    String thisUrl = request.getHeader("referer");

    data.loggedInFlag = userService.isUserLoggedIn();
    if (data.loggedInFlag) {
      data.actionURL = userService.createLogoutURL(thisUrl);
      data.nickname = userService.getCurrentUser().getNickname();
    } else {
      data.actionURL = userService.createLoginURL(thisUrl);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(data));
  }
}
