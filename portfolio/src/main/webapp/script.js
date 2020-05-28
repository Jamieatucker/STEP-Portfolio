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
 * Adds a random fact to the home page.
 */
function addRandomFact() {
  const facts =
      ['I was born in Orlando, Florida!', 'I have moved three times in my life, from Florida, to Virginia, to Michigan.', 'I have an older brother who recently graduated from Florida A&M University!',
       'I learned Japanese for two years in high school (and still know how to speak some phrases)!', 'I won an Xbox One S at a local NBA 2K Video Game Tournament in 2017!',
    	'I am a Detroit Lions fan.','I know how to create YouTube Videos using Adobe After Effects.','I played basketball for 10 years, and my favorite team is the Portland Trailblazers.'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Reveals my hidden talent to the hidden talent page
 */
function revealHiddenTalent(){
    const hiddenTalent = 'Can create music mashups! \n\nThe inspiration came to me from listening to hundreds of music mashups on YouTube. People were mashing vaporwave and hip hop together like it' + 
    ' was peanut butter and jelly. They were mashing video game soundtracks and pop songs together like mash potatoes and gravy. I was entranced by the musical artistry.' + 
    ' I have a playlist of over 50 of my favorite music mashups.\n\nI created my first music mashup in late September 2018. I spent weeks using the program Audacity making sure it sounded as' + 
    ' fluid as possible, with little training going into it. It may not have sounded the best, but I was thrilled to have created a mashup that I wanted, and had sounded coherent.' + 
    ' The audio file below contains one of my mashups, called "Highest in the Woods Instrumental". Feel free to listen if you would like!\n';

	// Add it to the page
    const hiddenTalentContainer = document.getElementById('hiddentalent-container');
    hiddenTalentContainer.innerText = hiddenTalent;
}
