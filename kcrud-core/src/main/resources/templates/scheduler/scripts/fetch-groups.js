/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

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
