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
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Set;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> requiredAttendees = request.getAttendees();
    long duration = request.getDuration();
    Collection<String> allAttendees = new HashSet<>();
    allAttendees.addAll(requiredAttendees);
    allAttendees.addAll(request.getOptionalAttendees());
    List<TimeRange> timeRangesOfEvents = new ArrayList<TimeRange>();
    Collection<String> peopleInEvents = new HashSet<>();
    List<Event> eventsAsList = new ArrayList<Event>();

    // Make a List (instead of Collection) out of events so we can sort it.
    eventsAsList.addAll(events);
    // Sort events based on their TimeRange's start time. 
    Collections.sort(eventsAsList, new Comparator<Event>() {
	  @Override
	  public int compare(Event e1, Event e2) {
		return e1.getWhen().start() - e2.getWhen().start();
	  }
    });
    Collection<TimeRange> onlyRequiredAttendeesIncluded = solveQuery(eventsAsList, requiredAttendees, duration);
    Collection<TimeRange> allAttendeesIncluded = solveQuery(eventsAsList, allAttendees, duration);
    // If we can include optional attendees.
    if (allAttendeesIncluded.size() > 0) {
      return allAttendeesIncluded;
    // If there was at least 1 required attendee (not always the case).
    } else if (requiredAttendees.size() > 0) {
      return onlyRequiredAttendeesIncluded;
    // If there were no required attendees and it was infeasible with all optional attendees.
    // (This would be a meeting for no one so it should be empty)
    } else {
      return Arrays.asList();
    }
  }

  public Collection<TimeRange> solveQuery(List<Event> eventsAsList, Collection<String> attendees, long duration) {
    List<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    // This represents the earliest time that we can schedule a window for the
    // meeting. As events are processed, this changes to their end times.
    int earliestPossibleSoFar = TimeRange.START_OF_DAY;
    for (Event e: eventsAsList) {
      TimeRange eventTimeRange = e.getWhen();
      // Make sure the event is relevant i.e. that someone attending the event is
      // actually requesting to be in the meeting as well 
      HashSet<String> intersection = new HashSet<String>(attendees);
      intersection.retainAll(e.getAttendees());
      if (intersection.size() > 0) {
        // Make sure the time window is enough between the start of event
        // and the earliest time for the window of the meeting.
        if (eventTimeRange.start() - earliestPossibleSoFar >= duration) {
          possibleTimes.add(TimeRange.fromStartEnd(earliestPossibleSoFar, eventTimeRange.start(), false));
        }
        earliestPossibleSoFar = Math.max(earliestPossibleSoFar, eventTimeRange.end());
      }
    }
    // The end of the day is potentially never included so we check to make sure
    if (TimeRange.END_OF_DAY - earliestPossibleSoFar >= duration) {
        possibleTimes.add(TimeRange.fromStartEnd(earliestPossibleSoFar, TimeRange.END_OF_DAY, true));
    }
    return possibleTimes;
  }
}
