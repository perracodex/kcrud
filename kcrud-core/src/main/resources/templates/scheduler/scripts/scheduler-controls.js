/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

function updateSchedulerPauseResumeButton() {
    checkSchedulerState(state => setSchedulerButtonState(state));
}

function checkSchedulerState(callback) {
    fetch('/admin/scheduler/state', {
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
    fetch('/admin/scheduler/pause', {method: 'POST'})
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
    fetch('/admin/scheduler/resume', {method: 'POST'})
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
