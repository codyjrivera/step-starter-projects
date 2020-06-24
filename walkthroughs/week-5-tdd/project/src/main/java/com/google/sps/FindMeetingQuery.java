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
import java.util.Arrays;
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
 * <p>This algorithm generates time intervals in which ALL mandatory attendees are free and, if such
 * a valid interval exists, intervals in which all optional attendees are free as well.
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
    Set<String> optionalAttendees = new HashSet<String>(request.getOptionalAttendees());

    // Expected O(E log E) time
    SortedMap<Integer, TimeRange> rawConflictSet = new TreeMap<Integer, TimeRange>();
    SortedMap<Integer, TimeRange> rawOptionalConflictSet = new TreeMap<Integer, TimeRange>();
    generateRawConflictSet(
        rawConflictSet, rawOptionalConflictSet, events, requestedAttendees, optionalAttendees);

    List<TimeRange> conflictSet = cleanConflictSet(rawConflictSet.values());
    List<TimeRange> optionalConflictSet = cleanConflictSet(rawOptionalConflictSet.values());
    List<TimeRange> combinedConflictSet = combineConflictSets(conflictSet, optionalConflictSet);

    Collection<TimeRange> combinedValidTimes =
        getValidTimes(combinedConflictSet, request.getDuration());
    if (!combinedValidTimes.isEmpty()) {
      return combinedValidTimes;
    }
    // Edge case -- if all optional attendees cannot meet
    // and there are no mandatory attendees, we return no times, according to
    // the test optionalOnlyNoGaps
    if (request.getAttendees().isEmpty()) {
      return Arrays.asList();
    }
    return getValidTimes(conflictSet, request.getDuration());
  }

  /**
   * Algorithm design approach:
   *
   * <p>- To build the set of valid meeting times, I find the set of invalid meeting times and take
   * the complement of that set.
   *
   * <p>- The following is a sketch of how I build the set of invalid meeting times:
   *
   * <pre>
   * S = the empty set
   * for each m in the set of all meetings,
   *   if INTERSECT(request's attendees, m's attendees) is non-empty, (1)
   *     S := UNION(S, m's time range) (2)
   * </pre>
   *
   * <p>- I choose an appropriate implementation for each of the abstract operations above. (1) is
   * implemented as a hashtable lookup, where each of m's attendees is looked up in the prepared
   * hashtable of request's attendees, in O(m's attendees) time. (2) is implemented in two phases,
   * the first being the insertion of all conflicting time ranges into a set ordered by start time,
   * with O(log n) insertion time. The first phase returns a set which may have several overlapping
   * entries. Thus, the second phase, done outside of the loop, converts the raw set in O(n) time
   * into a set without overlapping entries.
   *
   * <p>To incorporate optional attendees, I duplicate operations (1) and (2) for optional
   * attendees, storing the results in another set and cleaning that set separately. I then create
   * another cleaned set that is the combination of the mandatory and optional attendees' conflict
   * sets. If there is a set of times that works for all attendees, then those times are returned,
   * otherwise, only mandatory attendees are considered. Asymptotic runtime does not change, as the
   * extra work can be absorbed by a constant factor.
   */

  /**
   * Generates a set of time ranges that conflict with any of the requested attendees' schedules.
   * Note that ranges may overlap. Expected runtime is O(E log E). Sets are represented as a
   * SortedMap with start time as the key and TimeRange as the value.
   *
   * @param rawConflictSet Overlapping set of all
   * @param events the collection of all events
   * @param requestedAttendees the set of all requested attendees.
   */
  private void generateRawConflictSet(
      SortedMap<Integer, TimeRange> rawConflictSet,
      SortedMap<Integer, TimeRange> rawOptionalConflictSet,
      Collection<Event> events,
      Set<String> requestedAttendees,
      Set<String> optionalAttendees) {
    for (Event event : events) {
      if (attendeesIntersect(event.getAttendees(), requestedAttendees)) {
        insertTimeRange(rawConflictSet, event.getWhen());
      }
      if (attendeesIntersect(event.getAttendees(), optionalAttendees)) {
        insertTimeRange(rawOptionalConflictSet, event.getWhen());
      }
    }
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
   * Inserts a time range into a SortedMap containing an ordered overlapping set of times.
   *
   * @param rawSet The set of times
   * @param timeRange The range to insert
   */
  private void insertTimeRange(SortedMap<Integer, TimeRange> rawSet, TimeRange timeRange) {
    if (rawSet.containsKey(timeRange.start())) {
      // If an event with the same start time is in the set,
      // keep the longest event
      TimeRange setTime = rawSet.get(timeRange.start());
      if (timeRange.duration() > setTime.duration()) {
        rawSet.put(timeRange.start(), timeRange);
      }
    } else {
      rawSet.put(timeRange.start(), timeRange);
    }
  }

  /**
   * Converts a conflict set which may contain overlapping intervals into one that contains none.
   * Maximum runtime O(E).
   *
   * @param rawConflictSet
   * @return an ordered conflict set with no overlapping invervals.
   */
  private List<TimeRange> cleanConflictSet(Collection<TimeRange> rawConflictSet) {
    List<TimeRange> conflictSet = new ArrayList<TimeRange>();
    FlatRangeSetBuilder builder = new FlatRangeSetBuilder();

    for (TimeRange rawRange : rawConflictSet) {
      builder.add(rawRange);
    }

    return builder.getFlatRangeSet();
  }

  /**
   * Combines two existing sorted and cleaned conflict sets. This is basically merging with extra
   * considerations for end times. Runtime O(E).
   *
   * @param setA the first conflict set
   * @param setB the second conflict set
   * @return the union of the two conflict sets
   */
  public List<TimeRange> combineConflictSets(List<TimeRange> setA, List<TimeRange> setB) {
    FlatRangeSetBuilder builder = new FlatRangeSetBuilder();
    int indexA = 0, indexB = 0;

    // Merge and deal with any overlapping intervals.
    while (indexA < setA.size() && indexB < setB.size()) {
      TimeRange rawRange = null;
      if (setA.get(indexA).start() <= setB.get(indexB).start()) {
        rawRange = setA.get(indexA);
        ++indexA;
      } else {
        rawRange = setB.get(indexB);
        ++indexB;
      }

      builder.add(rawRange);
    }

    while (indexA < setA.size()) {
      builder.add(setA.get(indexA));
      ++indexA;
    }

    while (indexB < setB.size()) {
      builder.add(setB.get(indexB));
      ++indexB;
    }

    return builder.getFlatRangeSet();
  }

  /** Builds a flattened range set, where none of the elements overlap. */
  private class FlatRangeSetBuilder {
    private List<TimeRange> rangeSet;
    // Temporary range
    private int startTime;
    private int endTime;
    // Whether there is a temporary range that is currently being built
    private boolean flag;

    /** Makes a new FlatRangeSetBuilder */
    FlatRangeSetBuilder() {
      rangeSet = new ArrayList<TimeRange>();
      startTime = 0;
      endTime = 0;
      flag = false;
    }

    /**
     * Adds another potentially overlapping range to the range set.
     *
     * @param rawRange a time range, potentially an overlapping one.
     */
    private void add(TimeRange rawRange) {
      if (!flag) {
        // Start temporary time range with RawRange
        flag = true;
        startTime = rawRange.start();
        endTime = rawRange.end();
      } else {
        if (rawRange.start() < endTime) {
          // Temporary time range := UNION(temporary time range, RawRange)
          // Because both inputs are sorted, we do not need to consider
          // the case where rawRange.start() < startTime.
          endTime = Integer.max(rawRange.end(), endTime);
        } else {
          // Add temporary time range
          rangeSet.add(TimeRange.fromStartEnd(startTime, endTime, false));
          // Start another temporary time range
          startTime = rawRange.start();
          endTime = rawRange.end();
        }
      }
    }

    /**
     * Returns the non-overlapping range set as a list of time ranges. Behavior after calling this
     * method is undefined.
     *
     * @return non-overlapping range set
     */
    private List<TimeRange> getFlatRangeSet() {
      if (flag) {
        rangeSet.add(TimeRange.fromStartEnd(startTime, endTime, false));
      }
      return rangeSet;
    }
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
