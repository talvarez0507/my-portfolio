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

import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceConfig.Builder;  
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.ReadPolicy.Consistency; 

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;  
import java.util.ArrayList;  
import com.google.gson.Gson;

@WebServlet("/nickname")
public class NicknameServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    List<String> html = new ArrayList<>();
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      // Creates a form on the page that creates a nickname through a POST
      // request on this servlet     
      addFormHTML(html);
    } else {
      String loginUrl = userService.createLoginURL("/nickname");
      html.add("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
    out.println(convertToJson(html));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
      return;
    }
    String nickname = request.getParameter("nickname");
    String id = userService.getCurrentUser().getUserId();
    DatastoreService datastore = getDatastore();
    Entity entity = new Entity("UserInfo", id);
    entity.setProperty("id", id);
    entity.setProperty("nickname", nickname);
    datastore.put(entity);
    response.sendRedirect("/");
  }

  private String convertToJson(List<String> html) {
    Gson gson = new Gson();
    String json = gson.toJson(html);
    return json;
  }

  private void addFormHTML(List<String> html) {
    String s1 = "<h1>Set Nickname</h1>";
    String s2 = "<h2>It appears that you're logged in, but have no nickname.</h2>";
    String s3 = "<p>Create your nickname here:</p>";
    html.add(s1);
    html.add(s2);
    html.add(s3);

    String s4 = "<form method=\"POST\" action=\"/nickname\">";
    String s5 = "<input name=\"nickname\" value=\"\" />";
    String s6 = "<br/>";
    String s7 = "<button class=\"button\">Create</button>";
    String s8 = "</form>";

    // This cannot be added to html separately because when the html is later 
    // added to the page, in the JS, the form opening and closing must be in  
    // the same string 
    html.add(s4+s5+s6+s7+s8);
  }

  private DatastoreService getDatastore() {
    DatastoreServiceConfig datastoreConfig =
    DatastoreServiceConfig.Builder.withReadPolicy(
        new ReadPolicy(Consistency.STRONG)).deadline(5.0);
    return DatastoreServiceFactory.getDatastoreService(datastoreConfig);
  }
}
