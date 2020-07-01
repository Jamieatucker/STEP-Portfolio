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
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Find events that match the requested number of attendees and duration
    // and adds it to a set
    Set<TimeRange> meetings = new HashSet<>();
    final long requiredDuration = request.getDuration();
    final Collection<String> requiredAttendees = request.getAttendees();
    int requiredNumAttendees = requiredAttendees.size();
    String[] requiredArr = new String[requiredNumAttendees];
    String requiredPerson = "";
    if (requiredNumAttendees >= 1) {
      requiredAttendees.toArray(requiredArr);
      requiredPerson = requiredArr[0];
    }

    // Convert the collection to an event array and keep its contents
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

      // Find the times where a meeting can be planned
      if (events.size() == 1) {
        onlyOneEvent(events, event, currTime, meetings, evArr, requiredPerson, currPerson, requiredDuration);
      }
      else if (events.size() > 1) {
        moreThanOneEvent(events, event, currTime, meetings, evArr, requiredPerson, currPerson, requiredDuration, i);
        overlappingEvents(events, event, currTime, meetings, evArr, i);
        
        // Calculates the time left available in the day
        TimeRange firstEvent = evArr[k - 1].getWhen();
        TimeRange secondEvent = evArr[k].getWhen();
        timeLeft = (TimeRange.WHOLE_DAY.duration() - (secondEvent.duration() + firstEvent.duration()));
      }
      
      // Will be used for the last event in the Set
      if (i < events.size()){
        i++;
      }

      // Last event in the TimeRange collection (if there is more than one)
      lastEvent(events, event, currTime, requiredDuration, meetings, evArr, i);
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

  /**
   * If there is only one event is the collection.
   */
  private void onlyOneEvent(Collection<Event> events, Event event, TimeRange currTime,
      Set<TimeRange> meetings, Event[] evArr, String requiredPerson, String currPerson, long requiredDuration) {

    // Get the time information
    int evStart = currTime.start();
    int evEnd = currTime.end();
    int evDuration = currTime.duration();
    
    // If there is only one event scheduled and it doesn't stop at the end of the day
    if ((events.size() == 1) && (evStart != TimeRange.START_OF_DAY)
        && (evDuration == requiredDuration)
        && (currPerson.equals(requiredPerson))) {
          TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evStart, false);
          meetings.add(available);
          available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
          meetings.add(available);
    }
    // If an event is scheduled to begin at the start of the day, and it is the only event
    else if ((events.size() == 1) && (evStart == TimeRange.START_OF_DAY)
        && (evDuration == requiredDuration)
        && (currPerson.equals(requiredPerson))) {
          TimeRange available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
          meetings.add(available);
    }
  }

  /**
   * If there is more than one event in the collection.
   */
  private void moreThanOneEvent(Collection<Event> events, Event event, TimeRange currTime,
      Set<TimeRange> meetings, Event[] evArr, String requiredPerson, String currPerson, long requiredDuration, int i) {
    // Will be used in the conditionals
    int k = 1;

    // Get the time information
    int evStart = currTime.start();
    int evEnd = currTime.end();
    int evDuration = currTime.duration();
    TimeRange firstEvent = evArr[k - 1].getWhen();
    TimeRange secondEvent = evArr[k].getWhen();
    int firstEventStart = evArr[k - 1].getWhen().start();
    int firstEventEnd = evArr[k - 1].getWhen().end();
    int secondEventStart = evArr[k].getWhen().start();
    int secondEventEnd = evArr[k].getWhen().end();

    // If there is more than one event, and is scheduled to begin at the start of the day
    if ((evStart == TimeRange.START_OF_DAY)
        && (currPerson.equals(requiredPerson))
        && (secondEventStart - firstEventEnd == requiredDuration)) {
          TimeRange available = currTime.fromStartEnd(evEnd, secondEventStart, false);
          meetings.add(available);
    }
    // If there is more than one event, and is not scheduled to begin at the start of the day
    else if ((i < 1) && (evDuration <= requiredDuration)
        && (currPerson.equals(requiredPerson))) {
          TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, evStart, false);
          meetings.add(available);
          available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
          meetings.add(available);
    }
    // After the first event (and if there are more events)
    else if ((i >= 1 && i <= events.size() - 1)
        && (!firstEvent.overlaps(secondEvent))
        && (evDuration == requiredDuration)
        && (currPerson.equals(requiredPerson))) {
          TimeRange available = currTime.fromStartEnd(evEnd, evEnd + evDuration, false);
          meetings.add(available);
    }
  }

  /**
   * If there are any overlapping events.
   */
  private void overlappingEvents(Collection<Event> events, Event event, TimeRange currTime,
      Set<TimeRange> meetings, Event[] evArr, int i) {
    // Will be used in the conditionals
    int k = 1;

    // Get the time information
    int evStart = currTime.start();
    int evEnd = currTime.end();
    int evDuration = currTime.duration();
    TimeRange firstEvent = evArr[k - 1].getWhen();
    TimeRange secondEvent = evArr[k].getWhen();
    int firstEventStart = evArr[k - 1].getWhen().start();
    int firstEventEnd = evArr[k - 1].getWhen().end();
    int secondEventStart = evArr[k].getWhen().start();
    int secondEventEnd = evArr[k].getWhen().end();

    // If any of the events overlap with one another
    if (firstEvent.overlaps(secondEvent)) {
      TimeRange available = currTime.fromStartEnd(TimeRange.START_OF_DAY, firstEventStart, false);
      meetings.add(available);
      
      // If the first event's end time is before the second event's end time
      if (firstEventEnd <= secondEventEnd) {
        available = currTime.fromStartEnd(secondEventEnd, TimeRange.END_OF_DAY, true);
        meetings.add(available);
      }
      else {
        available = currTime.fromStartEnd(firstEventEnd, TimeRange.END_OF_DAY, true);
        meetings.add(available);
      }
    }
  }

  /**
   * If there is more than one event in the collection and it is the last event
   */
  private void lastEvent(Collection<Event> events, Event event, TimeRange currTime,
      long requiredDuration, Set<TimeRange> meetings, Event[] evArr, int i) {
    // Will be used in the conditionals
    int k = 1;

    // Get the time information
    int evStart = currTime.start();
    int evEnd = currTime.end();
    int evDuration = currTime.duration();

    if (events.size() > 1 && i == events.size()) {
      TimeRange firstEvent = evArr[k - 1].getWhen();
      TimeRange secondEvent = evArr[k].getWhen();
      int firstEventStart = evArr[k - 1].getWhen().start();
      int firstEventEnd = evArr[k - 1].getWhen().end();
      int secondEventStart = evArr[k].getWhen().start();
      int secondEventEnd = evArr[k].getWhen().end();

      // Last event and it doesn't stop at the end of the day
      if ((evEnd != TimeRange.END_OF_DAY)
          && (!firstEvent.overlaps(secondEvent))
          && (evDuration == requiredDuration)) {
            TimeRange available = currTime.fromStartEnd(evEnd, TimeRange.END_OF_DAY, true);
            meetings.add(available);
      }
      // Last event and it stops at the end of the day
      else if ((evEnd == TimeRange.END_OF_DAY)
          && (!firstEvent.overlaps(secondEvent))
          && (evDuration == requiredDuration)) {

            TimeRange available = currTime.fromStartEnd(firstEventEnd, evStart, true);
            meetings.add(available);
      }
    }
  }
}
