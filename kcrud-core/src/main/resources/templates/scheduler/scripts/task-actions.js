/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

function openFullAudit() {
    window.open('/admin/scheduler/audit', '_blank');
}

function openTaskAudit(button) {
    const groupId = encodeURIComponent(button.getAttribute('data-group-id'));
    const taskId = encodeURIComponent(button.getAttribute('data-task-id'));
    window.open(`/admin/scheduler/audit/task?groupId=${groupId}&taskId=${taskId}`, '_blank');
}

function refreshPage() {
    location.reload();
}

function deleteAll() {
    fetch(`/admin/scheduler`, {method: 'DELETE'})
        .then(response => {
            if (response.ok) {
                console.log('All tasks deleted successfully');
                window.location.reload();
            } else {
                console.error('Failed to delete all tasks');
                alert("Failed to delete all tasks.");
            }
        }).catch(error => console.error('Error:', error));
}

function deleteTask(button) {
    const groupId = encodeURIComponent(button.getAttribute('data-group-id'));
    const taskId = encodeURIComponent(button.getAttribute('data-task-id'));
    const taskRow = button.closest('.table-container');  // Get the row element of the task to remove.
    const currentGroup = document.getElementById('groupSelect').value;  // Get the current selected group.

    fetch(`/admin/scheduler/task?groupId=${groupId}&taskId=${taskId}`, {method: 'DELETE'})
        .then(response => {
            if (response.ok) {
                taskRow.remove();  // Remove the task row from the DOM.

                // Check if we are filtering by a specific group (not "all").
                if (currentGroup !== 'all') {
                    const remainingTasks = document.querySelectorAll(
                        `.table-container[data-group-id="${decodeURIComponent(groupId)}"]`
                    );

                    if (remainingTasks.length === 0) {
                        // No tasks left for this group, so remove the group from the select box.
                        const groupSelect = document.getElementById('groupSelect');
                        const optionToRemove = groupSelect.querySelector(
                            `option[value="${decodeURIComponent(groupId)}"]`
                        );
                        if (optionToRemove) {
                            optionToRemove.remove();
                        }

                        // Reset to 'all' group and update the URL accordingly.
                        groupSelect.value = 'all';
                        window.history.pushState({groupId: 'all'}, '', '?groupId=all'); // Update URL.
                        fetchTasks('all');
                    } else {
                        // If tasks are still remaining for the selected group, refresh tasks for the current group.
                        fetchTasks(currentGroup);
                    }
                } else {
                    // If we're already in "All Groups", just refresh the task list for "All Groups".
                    fetchTasks('all');
                }

            } else {
                alert("Failed to delete task");
            }
        }).catch(error => console.error('Error:', error));
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

// Pause or Resume a task
function toggleTaskPauseResume(button) {
    const groupId = encodeURIComponent(button.getAttribute('data-group-id'));
    const taskId = encodeURIComponent(button.getAttribute('data-task-id'));
    const state = button.getAttribute('data-state');
    const action = state === 'PAUSED' ? 'resume' : 'pause';

    fetch(`/admin/scheduler/task/${action}?groupId=${groupId}&taskId=${taskId}`, {method: 'POST'})
        .then(response => {
            if (response.ok) {
                const selectedGroup = document.getElementById('groupSelect').value;
                fetchTasks(selectedGroup);  // Use selected group in dropdown.
            } else {
                alert("Failed to update task state");
            }
        }).catch(error => console.error('Error:', error));
}

function resendTask(button) {
    const groupId = encodeURIComponent(button.getAttribute('data-group-id'));
    const taskId = encodeURIComponent(button.getAttribute('data-task-id'));

    fetch(`/admin/scheduler/task/resend?groupId=${groupId}&taskId=${taskId}`, {method: 'POST'})
        .then(response => {
            if (response.ok) {
                console.log('Task resent successfully');
                window.location.reload();
            } else {
                console.error('Failed to resend task');
                alert("Failed to pause/resume task.");
            }
        }).catch(error => console.error('Error:', error));
}
