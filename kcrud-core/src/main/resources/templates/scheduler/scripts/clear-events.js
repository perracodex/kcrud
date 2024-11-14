/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

function clearEvents() {
    fetch('/admin/events/clear', {
        method: 'POST',
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        }
    })
        .then(response => {
            if (response.ok) {
                document.getElementById('events').innerHTML = ''; // Clear the HTML content.
            } else {
                alert('Failed to clear events.');
            }
        })
        .catch(error => {
            console.error('Error clearing events:', error);
            alert('Error clearing events.');
        });
}
