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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  private static final Comparator<Event> sortAscending =
      Comparator.comparingLong(event -> event.getWhen().start());

  /* This method returns a Collection of TimeRange's which allow enough time
   * for all of the required participants and all/no optional participants of
   * the MeetingRequest to attend a meeting at that time, given the events
   * they already have scheduled.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> requiredAttendees = request.getAttendees();
    long durationMinutes = request.getDuration();
    Collection<String> allAttendees = new HashSet<>();
    allAttendees.addAll(requiredAttendees);
    allAttendees.addAll(request.getOptionalAttendees());
    List<TimeRange> timeRangesOfEvents = new ArrayList<TimeRange>();
    List<Event> eventsList = new ArrayList<Event>(events);

    // Sort events based on their TimeRange's start time.
    Collections.sort(eventsList, sortAscending);

    Collection<TimeRange> onlyRequiredAttendeesIncluded =
        allFeasibleTimesForMeetings(eventsList, requiredAttendees, durationMinutes);
    Collection<TimeRange> allAttendeesIncluded =
        allFeasibleTimesForMeetings(eventsList, allAttendees, durationMinutes);
    // If we can include optional attendees.
    if (!allAttendeesIncluded.isEmpty()) {
      return allAttendeesIncluded;
    } else if (!requiredAttendees.isEmpty()) {
      // If there was at least 1 required attendee (not always the case).
      return onlyRequiredAttendeesIncluded;
    } else {
      // If there were no required attendees and it was infeasible with all optional attendees.
      // (This would be a meeting for no one so it should be empty)
      return Arrays.asList();
    }
  }

  /**
   * This method returns a Collection of TimeRange's which allow enough time
   * for ALL participants to attend a meeting at that time, given the events
   * they already have scheduled.
   */
  private static Collection<TimeRange> allFeasibleTimesForMeetings(
      List<Event> events, Collection<String> attendees, long durationMinutes) {
    List<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    // This represents the earliest time that we can schedule a window for the
    // meeting. As events are processed, this changes to their end times.
    int earliestPossibleSoFar = TimeRange.START_OF_DAY;
    for (Event e : events) {
      TimeRange eventTimeRange = e.getWhen();
      // Make sure the event is relevant i.e. that someone attending the event is
      // actually requesting to be in the meeting as well.
      Set<String> intersection = new HashSet<String>(attendees);
      intersection.retainAll(e.getAttendees());
      if (!intersection.isEmpty()) {
        // Make sure the time window is enough between the start of event
        // and the earliest time for the window of the meeting.
        if (eventTimeRange.start() - earliestPossibleSoFar >= durationMinutes) {
          possibleTimes.add(
              TimeRange.fromStartEnd(
                  earliestPossibleSoFar, eventTimeRange.start(), /* inclusive= */ false));
        }
        earliestPossibleSoFar = Math.max(earliestPossibleSoFar, eventTimeRange.end());
      }
    }
    // The end of the day is potentially never included so we check.
    if (TimeRange.END_OF_DAY - earliestPossibleSoFar >= durationMinutes) {
      possibleTimes.add(
          TimeRange.fromStartEnd(earliestPossibleSoFar, TimeRange.END_OF_DAY, /* inclusive= */ true));
    }
    return possibleTimes;
  }
}
