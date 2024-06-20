/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

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
