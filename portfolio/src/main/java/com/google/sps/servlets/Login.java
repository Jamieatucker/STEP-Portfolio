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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class Login extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    UserService userService = UserServiceFactory.getUserService();

    String redirect = request.getHeader("Referer");
    if (redirect == null) {
      redirect = "/index.html";
    }
    String userEmail = "Stranger";
    String url = "";
    String status = "login";

    if (userService.isUserLoggedIn()) {
      userEmail = userService.getCurrentUser().getEmail();
      url = userService.createLogoutURL(redirect);
      status = "logout";
    } else {
      url = userService.createLoginURL(redirect);
    }
    // Generate the login page
    createLoginPage(response, userEmail, redirect, status, url);
  }

  private static void createLoginPage(HttpServletResponse response, String userEmail, String redirect, String status, String url) throws IOException {
    
    response.getWriter().println("<link rel=\"stylesheet\" href=\"style.css\">");
    response.getWriter().println("<!-- Favicons -->");
    response.getWriter().println("<link rel=\"apple-touch-icon\" href=\"favicons/apple-touch-icon.png\" sizes=\"180x180\">");
    response.getWriter().println("<link rel=\"icon\" href=\"favicons/favicon-32x32.png\" sizes=\"32x32\">");
    response.getWriter().println("<link rel=\"icon\" href=\"favicons/favicon-16x16.png\" sizes=\"16x16\">");
    response.getWriter().println("<link rel=\"icon\" href=\"favicons/android-chrome-512x512.png\" sizes=\"512x512\">");
    response.getWriter().println("<link rel=\"icon\" href=\"favicons/android-chrome-192x192.png\" sizes=\"192x192\">");
    response.getWriter().println("<link rel=\"icon\" href=\"favicons/favicon.ico\">");
    response.getWriter().println("<div id=\"content\">");
    response.getWriter().println("<p>Hello " + userEmail + "!</p>");
    response.getWriter().println("<p>Click <a href=\"" + redirect + "\">here</a> to view the site or " + 
          "<a href=\"" + url + "\">here</a> to " + status + ".</p>");
    response.getWriter().println("</div>");
  }
}

