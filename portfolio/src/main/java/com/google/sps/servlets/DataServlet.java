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
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;  
import java.util.ArrayList;  

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /** Query object given from Datastore */
    Query query = new Query("Comment").addSort("Timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    System.err.println(processNumber(request));
    int maxComments = processNumber(request);
    /**
     * Arraylist called comments that contains Comments, which are objects from
     * a class with some basic fields for relevant data
     */ 
    int i = 1;
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (i > maxComments) break;
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty("Text");
      long timestamp = (long) entity.getProperty("Timestamp");
      /** This variable comment becomes a Comment object based on the data */
      Comment comment = new Comment(id, text, timestamp);
      comments.add(comment);
      i+=1;
    }
    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(comments));
  }

  private String convertToJson(List list) {
    Gson gson = new Gson();
    String json = gson.toJson(list);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = processComment(request);
    long timestamp = System.currentTimeMillis();
    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("Text", comment);
    taskEntity.setProperty("Timestamp", timestamp);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);
    response.sendRedirect("/index.html");
  }

  private String processComment(HttpServletRequest request) {
    String CommentText = request.getParameter("CommentText");
    return CommentText;
  }

  private int processNumber(HttpServletRequest request) {

    String maxCommentsString = request.getParameter("maxComments");
    int maxComments;
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxCommentsString);
      return 0;
    }
    if (maxComments < 0 ) {
      System.err.println("Amount is less than 1: " + maxCommentsString);
      return 0;
    }
    return maxComments;

  }
}
