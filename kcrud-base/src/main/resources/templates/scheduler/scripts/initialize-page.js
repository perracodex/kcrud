/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

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
