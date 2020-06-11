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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/sports")
public class SportsData extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    // Prepare the Query to store the entities you want to load
    Query query = new Query("SportsData").addSort("Timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    Map<String, Integer> sportVotes = new HashMap<>();
    for(Entity entity : results.asIterable()){
      String sport = (String) entity.getProperty("Sport");
      int newVotes = 0;
      if (sportVotes.containsKey(sport)) {
        newVotes = sportVotes.get(sport);
        newVotes++;
        sportVotes.put(sport, newVotes);
      }
      else {
        newVotes = 1;
        sportVotes.put(sport, newVotes);
      }
    } 
    
    Gson gson = new Gson();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(sportVotes));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the vote details
    String sport = request.getParameter("sport");
    int currentVotes = 1;
    long timestamp = System.currentTimeMillis();

    // Create an entity and set its properties
    Entity dataEntity = new Entity("SportsData");
    dataEntity.setProperty("Sport", sport);
    dataEntity.setProperty("Votes", currentVotes);
    dataEntity.setProperty("Timestamp", timestamp);

    // Store the entities
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(dataEntity);
    
    // Send the JSON as the response
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(dataEntity));

    // Redirect to the page the user was just on
    String redirect = request.getHeader("Referer");
    response.sendRedirect(redirect);
  }
}
