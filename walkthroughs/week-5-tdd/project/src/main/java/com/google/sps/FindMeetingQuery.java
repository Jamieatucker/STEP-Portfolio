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

import java.util.Arrays;
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
    long requiredDuration = request.getDuration();
    Collection<String> requiredAttendees = request.getAttendees();
    int requiredNumAttendees = requiredAttendees.size();
    String[] requiredArr = new String[requiredNumAttendees];
    String requiredPerson = "";
    if (requiredNumAttendees >= 1) {
      requiredAttendees.toArray(requiredArr);
      requiredPerson = requiredArr[0];
    }

    // Convert the collection to an event array
    Event[] evArr = events.toArray(new Event[events.size()]);
    long timeLeft = TimeRange.WHOLE_DAY.duration();
    
    // Will be used in the for-each loop
    int i = 0;
    int k = 1;

    for (Event event : events) {
      Set<String> currAttendees = event.getAttendees();
      int numAttendees = currAttendees.size();
      String[] people = new String[numAttendees];
      currAttendees.toArray(people);
      String currPerson = "";

      // Check how many attendees are in the current event
      if (numAttendees == 1) {
        currPerson = people[0];
      }
      else if (numAttendees > 1) {
        currPerson = people[i];
        requiredPerson = requiredArr[i];
      }

      // Get the time information
      TimeRange currTime = event.getWhen();
      int evStart = currTime.start();
      int evEnd = currTime.end();
      int evDuration = currTime.duration();

      //////////////////////
      // One Event + More //
      //////////////////////

      // If there is only one event scheduled and it doesn't stop at the end of the day
      if ((events.size() == 1) && (evStart != TimeRange.START_OF_DAY)
          && (evDuration == requiredDuration)
          && (currPerson.equals(requiredPerson))) {
            TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evStart, false);
            meetings.add(available);
            available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
            break;
      }
      // If an event is scheduled to begin at the start of the day, and it is the only event
      else if ((events.size() == 1) && (evStart == TimeRange.START_OF_DAY)
          && (evArr[k - 1].getWhen().overlaps(evArr[k].getWhen()) == false)
          && (evDuration == requiredDuration)
          && (currPerson.equals(requiredPerson))) {
            TimeRange available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
            break;
      }
      // If an event is scheduled to begin at the start of the day, and there is more than one event
      else if ((evStart == TimeRange.START_OF_DAY)
          && (currPerson.equals(requiredPerson))
          && (evArr[k].getWhen().start() - evArr[k - 1].getWhen().end() == requiredDuration)) {
            TimeRange available = currTime.fromStartEnd(evEnd, evArr[k].getWhen().start(), false);
            meetings.add(available);
      }
      // If there is more than one event
      else if ((i < 1) && (evDuration <= requiredDuration)
          && (currPerson.equals(requiredPerson))) {
            TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evStart, false);
            meetings.add(available);
            available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
            meetings.add(available);
      }
      // After the first event (and if there are more events)
      else if ((i >= 1 && i <= events.size() - 1)
          && (evArr[k - 1].getWhen().overlaps(evArr[k].getWhen()) == false)
          && (evDuration == requiredDuration)
          && (currPerson.equals(requiredPerson))) {
            TimeRange available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
            meetings.add(available);
      }

      ////////////////////////
      // Overlapping Events //
      ////////////////////////

      // If any of the events overlap with one another
      if (events.size() > 1 && evArr[k - 1].getWhen().overlaps(evArr[k].getWhen()) == true) {
        // If the first event's end time is before the second event's end time
        if (evArr[k - 1].getWhen().end() <= evArr[k].getWhen().end()) {
          TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evArr[k - 1].getWhen().start(), false);
          meetings.add(available);
          available = currTime.fromStartEnd(evArr[k].getWhen().end(), TimeRange.END_OF_DAY, true);
          meetings.add(available);
          break;
        }
        else {
          TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evArr[k - 1].getWhen().start(), false);
          meetings.add(available);
          available = currTime.fromStartEnd(evArr[k - 1].getWhen().end(), TimeRange.END_OF_DAY, true);
          meetings.add(available);
          break;
        }
      }
      
      // Will be used for the last event in the Set
      if (i < events.size()){
        i++;
      }

      // Calculates the time left available in the day
      if (events.size() > 1) {
        timeLeft = (TimeRange.WHOLE_DAY.duration() - (evArr[k].getWhen().duration() + evArr[k - 1].getWhen().duration()));
      }

      ////////////////
      // Last Event //
      ////////////////

      // Last event and it doesn't stop at the end of the day
      if ((events.size() > 1) && (i == events.size()) && (evEnd != TimeRange.END_OF_DAY)
          && (evDuration == requiredDuration)) {
            TimeRange available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
      }
      // Last event and it stops at the end of the day
      else if ((events.size() > 1) && (i == events.size()) && (evEnd == TimeRange.END_OF_DAY)
          && (evDuration == requiredDuration)) {
            TimeRange available = currTime.fromStartEnd(evArr[k - 1].getWhen().end(), evStart, true);
            meetings.add(available);
      }
    }

    // If there are no events planned or if the meeting request is longer than one day
    if ((meetings.isEmpty()) && (timeLeft > requiredDuration)
      && (requiredDuration < TimeRange.WHOLE_DAY.duration())) {
        meetings.add(TimeRange.WHOLE_DAY);
    }
    
    // Sort the events by start time
    ArrayList<TimeRange> options = new ArrayList<>(meetings);
    Collections.sort(options, TimeRange.ORDER_BY_START);
    return options;
  }
}
