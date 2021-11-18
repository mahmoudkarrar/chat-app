'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');

let username = null;
let socket = null;

const colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        socket = new WebSocket("ws://localhost:8080/chat");

        socket.onopen = function() {

            socket.send(JSON.stringify({sender: username, type: 'JOIN'}))

            connectingElement.classList.add('hidden');
        };

        socket.onmessage = function(event) {
            onMessageReceived(event.data);
        };

        socket.onclose = function(event) {
            username = null;
            if (event.wasClean) {
                console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
            } else {
                // e.g. server process killed or network down
                // event.code is usually 1006 in this case
                alert('[close] Connection died');
            }
        };

        socket.onerror = function(error) {
            console.error(`[error] ${error.message}`);
            connectingElement.textContent = 'Unable to connect to the server! Please refresh the page and try again.';
            connectingElement.style.color = 'red';
        }

    }
    event.preventDefault();
}


function send(event) {
    const messageContent = messageInput.value.trim();

    if (messageContent) {
        const chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        socket.send(JSON.stringify(chatMessage));
        messageInput.value = '';
        event.preventDefault();
    }
}


function onMessageReceived(payload) {
    loadHistory();
    const message = JSON.parse(payload);

    const messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        const avatarElement = document.createElement('i');
        const avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        const usernameElement = document.createElement('span');
        const usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    const textElement = document.createElement('p');
    const messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    const index = Math.abs(hash % colors.length);
    return colors[index];
}

async function loadHistory() {

    try {
        const data = await axios.get('/chat/history/all');
        console.log("data " +data);
    } catch (error) {
       console.error(error);
    }
}
usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', send, true)