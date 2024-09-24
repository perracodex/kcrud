/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

function setupEventSource() {
    const eventsSource = new EventSource("/events");

    eventsSource.onmessage = function (event) {
        const eventsContainer = document.getElementById("events");
        const eventsDiv = document.createElement("div");
        eventsDiv.textContent = event.data;
        eventsContainer.appendChild(eventsDiv);
        eventsContainer.scrollTop = eventsContainer.scrollHeight;
    }
}
