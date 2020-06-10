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
  'I enjoy doing Project Euler! (add me: 1047518_OObct1RuC9uGHxendSH11pjh77Nw36lG)'
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

function setComments(){
  var field1 = document.getElementById('maxComments');
  field1.innerText = 'Comments Showing: 0';
  const maxComments= document.getElementById('numberOf').value;
  getComments(maxComments);
}

function getComments(maxComments) {
  fetch('/data?maxComments='+maxComments.toString()).then(response => response.json()).then((comments) => {
    const commentsListElement = document.getElementById('commentContainer');
    commentsListElement.innerHTML = '';
    comments.forEach(addCommentToPage);
  });
}

function addCommentToPage(comment) {
  var field2 = document.getElementById('maxComments');
  const num = parseInt(field2.innerText.substring(18));
  field2.innerText = "Comments Showing: "+(num+1).toString();

  commentsListElement = document.getElementById('commentContainer');
  commentsListElement.innerHTML += createCommentDiv(comment.text,num+1);
}

function getNumberOfComments() {
  var commentElement = document.getElementById('maxComments');
  const num = parseInt(commentElement.innerText.substring(18));
  return num;
}

function createCommentDiv(text,num) {
  return "<div id=\"comment"+num.toString()+"\">"+text+"</div>";
}

function deleteComments() {
  var init = { method: 'POST'};
  var request = new Request('/delete-data',init);

  fetch(request).then(getComments(0));
  var field1 = document.getElementById('maxComments');
  field1.innerText = 'Comments Showing: 0';
}

function checkUser() {
  fetch('/login').then(response => response.json()).then((html) => {
    // If User has no nickname, display nickname form
    if (html[0]==="needNickname") {
        showNicknameForm();
        html.splice(0, 1);
        arrayToUserInfo(html);
        loadComments(0);
    // If User is not logged in
    } else if (html[0]==="needLogin") {
        html.splice(0, 1);
        arrayToUserInfo(html);
        loadComments(1);
    } else {
        arrayToUserInfo(html);
        loadComments(2);
    }
    
  });
}

function showNicknameForm() {
    fetch('/nickname').then(response => response.json()).then((html) => {
    arrayToUserInfo(html);
    });
}

function arrayToUserInfo(array) {
    array.forEach(textToUserInfo);
}

function textToUserInfo(text) {
    document.getElementById('userInfo').innerHTML += text;
}

function loadComments(num) {
    commentElement = document.getElementById('comments');
    messageElement = document.getElementById('possibleMessage');
    if (num===0) {
        commentElement.style.display = "none";
        messageElement.innerHTML = "<h2>You need to set your nickname to see comments.</h2>" 
    } else if (num===1){
        commentElement.style.display = "none";
        messageElement.innerHTML = "<h2>You need to log in to see comments.</h2>" 
    } else {
        commentElement.style.display = "block";
    }
}

function performTranslation(text, resultContainer) {
  const languageCode = document.getElementById('language').value;

  resultContainer.innerText = 'Loading...';

  const params = new URLSearchParams();
  params.append('text', text);
  params.append('languageCode', languageCode);

  fetch('/translate', {
    method: 'POST',
    body: params
  }).then(response => response.text())
  .then((translatedMessage) => {
    resultContainer.innerText = translatedMessage;
  });
}

function translateComments() {
  var amount = getNumberOfComments();
  for (i = 1; i <= amount; i++) {
    const container = document.getElementById('comment'+i.toString())
    const text = container.innerText;
    performTranslation(text,container);    
    }
}
