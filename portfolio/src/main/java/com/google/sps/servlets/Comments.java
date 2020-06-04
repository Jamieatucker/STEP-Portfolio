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
import com.google.appengine.api.datastore.Query.SortDirection;
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

  private ArrayList<String> comments = new ArrayList<String>();
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the body of the HTTP Post
    String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    System.out.println(body);

    Gson gson = new Gson();
    Comment target = gson.fromJson(body, Comment.class);

    // Get the input from the form
    String name = target.name;
    String email = target.email;
    String comment = target.comment;
    long timestamp = System.currentTimeMillis();

    //String name = getParameter(request, "name", "");
    //String email = getParameter(request, "email", "");
    //String comment = getParameter(request, "comment", "");
    //long timestamp = System.currentTimeMillis();

    // Create an entity and set its properties
    Entity dataEntity = new Entity("Data");
    dataEntity.setProperty("Name", name);
    dataEntity.setProperty("Email", email);
    dataEntity.setProperty("Comment", comment);
    dataEntity.setProperty("Timestamp", timestamp);

    // Create a space to store the entities
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(dataEntity);


    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(dataEntity));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Prepare the Query to store the entity you want to load
    Query query = new Query("Data").addSort("Timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	PreparedQuery results = datastore.prepare(query);

    // Get the input from the form
    int userChoice = getNumberOfComments(request);
    if (userChoice == -1) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter an integer larger than -1.");
      return;
    }
	else{
      // Loop over the entities to store the feedback in a list
      List<Feedback> statements = new ArrayList<Feedback>();
      for (Entity entity : results.asIterable()) {
        if(userChoice == 0){
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
      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(statements));
    }
  }

  private String convertToJson(String name, String email, String comment, long timestamp){
    String json = "{";
    json += "\"Name\": ";
    json += "\"" + name + "\"";
    json += ", ";
    json += "\"Email\": ";
    json += "\"" + email + "\"";
    json += ", ";
    json += "\"Comment\": ";
    json += comment;
    json += "\"Timestamp\": ";
    json += timestamp;
    json += "}";
    return json;
  }
  
  /**
   * Convert Data instance into a JSON string using GSON. Note: We had to add a GSON library dependency to the pom.xml file.
   */
  private String convertToJsonUsingGson(ArrayList comments){
	Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
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

    // Check that the input is greater than 0.
    if (userChoice < 0) {
      System.err.println("User choice is out of range: " + userChoiceString);
      return Integer.MAX_VALUE;
    }

    return userChoice;
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the user/client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue){
	String value = request.getParameter(name);
    if(value == null){
      return defaultValue;
    }
    return value;
  }

}
