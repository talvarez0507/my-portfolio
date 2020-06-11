package com.google.sps.data;

/** A class for comments. */
public final class Comment {

  private final long id;
  private final String text;
  private final long timestamp;
  
  /**
   * This constructor uses the Datastore ID belonging
   * to the comment as the id field, the comment's 
   * actual text in its text field, and the comment's 
   * creation time in milliseconds in its timestamp field.
   */ 
  public Comment(long id, String text, long timestamp) {
    this.id = id;
    this.text = text;
    this.timestamp = timestamp;
  }
}
