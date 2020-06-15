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

package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;

/** Comment data class */
public class Comment {

  /* Comment poster */
  private String commentPoster;

  /* Comment text */
  private String commentText;

  /* Comment sentiment */
  private float sentimentScore;

  /**
   * Constructs a comment object with given text
   *
   * @param commentPoster the username of the poster
   * @param commentText the comment text
   * @param sentimentScore the sentiment of the comment
   */
  public Comment(String commentPoster, String commentText, float sentimentScore) {
    this.commentPoster = commentPoster;
    this.commentText = commentText;
    this.sentimentScore = sentimentScore;
  }

  /**
   * Constructs a comment object by unmarshalling an entity
   *
   * @param entity the entity to unmarshall
   */
  private Comment(Entity entity) {
    entityUnmarshall(entity);
  }  

  /** Getter and setter for commentPoster */
  public String getCommentPoster() {
    return commentPoster;
  }

  public void setCommentPoster(String commentPoster) {
    this.commentPoster = commentPoster;
  }

  /** Getter and setter for commentText */
  public String getCommentText() {
    return commentText;
  }

  public void setCommentText(String commentText) {
    this.commentText = commentText;
  }

  /** Getter and setter for sentimentScore */
  public float getSentimentScore() {
    return sentimentScore;
  }

  public void setSentimentScore(float sentimentScore) {
    this.sentimentScore = sentimentScore;
  }

  /**
   * Marshalls the current class instance into a Datastore entity.
   *
   * @param entity the entity to marshall data into.
   */
  public void entityMarshall(Entity entity) {
    entity.setProperty("poster", commentPoster);
    entity.setProperty("text", commentText);
    entity.setProperty("sentiment-score", sentimentScore);
  }

  /**
   * Unmarshalls the provided Datastore entity into this class instance, overwriting the instance's
   * current fields.
   *
   * @param entity the entity to unmarshall data from.
   */
  public void entityUnmarshall(Entity entity) {
    commentPoster = (String) entity.getProperty("poster");
    commentText = (String) entity.getProperty("text");
    Double score = (Double) entity.getProperty("sentiment-score");
    sentimentScore = score.floatValue();
  }

  /**
   * Generates a comment from an entity
   *
   * @param entity the entity to generate the comment from
   * @return the new comment with the entity's information.
   */
  public static Comment from(Entity entity) {
    // Elegant wrapper for private constructor.
    return new Comment(entity);
  }

  /**
   * Generates an entity from a comment
   *
   * @return A freshly-created Comment entity with the current
   * timestamp.
   */
  public Entity toEntity() {
    Entity entity = new Entity("Comment");
    // Fill timestamp
    entity.setProperty("timestamp", System.currentTimeMillis());
    // Fill rest of comment
    entityMarshall(entity);
    return entity;
  }
}
