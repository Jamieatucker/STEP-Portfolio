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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Feedback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet that returns some example content.
 */
@WebServlet("/comments")
public class Comments extends HttpServlet {
  private class Comment{
    public String name;
    public String email;
    public String comment;

    public Comment(String name, String email, String comment){
      this.name = name;
      this.email = email;
      this.comment = comment;
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response){
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Prepare the Query to store the entities you want to load
    Query query = new Query("Data");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Delete all the comments in the datastore admin page
    for(Entity entity : results.asIterable()){
      datastore.delete(entity.getKey());
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {    
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    
    // Prepare the Query to store the entities you want to load
    Query query = new Query("Data").addSort("Timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Get the input from the form
    int userChoice = getNumberOfComments(request);
    
    // Loop over the entities to store the feedback in a list
    List<Feedback> statements = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (userChoice == 0) {
        break;
      }
      String name = (String) entity.getProperty("Name");
      String email = (String) entity.getProperty("Email");
      String comment = (String) entity.getProperty("Comment");
      long timestamp = (long) entity.getProperty("Timestamp");
	    
      Feedback feedback = new Feedback(name, email, comment, timestamp);
      statements.add(feedback);
      userChoice--;
    }
      // Send the JSON as the response
      Gson gson = new Gson();
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(statements));
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    // Get the body of the HTTP Post
    String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    Gson gson = new Gson();
    Comment target = gson.fromJson(body, Comment.class);

    // Get the input from the form
    String name = target.name;
    String email = userService.getCurrentUser().getEmail();
    String comment = target.comment;
    long timestamp = System.currentTimeMillis();

    // Create an entity and set its properties
    Entity dataEntity = new Entity("Data");
    dataEntity.setProperty("Name", name);
    dataEntity.setProperty("Email", email);
    dataEntity.setProperty("Comment", comment);
    dataEntity.setProperty("Timestamp", timestamp);

    // Store the entities
    datastore.put(dataEntity);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(dataEntity));
  }
  
  /** 
   * Returns the number of comments the user wants, or -1 if the choice was invalid.
   */
  private int getNumberOfComments(HttpServletRequest request){
    // Get the input from the form.
    String userChoiceString = request.getParameter("numComments");
	
    // Convert the input to an int.
    int userChoice;
    try {
      userChoice = Integer.parseInt(userChoiceString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + userChoiceString);
      return Integer.MAX_VALUE;
    }

    // Check that the input is greater than or equal to 0.
    if (userChoice < 0) {
      System.err.println("User choice is out of range: " + userChoiceString);
      return Integer.MAX_VALUE;
    }

    return userChoice;
  }
}
