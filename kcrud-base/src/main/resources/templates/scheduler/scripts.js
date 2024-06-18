/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

const SCHEDULER_STATE = {
    RUNNING: 'RUNNING',
    PAUSED: 'PAUSED',
    STOPPED: 'STOPPED'
};

document.addEventListener('DOMContentLoaded', () => {
    initializePage();
});

let isPageInitialized = false;  // Flag to prevent double initialization

function initializePage() {
    if (isPageInitialized) {
        console.log("Page already initialized");
        return;
    }

    isPageInitialized = true;
    console.log("Initializing page...");

    try {
        fetchGroups();
        updateSchedulerPauseResumeButton();
        setupTableExpandCollapse();
        setupEventSource();
    } catch (error) {
        console.error("Error during page initialization:", error);
    }
}

function fetchGroups() {
    fetch('/scheduler/task/group', {
        method: 'GET',
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        }
    })
        .then(response => response.json())
        .then(groups => populateGroupSelect(groups))
        .catch(error => {
            console.error('Error fetching groups:', error);
        });
}

function populateGroupSelect(groups) {
    const groupSelect = document.getElementById('groupSelect');
    const currentGroup = new URLSearchParams(window.location.search).get('group');

    groupSelect.innerHTML = '<option value="all">All Groups</option>';

    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group;
        option.textContent = group;
        groupSelect.appendChild(option);
    });

    groupSelect.value = currentGroup || 'all';
    groupSelect.addEventListener('change', handleGroupChange);
}

function handleGroupChange() {
    const selectedGroup = document.getElementById('groupSelect').value;
    const url = new URL(window.location);

    if (selectedGroup === 'all') {
        url.searchParams.delete('group');
    } else {
        url.searchParams.set('group', selectedGroup);
    }

    window.location.href = url.toString();
}

function setupTableExpandCollapse() {
    const pageIdentifier = window.location.pathname;

    document.querySelectorAll('.table-container').forEach(container => {
        const key = `${pageIdentifier}_${container.getAttribute('data-id')}`;
        const isExpandedInitially = getCookie(key) === 'true';
        const subRow = container.querySelector('.table-sub-row');

        subRow.style.display = isExpandedInitially ? 'block' : 'none';

        const trigger = container.querySelector('.expand-trigger');
        trigger.innerHTML = isExpandedInitially ? '-' : '+';

        trigger.addEventListener('click', function () {
            const isExpanded = getComputedStyle(subRow).display === 'block';
            subRow.style.display = isExpanded ? 'none' : 'block';
            setCookie(key, (!isExpanded).toString(), 1);
            trigger.innerHTML = isExpanded ? '+' : '-';
        });
    });
}

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

function updateSchedulerPauseResumeButton() {
    checkSchedulerState(state => setSchedulerButtonState(state));
}

function checkSchedulerState(callback) {
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
        .then(state => callback(state))
        .catch(error => {
            console.error('Error fetching scheduler state:', error);
            alert('Error fetching scheduler state. Check console for details.');
        });
}

function setSchedulerButtonState(state) {
    const button = document.getElementById('toggleScheduler');
    state = state.trim();

    if (state === SCHEDULER_STATE.RUNNING) {
        button.textContent = 'Pause Scheduler';
        button.classList.add('pause');
        button.classList.remove('resume');
    } else if (state === SCHEDULER_STATE.PAUSED) {
        button.textContent = 'Resume Scheduler';
        button.classList.add('resume');
        button.classList.remove('pause');
    } else if (state === SCHEDULER_STATE.STOPPED) {
        button.textContent = 'Scheduler Stopped';
        button.disabled = true;
    }

    button.addEventListener('click', toggleScheduler);
}

function toggleScheduler() {
    checkSchedulerState(state => {
        if (state.trim() === SCHEDULER_STATE.RUNNING) {
            pauseScheduler(confirmAndRefreshState);
        } else if (state.trim() === SCHEDULER_STATE.PAUSED) {
            resumeScheduler(confirmAndRefreshState);
        }
    });
}

function pauseScheduler(callback) {
    fetch('/scheduler/pause', {method: 'POST'})
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to pause scheduler');
            }
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
            callback();
        })
        .catch(error => {
            console.error('Error resuming scheduler:', error);
            alert('Error resuming scheduler. Check console for details.');
        });
}

function confirmAndRefreshState() {
    checkSchedulerState(state => {
        setSchedulerButtonState(state);
        window.location.reload();
    });
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
