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

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I was born in Orlando, Florida!', 'I have moved three times in my life, from Florida, to Virginia, to Michigan.', 'I have an older brother who recently graduated from Florida A&M University!',
       'I learned Japanese for two years and high school (and still know how to speak some phrases)!', 'I won an Xbox One S at a local NBA 2K Video Game Tournament in 2017!',
    	'I am a Detroit Lions fan.','I know how to create YouTube Videos using Adobe After Effects.','I played basketball for 10 years, and my favorite team is the Portland Trailblazers.'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}
