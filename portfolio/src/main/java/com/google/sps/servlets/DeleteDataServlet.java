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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes everything from CloudStore by POST */
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  static final long serialVersionUID = 2L;

  /**
   * Processes HTTP POST requests for the /delete-data servlet This servlet deletes all records on
   * the Comments table in CloudStore, if a user is logged in. Otherwise, this servlet does nothing,
   * and returns 'no-login' in the status field of the response.
   *
   * @param request Information about the POST Request
   * @param response Information about the servlet's response
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      // Get all Comments from datastore, so we can delete by key.
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      Query query = new Query("Comment");
      PreparedQuery results = datastore.prepare(query);
      for (Entity entity : results.asIterable()) {
        datastore.delete(entity.getKey());
      }

      response.setContentType("application/json;");
      response.getWriter().println("{ \"status\": \"ok\" }");
    } else {
      // User is not logged in
      response.setContentType("application/json;");
      response.getWriter().println("{ \"status\": \"no-login\" }");
    }
  }
}
