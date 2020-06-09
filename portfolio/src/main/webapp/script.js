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
  // Some hard-coded facts about me
  const facts = [
      'I was born in Orlando, Florida!',
      'I have moved three times in my life, from Florida, to Virginia, to Michigan.',
      'I have an older brother who recently graduated from Florida A&M University!',
      'I learned Japanese for two years in high school (and still know how to speak some phrases)!',
      'I won an Xbox One S at a local NBA 2K Video Game Tournament in 2017!',
      'I am a Detroit Lions fan.',
      'I know how to create YouTube Videos using Adobe After Effects.',
      'I played basketball for 10 years, and my favorite team is the Portland Trailblazers.'
   ];

  // Pick a random fact
  let fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page
  const factContainer = document.querySelector('#fact-container')
  factContainer.style.visibility = 'visible';
  factContainer.innerText = fact;
}

/**
 * Deletes all the comments from the 'Comments' servlet.
 */
async function deleteDataUsingAsyncAwait() {
  // Retrieve the data from '/comments' and delete the comments from the admin page
  /* TODO: handle errors */
  const response = await fetch('/comments', {
    method: 'DELETE',
  });
  
  // Delete the data from the page
  const dataContainer = document.querySelector('#data-container');
  dataContainer.style.visibility = 'hidden';
  dataContainer.innerHTML = data;
}

/**
 * Adds the data from the Comments servlet using async/await (the return values are used directly), and converts it to a JSON.
 */
async function getDataUsingAsyncAwait() {
  // Retrieve the data from '/comments'
  /* TODO: handle errors */
  const response = await fetch('/comments?numComments=' + document.querySelector('#numComments').value);
  const data = await response.json();
  var text = "";
  for(i = 0; i < data.length; i++){
    text += "<b>" + data[i].name + " " + data[i].email + "</b>" + " " + "<i>" + data[i].comment + "</i><br/>";
  }

  // Add the data to the page
  const dataContainer = document.querySelector('#data-container');
  dataContainer.style.visibility = 'visible';
  dataContainer.innerHTML = text;
}

/**
 * Hides the comment section.
 */
function hideData() {
  document.querySelector('#data-container').style.visibility = 'hidden';
}

/**
 * Reveals my hidden talent to the hidden talent page.
 */
function revealHiddenTalent() { 
  // Add the hidden talent on the page
  const hiddenTalentContainer = document.querySelector('#hiddentalent-container');
  hiddenTalentContainer.style.visibility = 'visible';

  // Reveal the GIF on the page
  document.querySelector('#rollsafe').style.visibility = 'visible';

  // Reveal the audio file on the page
  document.querySelector('#music').style.visibility = 'visible';
}

/**
 * Submits the comment to the '/comment' servlet.
 */
async function submitComment() {
  // Retrieve the data from '/comments'
  const data = {
    'name': document.querySelector('#username').value,
    'email': document.querySelector('#email').value,
    'comment': document.querySelector('#comment').value,
  };
  const response = await fetch('/comments', {
    method: 'POST',
    body: JSON.stringify(data)
  });

  // Set the values to empty strings so the next data can be itself
  document.querySelector('#username').value = "";
  document.querySelector('#email').value = "";
  document.querySelector('#comment').value = "";

  // Put the text on the page
  getDataUsingAsyncAwait();
}
