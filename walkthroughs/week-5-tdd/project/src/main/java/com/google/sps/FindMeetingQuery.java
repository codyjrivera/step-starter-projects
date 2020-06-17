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

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class provides an implementation of an algorithm designed to determine time intervals
 * attendees can meet on. The input is the meeting request as well as a collection of all meetings
 * scheduled on a day. The algorithm returns a collection of time intervals at least as long as the
 * requested meeting time.
 *
 * <p>Right now, this algorithm only generates time intervals in which ALL mandatory attendees are
 * free.
 *
 * <p>This algorithm's *expected* runtime is O(M + E log E), where M = the number of mandatory
 * attendees and E = the number of events and each of their attendees.
 */
public final class FindMeetingQuery {

  /**
   * Finds all time intervals, if they exist, where all a meeting request's mandatory attendees are
   * not scheduled to attend any other meeting. The set of valid times is returned as an ordered
   * collection of TimeRanges.
   *
   * @param events the collection of all scheduled events
   * @param request the meeting request
   * @return an ordered collection of TimeRanges where all attendees can attend a meeting, or an
   *     empty collection if no valid times exist.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Expected O(M) time
    Set<String> requestedAttendees = new HashSet<String>(request.getAttendees());

    // Expected O(E log E) time
    SortedMap<Integer, TimeRange> rawConflictSet =
        generateRawConflictSet(events, requestedAttendees);
    Collection<TimeRange> conflictSet = cleanConflictSet(rawConflictSet.values());
    return getValidTimes(conflictSet, request.getDuration());
  }

  /**
   * Algorithm design approach:
   *
   * <p>- To build the set of valid meeting times, I find the set of invalid meeting times and take
   * the complement of that set.
   *
   * <p>- The following is a sketch of how I build the set of invalid meeting times: S = the empty
   * set for each m in the set of all meetings, if INTERSECT(request's attendees, m's attendees) is
   * non-empty, (1) S := UNION(S, m's time range) (2)
   *
   * <p>- I choose an appropriate implementation for each of the abstract operations above. (1) is
   * implemented as a hashtable lookup, where each of m's attendees is looked up in the prepared
   * hashtable of request's attendees, in O(m's attendees) time. (2) is implemented in two phases,
   * the first being the insertion of all conflicting time ranges into a set ordered by start time,
   * with O(log n) insertion time. The first phase returns a set which may have several overlapping
   * entries. Thus, the second phase, done outside of the loop, converts the raw set in O(n) time
   * into a set without overlapping entries.
   */

  /**
   * Generates a set of time ranges that conflict with any of the requested attendees' schedules.
   * Note that ranges may overlap. Expected runtime is O(E log E).
   *
   * @param events the collection of all events
   * @param requestedAttendees the set of all requested attendees.
   * @return a map of potentially overlapping time ranges, with the start time as the key
   */
  private SortedMap<Integer, TimeRange> generateRawConflictSet(
      Collection<Event> events, Set<String> requestedAttendees) {
    SortedMap<Integer, TimeRange> rawConflictSet = new TreeMap<Integer, TimeRange>();
    for (Event event : events) {
      if (attendeesIntersect(event.getAttendees(), requestedAttendees)) {
        TimeRange eventTime = event.getWhen();
        if (rawConflictSet.containsKey(eventTime.start())) {
          // If an event with the same start time is in the set,
          // keep the longest event
          TimeRange setTime = rawConflictSet.get(eventTime.start());
          if (eventTime.duration() > setTime.duration()) {
            rawConflictSet.put(eventTime.start(), eventTime);
          }
        } else {
          rawConflictSet.put(eventTime.start(), eventTime);
        }
      }
    }
    return rawConflictSet;
  }

  /**
   * Determines if a set of meeting attendees intersects with our hashtable of requested attendees.
   * Expected runtime is O(n), where n is the size of the set of attendees (not the hashtable)
   *
   * @param meetingAttendees a set of meeting attendees
   * @param requestedAttendees a hashtable of requested attendees.
   * @return Whether the sets intersect
   */
  private boolean attendeesIntersect(
      Collection<String> meetingAttendees, Set<String> requestedAttendees) {
    for (String attendee : meetingAttendees) {
      if (requestedAttendees.contains(attendee)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Converts a conflict set which may contain overlapping intervals into one that contains none.
   * Maximum runtime O(E).
   *
   * @param rawConflictSet
   * @return an ordered conflict set with no overlapping invervals.
   */
  private Collection<TimeRange> cleanConflictSet(Collection<TimeRange> rawConflictSet) {
    List<TimeRange> conflictSet = new ArrayList<TimeRange>();
    // Temporary TimeRange construction information
    boolean tempRangeFlag = false;
    int startTime = 0, endTime = 0;

    for (TimeRange rawRange : rawConflictSet) {
      if (!tempRangeFlag) {
        // Start temporary time range with RawRange
        tempRangeFlag = true;
        startTime = rawRange.start();
        endTime = rawRange.end();
      } else {
        if (rawRange.start() < endTime) {
          // Temporary time range := UNION(temporary time range, RawRange)
          // Because rawConflictSet is sorted, we do not need to consider
          // the case where rawRange.start() < startTime.
          endTime = Integer.max(rawRange.end(), endTime);
        } else {
          // Add temporary time range
          conflictSet.add(TimeRange.fromStartEnd(startTime, endTime, false));
          // Start another temporary time range
          startTime = rawRange.start();
          endTime = rawRange.end();
        }
      }
    }

    // Add last time range, if such a range exists.
    if (tempRangeFlag) {
      conflictSet.add(TimeRange.fromStartEnd(startTime, endTime, false));
    }
    return conflictSet;
  }

  /**
   * Converts a set of conflicting time intervals into a set of good time intervals that are at
   * least minDuration long. Maximum runtime O(E).
   *
   * @param conflictSet an ordered collection of all conflicting TimeRanges.
   * @param minDuration the minimum duration of a TimeRange
   * @return the complement of conflictSet where all TimeRanges are at least minDuration long.
   */
  private Collection<TimeRange> getValidTimes(Collection<TimeRange> conflictSet, long minDuration) {
    List<TimeRange> validTimes = new ArrayList<TimeRange>();
    int startTime = TimeRange.START_OF_DAY;

    for (TimeRange conflictRange : conflictSet) {
      if ((long) (conflictRange.start() - startTime) >= minDuration) {
        validTimes.add(TimeRange.fromStartEnd(startTime, conflictRange.start(), false));
      }
      // Skip over entire conflict region.
      startTime = conflictRange.end();
    }

    // Final element
    if (TimeRange.END_OF_DAY - startTime >= minDuration) {
      validTimes.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
    }
    return validTimes;
  }
}
