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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet that returns some example content.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private ArrayList<String> comments = new ArrayList<String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Prepare the Query to store the entity you want to load
    Query query = new Query("Data").addSort("Timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	PreparedQuery results = datastore.prepare(query);

	// Loop over the entities to store the feedback in a list
    List<Feedback> statements = new ArrayList<>();
	for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("Name");
      String email = (String) entity.getProperty("Email");
      String comment = (String) entity.getProperty("Comment");
      long timestamp = (long) entity.getProperty("Timestamp");
	  
      Feedback feedback = new Feedback(name, email, comment, timestamp);
      statements.add(feedback);
    }

    // Send the JSON as the response
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(statements));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form
    String name = getParameter(request, "name", "");
    String email = getParameter(request, "email", "");
    String comment = getParameter(request, "comment", "");
    long timestamp = System.currentTimeMillis();
    
    // Create an entity and set its properties
    Entity dataEntity = new Entity("Data");
    dataEntity.setProperty("Name", name);
    dataEntity.setProperty("Email", email);
    dataEntity.setProperty("Comment", comment);
    dataEntity.setProperty("Timestamp", timestamp);

    // Create a space to store the entities
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(dataEntity);

    // Convert parameters to JSON (May work on this feature later)
	String json = convertToJson(name,email,comment,timestamp);

    // Add input to ArrayList 
    if(!comment.equals("")){
      comments.add(comment + "\n"); 
    }

    // Send the HTML as the response
    response.setContentType("text/html;");
    for(int i = 0; i < comments.size(); i++){
      response.getWriter().println(comments.get(i));
    }

    // Send user to new page once comment is submitted
    response.sendRedirect("/index.html");
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
