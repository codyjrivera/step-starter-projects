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

/** Comment data class */
public class Comment {

  /* Comment text */
  private String commentText;

  /* Comment sentiment */
  private boolean sentimentScoreFlag;

  private float sentimentScore;

  /**
   * Constructs a comment object with given text
   *
   * @param commentText the comment text
   */
  public Comment(String commentText) {
    this.commentText = commentText;
    this.sentimentScoreFlag = false;
  }

  /** Getter and setter for commentText */
  public String getCommentText() {
    return commentText;
  }

  public void setCommentText(String commentText) {
    this.commentText = commentText;
  }

  /**
   * Retreives comment sentiment -- provided hasSentimentScore is true -- otherwise the result is
   * undefined.
   *
   * @return sentiment score provided hasSentimentScore is true.
   */
  public float getSentimentScore() {
    return sentimentScore;
  }

  /**
   * Sets comment sentiment score, setting hasSentimentScore as true if not already set.
   *
   * @param sentimentScore the score in the range [-1.0 to 1.0].
   */
  public void setSentimentScore(float sentimentScore) {
    this.sentimentScore = sentimentScore;
    this.sentimentScoreFlag = true;
  }

  /**
   * Test if comment has sentiment score
   *
   * @return whether the comment has a sentiment score.
   */
  public boolean hasSentimentScore() {
    return sentimentScoreFlag;
  }

  /** Deletes the comment's sentiment score */
  public void deleteSentimentScore() {
    sentimentScoreFlag = false;
  }
}
