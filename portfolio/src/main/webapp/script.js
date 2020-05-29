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
      ['I have a cat named Ash and he loves to jump on my desk!', 
       'I am a student at Cornell University majoring in Operations Research.', 
       'I live in Miami, FL.', 
       'My family is from Colombia.', 
       'I love doing Project Euler! (add me: 1047518_OObct1RuC9uGHxendSH11pjh77Nw36lG)'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('factContainer');
  factContainer.innerText = fact;
}
