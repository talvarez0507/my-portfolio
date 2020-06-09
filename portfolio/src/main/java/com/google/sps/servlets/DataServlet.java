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
  
  private static final String COMMENT = "Comment";
  private static final String TIME = "Timestamp";
  private static final String TEXT = "Text";
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Query object given from Datastore
    Query query = new Query(COMMENT).addSort(TIME, SortDirection.DESCENDING);
    DatastoreService datastore = getDatastore();
    PreparedQuery results = datastore.prepare(query);
    System.err.println(processNumber(request));
    int maxComments = processNumber(request);
    // Arraylist called comments that contains Comments, which are objects from
    // a class with some basic fields for relevant data
    List<Comment> comments = new ArrayList<>();
    int i = 1;
    for (Entity entity : results.asIterable()) {
      if (i > maxComments) break;
      comments.add(entityToComment(entity));
      i+=1;
    }
    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(comments));
  }

  private Comment entityToComment(Entity entity) {
    long id = entity.getKey().getId();
    String text = (String) entity.getProperty(TEXT);
    long timestamp = (long) entity.getProperty(TIME);
    // This variable comment becomes a Comment object based on the data 
    Comment comment = new Comment(id, text, timestamp);
    return comment;
  }

  private String convertToJson(List<Comment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = processComment(request);
    long timestamp = System.currentTimeMillis();
    Entity commentEntity = new Entity(COMMENT);
    commentEntity.setProperty(TEXT, comment);
    commentEntity.setProperty(TIME, timestamp);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
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

  private DatastoreService getDatastore() {
    DatastoreServiceConfig datastoreConfig =
    DatastoreServiceConfig.Builder.withReadPolicy(
        new ReadPolicy(Consistency.STRONG)).deadline(5.0);
    return DatastoreServiceFactory.getDatastoreService(datastoreConfig);
  }
}
