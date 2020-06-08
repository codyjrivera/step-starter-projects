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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  static final long serialVersionUID = 1L;

  /**
   * Constant test comments
   */
  static final String[] TEST_COMMENTS = {
    "This is a normal comment",
    "This is another normal comment",
    "This is yet a third comment"
  };
  
  private ArrayList<String> commentsList =
    new ArrayList<String>(Arrays.asList(TEST_COMMENTS));

  /**
   * Processes HTTP GET requests for the /data servlet
   * The requests are responded to by a list of test commments
   * sent back as a JSON array of strings.
   *
   * @param request Information about the GET Request
   * @param response Information about the servlet's response
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    // Convert ArrayList to JSON.
    Gson gson = new Gson();
    String commentsListJson = gson.toJson(commentsList);
    response.getWriter().println(commentsListJson);
  }

  /**
   * Processes HTTP POST requests for the /data servlet
   * The requests are responded to by appending the
   * 'comment-text' argument of the POST request
   * to the list of comments. The client is then redirected back to
   * the com.html page.
   *
   * @param request Information about the POST Request
   * @param response Information about the servlet's response
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentText = getParameter(request, "comment-text", "");
    commentsList.add(commentText);
    
    response.sendRedirect("/com.html");
  }


  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
