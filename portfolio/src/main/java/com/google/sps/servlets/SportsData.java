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

  private Map<String, Integer> sportVotes = new HashMap<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(sportVotes);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    String sport = request.getParameter("sport");
    int currentVotes = sportVotes.containsKey(sport) ? sportVotes.get(sport) : 0;
    sportVotes.put(sport, currentVotes + 1);

    // Create an entity and set its properties
    Entity dataEntity = new Entity("SportsData");
    dataEntity.setProperty("Sport", sport);
    dataEntity.setProperty("Votes", currentVotes);

    // Store the entities
    datastore.put(dataEntity);
    
    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(dataEntity));
    
    String redirect = request.getHeader("Referer");
    response.sendRedirect(redirect);
  }
}
