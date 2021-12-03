'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');
const searchText = document.querySelector('#search');

let username = null;
let socket = null;


const colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

const connect = (event) => {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        socket = new WebSocket("ws://localhost:8080/chat");

        socket.onopen = () => {
            //loadHistory();
            socket.send(JSON.stringify({sender: username, type: 'JOIN'}));
            connectingElement.classList.add('hidden');
        };

        socket.onmessage = (event) => onMessageReceived(event.data);

        socket.onclose = (event) => {
            username = null;
            if (event.wasClean) {
                console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
            } else {
                alert('[close] Connection died');
            }
        };

        socket.onerror =  (error) => {
            console.error(`[error] ${error.message}`);
            connectingElement.textContent = 'Unable to connect to the server! Please refresh the page and try again.';
            connectingElement.style.color = 'red';
        }

    }
    event.preventDefault();
}


const send = (event) => {
    const messageContent = messageInput.value;

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


const onMessageReceived = (payload) => {
    const message = JSON.parse(payload);
    renderMessage(message);
}

const renderMessage = (message) => {
    const messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
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
        avatarElement.style['background-color'] = getAvatarColor(getHash(message.sender));

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

const getHash = (messageSender) => {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    return hash;
}
const getAvatarColor = (hash) => {
    const index = Math.abs(hash % colors.length);
    return colors[index];
}

searchText.addEventListener('keyup', (ev) => {
    let text = ev.target.value;
    let pat = new RegExp(text, 'i');

    for(const element of messageArea.children) {
        if (pat.test(element.innerText)) {
            element.classList.remove("hidden");
        } else {
            element.classList.add("hidden");
        }

    }
});

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', send, true)