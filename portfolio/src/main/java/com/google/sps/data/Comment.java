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
  private String nickname;

  /* Comment text */
  private String text;

  /* Comment sentiment */
  private float sentimentScore;

  /** Private empty constructor */
  private Comment() {}

  /**
   * Constructs a comment object with given text
   *
   * @param nickname the username of the poster
   * @param text the comment text
   * @param sentimentScore the sentiment of the comment
   */
  public Comment(String nickname, String text, float sentimentScore) {
    this.nickname = nickname;
    this.text = text;
    this.sentimentScore = sentimentScore;
  }

  /** Getter and setter for nickname */
  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  /** Getter and setter for text */
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  /** Getter and setter for sentimentScore */
  public float getSentimentScore() {
    return sentimentScore;
  }

  public void setSentimentScore(float sentimentScore) {
    this.sentimentScore = sentimentScore;
  }

  /**
   * Generates a comment from an entity
   *
   * @param entity the entity to generate the comment from
   * @return the new comment with the entity's information.
   */
  public static Comment from(Entity entity) {
    Comment comment = new Comment();
    comment.nickname = (String) entity.getProperty("nickname");
    comment.text = (String) entity.getProperty("text");
    Double score = (Double) entity.getProperty("sentiment-score");
    comment.sentimentScore = score.floatValue();
    return comment;
  }

  /**
   * Generates an entity from a comment
   *
   * @return A freshly-created Comment entity with the current timestamp.
   */
  public Entity toEntity() {
    Entity entity = new Entity("Comment");
    // Fill timestamp
    entity.setProperty("timestamp", System.currentTimeMillis());
    // Fill rest of comment
    entity.setProperty("nickname", nickname);
    entity.setProperty("text", text);
    entity.setProperty("sentiment-score", sentimentScore);
    return entity;
  }
}
