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

  /* Comment text */
  private String commentText;

  /* Comment sentiment */
  private float sentimentScore;

  /**
   * Constructs a comment object with given text
   *
   * @param commentText the comment text
   */
  public Comment(String commentText, float sentimentScore) {
    this.commentText = commentText;
    this.sentimentScore = sentimentScore;
  }

  /**
   * Constructs a comment object by unmarshalling an entity
   *
   * @param entity the entity to unmarshall
   */
  public Comment(Entity entity) {
    entityUnMarshall(entity);
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
    entity.setProperty("text", commentText);
    entity.setProperty("sentiment-score", sentimentScore);
  }

  /**
   * Unmarshalls the provided Datastore entity into this class instance, overwriting the instance's
   * current fields.
   *
   * @param entity the entity to unmarshall data from.
   */
  public void entityUnMarshall(Entity entity) {
    commentText = (String) entity.getProperty("text");
    Double score = (Double) entity.getProperty("sentiment-score");
    sentimentScore = score.floatValue();
  }
}
