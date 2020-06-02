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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** 
 * Servlet that returns some example content. TODO: modify this file to handle comments data 
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private ArrayList<String> comments = new ArrayList<String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Convert the ArrayList to JSON
    String json = convertToJsonUsingGson(comments);
    
    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	// Get the input from the form
    String name = getParameter(request, "name", "");
	String email = getParameter(request, "email", "");
    String text = getParameter(request, "comment", "");

    // Add input to ArrayList 
    if(!text.equals("")){
      comments.add(text);
    }

    // Send the HTML as the response
    response.setContentType("text/html;");
    for(int i = 0; i < comments.size(); i++){
      response.getWriter().println(comments.get(i));
    }

    // Send user to new page once comment is submitted
    response.sendRedirect("/hiddentalent.html");
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
