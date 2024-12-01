// noinspection JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

// Fetch groups and populate the select dropdown
function fetchGroups() {
    fetch('/admin/scheduler/group', {
        method: 'GET',
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        }
    }).then(response => response.json())
        .then(groups => {
            populateGroupSelect(groups);
            const defaultGroup = new URLSearchParams(window.location.search).get('groupId') || '';
            document.getElementById('groupSelect').value = defaultGroup; // Ensure the correct group is selected.
            fetchTasks(defaultGroup);
        }).catch(error => {
        console.error('Error fetching groups:', error);
    });
}

// Populate the group select dropdown with the available groups
function populateGroupSelect(groups) {
    const groupSelect = document.getElementById('groupSelect');
    const currentGroup = new URLSearchParams(window.location.search).get('groupId');

    groupSelect.innerHTML = '<option value="">All Groups</option>';

    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.groupId;
        option.textContent = group.description
            ? `${group.groupId} | ${group.description}`
            : group.groupId; // Show description if available, otherwise just groupId
        groupSelect.appendChild(option);
    });

    groupSelect.value = currentGroup || '';
    groupSelect.addEventListener('change', handleGroupChange);
}

// Handle group change
function handleGroupChange() {
    const selectedGroup = document.getElementById('groupSelect').value;
    window.history.pushState({groupId: selectedGroup}, '', `?groupId=${selectedGroup}`);
    fetchTasks(selectedGroup);
}

// Fetch tasks based on the selected groupId
function fetchTasks(groupId) {
    const url = groupId ? `/admin/scheduler/task?groupId=${groupId}` : '/admin/scheduler/task';
    fetch(url, {
        method: 'GET',
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        }
    })
        .then(response => response.json())
        .then(tasks => updateTaskList(tasks))
        .catch(error => {
            console.error('Error fetching tasks:', error);
        });
}

// Update the task list and reinitialize event listeners
function updateTaskList(tasks) {
    const taskTableBody = document.querySelector('.table-content');
    taskTableBody.innerHTML = ''; // Clear any existing content

    tasks.forEach(task => {
        const row = document.createElement('div');
        row.classList.add('table-container', 'expandable');
        row.setAttribute('data-group-id', task.groupId);
        row.setAttribute('data-task-id', task.taskId);

        const rowDiv = document.createElement('div');
        rowDiv.classList.add('table-row');

        // Apply the correct class for paused tasks
        if (task.state === 'PAUSED') {
            rowDiv.classList.add('row-paused');
        } else if (task.state === 'ERROR') {
            rowDiv.classList.add('row-error');
        }

        rowDiv.innerHTML = `
            <span class="expand-trigger">+</span>
            <span class="groupId">
                ${task.description ? `${task.description}<br>` : ''}${task.groupId}
            </span>
            <span class="taskId" data-toggle="tooltip" title="Snowflake ID: ${task.snowflakeData}">${task.taskId}</span>
            <span class="consumer">${task.consumer || ''}</span>
            <span class="nextFireTime">${task.nextFireTime || ''}</span>
            <span class="state">${task.state || ''}</span>
            <span class="outcome" style="color: ${task.outcome === 'ERROR' ? '#EA3939FF' : ''}">
                ${task.outcome || ''}
                ${task.outcome === 'ERROR' ? `<span class="logViewer" data-log="${task.log}" onclick="openLog(this)">👁️</span>` : ''}
            </span>
            <span class="schedule">${task.schedule || ''}</span>
            <span class="runs">${task.runs || ''}</span>
            <span class="failures">${task.failures || ''}</span>
            <div>
                <button class="icon-button"
                        data-task-id="${task.taskId}" data-group-id="${task.groupId}"
                        onclick="deleteTask(this)"
                        title="Delete">
                    <span class="delete-icon">&#x1F5D9;</span>
                </button>
                <button class="icon-button"
                        data-task-id="${task.taskId}" data-group-id="${task.groupId}"
                        onclick="openTaskAudit(this)"
                        title="Audit">
                    <span>&#x1F5D2;&#xFE0F;</span>
                </button>
                <div ${task.state === 'COMPLETE' || task.state === 'NONE' ? 'style="display:none"' : ''}>
                    <button class="icon-button"
                            data-task-id="${task.taskId}" data-group-id="${task.groupId}" data-state="${task.state}"
                            onclick="toggleTaskPauseResume(this)"
                            title="${task.state === 'PAUSED' ? 'Resume' : 'Pause'}">
                        <span class="row-pause-icon" ${task.state === 'PAUSED' ? 'style="display:none"' : ''}>&#9616;&nbsp;&#9612;</span>
                        <span class="row-resume-icon" ${task.state !== 'PAUSED' ? 'style="display:none"' : ''}>&#x23F5;</span>
                    </button>
                </div>
                <button class="icon-button resend-button"
                        ${!task.nextFireTime ? '' : 'style="display:none"'}
                        data-task-id="${task.taskId}" data-group-id="${task.groupId}"
                        onclick="resendTask(this)"
                        title="Resend">
                    <span>&#x21BA;</span>
                </button>
            </div>
        `;

        row.appendChild(rowDiv);

        if (task.dataMap) {
            const subRowDiv = document.createElement('div');
            subRowDiv.classList.add('table-sub-row');
            const dataList = document.createElement('ul');

            Object.entries(task.dataMap).forEach(([key, value]) => {
                const listItem = document.createElement('li');
                listItem.textContent = `${key}: ${value}`;
                dataList.appendChild(listItem);
            });

            subRowDiv.appendChild(dataList);
            row.appendChild(subRowDiv);
        }

        taskTableBody.appendChild(row);
    });

    setupTableExpandCollapse();  // Reinitialize expand/collapse
    reinitializeTooltips(); // Reinitialize tooltips
}

// Reinitialize tooltips for newly added elements (assuming Bootstrap or similar)
function reinitializeTooltips() {
    const tooltipElements = document.querySelectorAll('[data-toggle="tooltip"]');
    tooltipElements.forEach(element => {
        new bootstrap.Tooltip(element);  // Assuming you're using Bootstrap's tooltip
    });
}
