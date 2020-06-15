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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that displays all comments via GET and adds a comment via POST */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  static final long serialVersionUID = 1L;

  /**
   * Processes HTTP GET requests for the /data servlet The requests are responded to by a list of
   * commments from the Datastore sent back as a JSON array of strings. The argument 'max-comments'
   * can optionally restrict the number of comments returned to at most that number, provided the
   * argument is a valid positive integer.
   *
   * @param request Information about the GET Request
   * @param response Information about the servlet's response
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Extract maximum number of comments returned from arguments
    String maxCommentsString = getParameter(request, "max-comments", "");

    boolean hasMaxComments = false;
    int maxComments = 0;

    // If maxComments is a positive integer, read it in. Otherwise, the servlet will
    // return every comment
    try {
      if (Pattern.matches("(\\+)?[0-9]+", maxCommentsString)) {
        maxComments = Integer.parseInt(maxCommentsString);
        hasMaxComments = true;
      }
    } catch (NumberFormatException e) {
      // Catches regex matches which overflow the system Integer type.
      hasMaxComments = false;
    }

    // Gets all existing comments from database
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    // Map entities into Comment instances.
    List<Comment> commentsList = results.asList(FetchOptions.Builder.withDefaults()).stream().map(Comment::from).collect(Collectors.toList());

    // If max-comments was provided, extract a sublist.
    if (hasMaxComments) {
      commentsList = commentsList.subList(0, Integer.min(maxComments, commentsList.size()));
    }

    // Convert List of comments to JSON.
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(commentsList));
  }

  /**
   * Processes HTTP POST requests for the /data servlet The requests are responded to by appending
   * a comment to the datastore database. All necessary fields will be 
   * assembled here. If the user is not logged in, then the status field in
   * the response json will be set to 'no-login'
   *
   * @param request Information about the POST Request
   * @param response Information about the servlet's response
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      // Constructs comment and gets comment sentiment
      String commentText = getParameter(request, "comment-text", "");
      float commentSentiment = getSentiment(commentText);

      Comment comment = new Comment(commentText, commentSentiment);

      // Create entity
      Entity commentEntity = comment.toEntity();

      // Add to database
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);

      response.setContentType("application/json;");
      response.getWriter().println("{ \"status\": \"ok\" }");
    } else {
      // Return user not logged in
      response.setContentType("application/json;");
      response.getWriter().println("{ \"status\": \"no-login\" }");
    }
  }

  /**
   * Attains the sentiment of the comment string according to Google Cloud Natural Language
   * sentiment analysis. The sentiment is graded on a one-dimensional scale of positive or negative,
   * with -1.0 = negative and 1.0 = positive.
   *
   * @param comment The comment to analyze
   * @return A sentiment score in the interval [-1.0, 1.0]
   */
  private float getSentiment(String comment) throws IOException {
    Document doc =
        Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();
    return score;
  }

  /**
   * @return the request parameter, or the default value if the parameter was not specified by the
   *     client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
