/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

function setCookie(name, value, days) {
    let expires = "";
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/";
}

function getCookie(name) {
    const nameEQ = `${name}=`;
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i].trim();
        if (cookie.indexOf(nameEQ) === 0) {
            return cookie.substring(nameEQ.length, cookie.length);
        }
    }
    return null;
}

document.addEventListener('DOMContentLoaded', () => {
    const pageIdentifier = window.location.pathname;  // Unique page identifier

    document.querySelectorAll('.expandable').forEach(item => {
        item.addEventListener('click', function (event) {
            if (event.target.closest('span[data-log], .icon-button')) {
                return;
            }
            const subRow = this.querySelector('.table-sub-row');
            if (subRow) {
                const isVisible = subRow.style.display === 'block';
                subRow.style.display = isVisible ? 'none' : 'block';
                const key = `${pageIdentifier}_${this.getAttribute('data-id')}`;
                setCookie(key, (!isVisible).toString(), 1);  // Set cookie for 1 day
            }
        });

        const key = `${pageIdentifier}_${item.getAttribute('data-id')}`;
        const isExpanded = getCookie(key) === 'true';
        if (isExpanded) {
            item.querySelector('.table-sub-row').style.display = 'block';
        }
    });

    updateSchedulerPauseResumeButton();
});

const eventsSource = new EventSource("/events");
eventsSource.onmessage = function (event) {
    const eventsContainer = document.getElementById("events");
    const eventsDiv = document.createElement("div");
    eventsDiv.textContent = event.data;
    eventsContainer.appendChild(eventsDiv);
    eventsContainer.scrollTop = eventsContainer.scrollHeight; // Auto scroll to bottom.
}

function openLog(element) {
    const log = element.getAttribute('data-log');
    const newWindow = window.open();
    newWindow.document.write(`
        <html lang="en">
            <head>
                <title>Log Details</title>
                <style>
                    body { font-family: Arial, sans-serif; font-size: 1.25em; background-color: #1e1e1e; color: #c7c7c7; }
                    pre { white-space: pre-wrap; word-wrap: break-word; }
                </style>
            </head>
            <body>
                <pre>${log}</pre>
            </body>
        </html>
    `);
    newWindow.document.close();
}

function openFullAudit() {
    window.open('/scheduler/audit', '_blank');
}

function openTaskAudit(button) {
    const itemName = encodeURIComponent(button.getAttribute('data-name'));
    const itemGroup = encodeURIComponent(button.getAttribute('data-group'));
    window.open(`/scheduler/audit/${itemName}/${itemGroup}`, '_blank');
}

function refreshPage() {
    location.reload();
}

function deleteAll() {
    fetch(`/scheduler/task`, {method: 'DELETE'})
        .then(response => {
            if (response.ok) {
                console.log('All tasks deleted successfully');
                window.location.reload();
            } else {
                console.error('Failed to delete all tasks');
                alert("Failed to delete all tasks.");
            }
        })
        .catch(error => console.error('Error:', error));
}

function toggleTaskPauseResume(button) {
    const itemName = encodeURIComponent(button.getAttribute('data-name'));
    const itemGroup = encodeURIComponent(button.getAttribute('data-group'));
    const state = button.getAttribute('data-state');
    const action = state === 'PAUSED' ? 'resume' : 'pause';

    fetch(`/scheduler/task/${itemName}/${itemGroup}/${action}`, {method: 'POST'})
        .then(response => {
            if (response.ok) {
                console.log('Task paused/resumed successfully');
                window.location.reload();
            } else {
                console.error('Failed to pause/resume task');
                alert("Failed to pause/resume task.");
            }
        })
        .catch(error => console.error('Error:', error));
}

function deleteTask(button) {
    const itemName = encodeURIComponent(button.getAttribute('data-name'));
    const itemGroup = encodeURIComponent(button.getAttribute('data-group'));

    fetch(`/scheduler/task/${itemName}/${itemGroup}`, {method: 'DELETE'})
        .then(response => {
            if (response.ok) {
                console.log('Task deleted successfully');
                window.location.reload();
            } else {
                console.error('Failed to delete task');
                alert("Failed to delete task.");
            }
        })
        .catch(error => console.error('Error:', error));
}

function toggleScheduler() {
    checkSchedulerState(function (state) {
        if (state.trim() === 'RUNNING') {
            pauseScheduler(confirmAndRefreshState);
        } else if (state.trim() === 'PAUSED') {
            resumeScheduler(confirmAndRefreshState);
        }
    });
}

function checkSchedulerState(callback) {
    console.log("Fetching scheduler state...");
    fetch('/scheduler/state', {
        method: 'GET',
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(state => {
            console.log('Scheduler state:', state);
            callback(state);
        })
        .catch(error => {
            console.error('Error fetching scheduler state:', error);
            alert('Error fetching scheduler state. Check console for details.');
        });
}

function pauseScheduler(callback) {
    fetch('/scheduler/pause', {method: 'POST'})
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to pause scheduler');
            }
            console.log('Paused scheduler');
            callback();
        })
        .catch(error => {
            console.error('Error pausing scheduler:', error);
            alert('Error pausing scheduler. Check console for details.');
        });
}

function resumeScheduler(callback) {
    fetch('/scheduler/resume', {method: 'POST'})
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to resume scheduler');
            }
            console.log('Resumed scheduler');
            callback();
        })
        .catch(error => {
            console.error('Error resuming scheduler:', error);
            alert('Error resuming scheduler. Check console for details.');
        });
}

function confirmAndRefreshState() {
    checkSchedulerState(function (state) {
        setSchedulerButtonState(state);
        window.location.reload(); // Reload the page after state has been committed
    });
}

function setSchedulerButtonState(state) {
    const button = document.getElementById('pauseResumeButton');
    console.log('Setting button state for:', state); // Debug log

    if (state.trim() === 'RUNNING') {
        button.textContent = 'Pause Scheduler';
        button.classList.add('pause');
        button.classList.remove('resume');
    } else if (state.trim() === 'PAUSED') {
        button.textContent = 'Resume Scheduler';
        button.classList.add('resume');
        button.classList.remove('pause');
    } else if (state.trim() === 'STOPPED') {
        button.textContent = 'Scheduler Stopped';
        button.disabled = true;
    }
}

function updateSchedulerPauseResumeButton() {
    checkSchedulerState(function (state) {
        setSchedulerButtonState(state);
    });
}
