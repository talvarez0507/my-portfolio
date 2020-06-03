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


const allFacts = [
  'I have a cat named Ash and he loves to jump on my desk!', 
  'I am a student at Cornell University majoring in Operations Research.', 
  'I live in Miami, FL.', 
  'My family is from Colombia.', 
  'I love doing Project Euler! (add me: 1047518_OObct1RuC9uGHxendSH11pjh77Nw36lG)'
];

var facts = [...allFacts];
var finishedOnce = false;

/**
 * Adds a random fact to the page.
 */

function addRandomFact() {
  const fact = getNewRandomFact();
  setFactText(fact);
}

function getFactText() {
  const factContainer = document.getElementById('factContainer');
  return factContainer.innerText;
}

function setFactText(message) {
  const factContainer = document.getElementById('factContainer');
  factContainer.innerText = message;
}

function getNewRandomFact() {
  if (facts.length > 0) {
    const fact = facts[Math.floor(Math.random() * facts.length)];
    const lastFact = getFactText();
    if (fact === lastFact) {
      return getNewRandomFact();
    } else {
      facts.splice(facts.indexOf(fact), 1);
      return fact;
    }
  } else if (!finishedOnce) {
    finishedOnce = true;
    facts = [...allFacts];
    return "You've already seen all the facts! You can keep seeing them by clicking again.";
  } else {
    facts = [...allFacts];
    return getNewRandomFact();
    }
}

function getComments() {
  fetch('/data').then(response => response.json()).then((comments) => {
    const commentsListElement = document.getElementById('commentContainer');
    commentsListElement.innerHTML = '';
    for (i = 0; i < comments.length; i++) {
      commentsListElement.appendChild(
          createListElement(comments[i].text));
    }
  });
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
