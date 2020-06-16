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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Find events that match the requested number of attendees and duration
    // and adds it to an ArrayList
    Set<TimeRange> meetings = new HashSet<>();
    Collection<String> requiredAttendees = request.getAttendees();
    int requiredNumAttendees = requiredAttendees.size();
    long requiredDuration = request.getDuration();
    
    // Will be used in the for-each loop
    int i = 0;
    int k = 1;

    for (Event event : events) {
      // Convert the collection to an event array
      Event[] evArr = events.toArray(new Event[events.size()]);

      Set<String> currAttendees = event.getAttendees();
      int numAttendees = currAttendees.size();
      TimeRange currTime = event.getWhen();
      int evStart = currTime.start();
      int evEnd = currTime.end();
      int evDuration = currTime.duration();

      // If any of the events overlap with one another
      if (events.size() > 1 && evArr[k - 1].getWhen().overlaps(evArr[k].getWhen()) == true) {
        TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evArr[k-1].getWhen().start(), false);
        meetings.add(available);
        available = currTime.fromStartEnd(evArr[k].getWhen().end(), TimeRange.END_OF_DAY, true);
        meetings.add(available);
      }

      // If there is only one event scheduled
      if ((events.size() == 1) && (evStart != TimeRange.START_OF_DAY)
          && (evDuration <= requiredDuration) 
          && (numAttendees <= requiredNumAttendees)) {
            TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evStart, false);
            meetings.add(available);
            available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
      }
      // If an event is scheduled to begin at the start of the day, and it is the only event
      else if ((evStart == TimeRange.START_OF_DAY) && (evDuration <= requiredDuration)
          && (numAttendees <= requiredNumAttendees)) {
            TimeRange available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
            meetings.add(available);
            available = currTime.fromStartEnd(evEnd + evDuration, TimeRange.END_OF_DAY, true);
            meetings.add(available);
      }
      // The first event if the first two if conditions are not met (and if there are more events)
      else if ((i < 1) && (evDuration <= requiredDuration) 
          && (numAttendees <= requiredNumAttendees)) {
            TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evStart, false);
            meetings.add(available);
            available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
            meetings.add(available);
      }
      // After the first event (and if there are more events)
      else if ((i >= 1 && i < events.size() - 1) && (evDuration <= requiredDuration) 
          && (numAttendees <= requiredNumAttendees)) {
            TimeRange available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
            meetings.add(available);
      }
      i++;
      
      // Last event and it doesn't stop at the end of the day
      if ((events.size() > 1) && (i == events.size()) && (evDuration <= requiredDuration)
          && (numAttendees <= requiredNumAttendees)) {
            TimeRange available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
      }
      // Last event and it doesn't stop at the end of the day
      if ((events.size() > 1) && (i == events.size()) && (evDuration <= requiredDuration)
          && (numAttendees <= requiredNumAttendees)) {
            TimeRange available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
      }
    }

    if ((meetings.isEmpty()) && (requiredDuration < TimeRange.WHOLE_DAY.duration())) {
      meetings.add(TimeRange.WHOLE_DAY);
    }
    
    // Sort the events by start time
    ArrayList<TimeRange> options = new ArrayList<>(meetings);
    Collections.sort(options, TimeRange.ORDER_BY_START);
    return options;
  }
}
