// noinspection JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

/**
 * Interactive demo.
 * It fetches data from the server as the user scrolls and updates the table content dynamically.
 * It also provides functionality to dump records in JSON format.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Constants for pagination and dynamic loading.
    const newPageOverflowThreshold = 100;
    const container = document.querySelector('.table-content');
    const urlParams = new URLSearchParams(window.location.search);
    const sortParams = urlParams.getAll('sort');
    let lastFocusedElement; // Hold the last focused element.

    // State variables for pagination control.
    let timerInterval; // Holds the interval ID for the timer.
    let totalPages; // Total number of pages available based on data.
    let totalLoadedRecords = 0; // Counter for loaded records.
    let currentPage = parseInt(urlParams.get('page'), 10);  // Get current page index. Default is 0.
    let pageSize = parseInt(urlParams.get('size'), 10) || 24; // Get page size. Default is 24.
    currentPage = isNaN(currentPage) ? 0 : currentPage; // Validate current page index.

    // Set up event listeners for the buttons.
    document.getElementById('createRecordsButton').addEventListener('click', createRecords);
    document.getElementById('jsonDumpButton').addEventListener('click', jsonDump);
    document.getElementById('deleteRecordsButton').addEventListener('click', deleteRecords);
    document.addEventListener('click', closeInfoCard);

    // Initialize the UI components and fetch initial data.
    initializePageSize();
    hookScroll();
    hookNewRecordsInput();
    setupTableInteractivity();

    /**
     * Checks if the scroll is near the bottom of the container to trigger loading more data.
     */
    function hookScroll() {
        if (pageSize && currentPage >= 0) {
            fetchMoreData();
            container.addEventListener('scroll', () => {
                closeInfoCard(); // Close any open info-card when scrolling.

                if (nearBottomOfContainer() && shouldFetchMoreData()) {
                    currentPage++;
                    fetchMoreData();
                }
            });
        }
    }

    /**
     * Hooks the input for the number of records to create.
     */
    function hookNewRecordsInput() {
        const numberRecordsInput = document.getElementById('numberRecords');
        numberRecordsInput.addEventListener('change', validateRange);
        numberRecordsInput.addEventListener('keypress', event => {
            if (event.key === 'Enter') {
                event.preventDefault();
                validateAndCreateRecords();
            }
        });

        function validateRange() {
            const value = parseInt(numberRecordsInput.value, 10);
            if (value < 1) {
                numberRecordsInput.value = 1;
            } else if (value > 100000) {
                numberRecordsInput.value = 100000;
            }
        }

        function validateAndCreateRecords() {
            validateRange();
            createRecords();
        }
    }

    /**
     * Initializes and handles the page size selection.
     */
    function initializePageSize() {
        const pageSizeFromUrl = parseInt(urlParams.get('size'), 10);
        const pageSizeInput = document.getElementById('pageSize');

        // Set the input value to URL parameter, or default if not specified or invalid.
        pageSizeInput.value = isNaN(pageSizeFromUrl) || pageSizeFromUrl <= 0 ? 24 : pageSizeFromUrl; // Default is 24.
        pageSizeInput.addEventListener('change', () => {
            changePageSize(pageSizeInput.value);
        });
    }

    /**
     * Updates the page size and reloads the page.
     * @param {number} newSize - The new size for the pages.
     */
    function changePageSize(newSize) {
        const validNewSize = (newSize > 0) ? newSize : 24; // Validate new page size, default is 24.
        const newUrlParams = new URLSearchParams(window.location.search);
        newUrlParams.set('page', '0'); // Reset to page 0 when size changes.
        newUrlParams.set('size', String(validNewSize)); // Set new page size.
        window.location.search = newUrlParams.toString(); // Redirect to the updated URL.
    }

    /**
     * Determines whether more data should be fetched based on current page and total pages.
     */
    function shouldFetchMoreData() {
        return !totalPages || currentPage < totalPages;
    }

    /**
     * Checks if the scroll is near the bottom of the container to trigger loading more data.
     */
    function nearBottomOfContainer() {
        return container.scrollHeight - container.scrollTop <= container.clientHeight + newPageOverflowThreshold;
    }

    /**
     * Fetches more data from the server and updates the page content.
     */
    function fetchMoreData() {
        if (!shouldFetchMoreData()) return;

        const params = new URLSearchParams({
            page: currentPage.toString(),
            size: pageSize.toString()
        });
        sortParams.forEach(sort => params.append('sort', sort));

        fetch(`/demo/json?${params}`)
            .then(response => response.json())
            .then(data => {
                totalPages = data.totalPages;
                totalLoadedRecords += data.content.length;
                closeInfoCard();
                appendRecords(data);
                updatePageDetails(data);
                updateTotalElements(data.totalElements);
                setupTableInteractivity();

                if (container.scrollHeight <= container.clientHeight && currentPage < totalPages) {
                    currentPage++;
                    fetchMoreData();
                }
            })
            .catch(error => console.error('Error:', error));
    }

    /**
     * Appends records to the container.
     * @param {Object} page - The page data containing record information.
     */
    function appendRecords(page) {
        // Create a document fragment to minimize re-flows and repaints.
        const fragment = document.createDocumentFragment();
        let startIndex = page.pageIndex * page.elementsPerPage;

        page.content.forEach((employment) => {
            const employee = employment.employee;
            const dob = new Date(employee.dob).toISOString().slice(0, 10);
            const email = employee.contact ? employee.contact.email : 'N/A';
            const phone = employee.contact ? employee.contact.phone : 'N/A';

            const row = document.createElement('div');
            row.className = 'table-row';

            // Store the entire employment object as a JSON string in a data attribute.
            row.setAttribute('data-employment', JSON.stringify(employment));

            // Set the inner HTML of the row with employee data
            row.innerHTML =
                `<div>${++startIndex}</div>` +
                `<div>${employee.fullName}</div>` +
                `<div>${employee.age}</div>` +
                `<div>${dob}</div>` +
                `<div>${capitalize(employee.honorific)}</div>` +
                `<div>${capitalize(employee.maritalStatus)}</div>` +
                `<div>${email}</div>` +
                `<div>${phone}</div>` +
                `<div>${capitalize(employment.status)}</div>` +
                `<div>${capitalize(employment.workModality).replace("_", " ")}</div>`;

            // Create the avatar element.
            const avatarDiv = document.createElement('div');
            avatarDiv.className = 'avatar';
            const initialsSpan = document.createElement('span');
            initialsSpan.textContent = employee.firstName.charAt(0) + employee.lastName.charAt(0);
            avatarDiv.appendChild(initialsSpan);
            row.insertBefore(avatarDiv, row.firstChild);

            // Append the row to the fragment instead of the container.
            fragment.appendChild(row);
        });

        // Append the entire fragment to the container at once.
        container.appendChild(fragment);
    }

    /**
     * Updates the page detail information.
     * @param {Object} page - The page data containing details.
     */
    function updatePageDetails(page) {
        const detailsDiv = document.querySelector('.page-details');
        const loadedPages = page.totalElements === 0 ? 0 : (page.pageIndex + 1); // Adjust because of zero-based indexing.

        detailsDiv.innerHTML =
            `Loaded Pages: ${loadedPages}, ` +
            `Loaded Records: ${totalLoadedRecords.toLocaleString()}, ` +
            `Records per Page: ${page.elementsPerPage.toLocaleString()}, ` +
            `Total Pages: ${page.totalPages.toLocaleString()}, ` +
            `Total Records: ${page.totalElements.toLocaleString()}`;
    }

    /**
     * Capitalizes the first letter of a string and lower-cases the rest.
     */
    function capitalize(text) {
        return text ? text.charAt(0).toUpperCase() + text.slice(1).toLowerCase() : '';
    }

    /**
     * Calls the server to create a set of records.
     */
    function createRecords() {
        setButtonsState(false);

        const numberRecords = document.getElementById('numberRecords').value;

        fetch(`/demo?count=${numberRecords}`, {method: 'POST'})
            .then(response => {
                if (response.ok) {
                    console.log('Records created successfully');
                    // Call refreshData to reload the content with the updated information.
                    refreshData();
                } else {
                    console.error('Failed to create records');
                    alert("Failed to create records. Must be between 1 and 100,000.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("Error in creating records.");
            })
            .finally(() => {
                setButtonsState(true);
            });
    }

    /**
     * Calls the server to fetch records and navigate to the JSON page.
     */
    function jsonDump() {
        setButtonsState(false);

        const jsonPageURL = '/demo/json';
        fetch(jsonPageURL, {method: 'POST'})
            .then(() => {
                // Navigate to the JSON page
                window.location.href = jsonPageURL;
            })
            .catch(error => console.error('Error:', error))
            .finally(() => setButtonsState(true));
    }

    /**
     * Calls the server to delete all records.
     */
    function deleteRecords() {
        setButtonsState(false);

        fetch(`/demo`, {method: 'DELETE'})
            .then(response => {
                if (response.ok) {
                    console.log('Records deleted successfully');
                    refreshData();
                } else {
                    console.error('Failed to delete records');
                    alert("Failed to delete records.");
                }
            })
            .catch(error => console.error('Error:', error))
            .finally(() => setButtonsState(true));
    }

    /**
     * Refreshes the data by clearing the existing content and fetching new data.
     */
    function refreshData() {
        totalPages = null;
        currentPage = 0;
        totalLoadedRecords = 0;

        // Clear existing records list.
        const container = document.querySelector('.table-content');
        container.innerHTML = '';

        // Fetch new set of data based on the current state.
        fetchMoreData();
    }

    /**
     * Updates the total elements count in the UI.
     * @param totalElements
     */
    function updateTotalElements(totalElements) {
        const totalElementsH3 = document.getElementById('totalElements');
        if (totalElementsH3) {
            totalElementsH3.textContent = totalElements.toLocaleString();
        }
    }

    /**
     * Starts the timer and displays the elapsed time whenever records are created or deleted.
     */
    function startTimer() {
        // Clear any existing timer interval before starting a new one.
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }

        let timerDisplay = document.querySelector('.timer');
        if (!timerDisplay) {
            timerDisplay = document.createElement('div'); // Create the timer display element if not found.
            timerDisplay.classList.add('timer');
            const timerPlaceholder = document.querySelector('.timer-placeholder');
            if (timerPlaceholder) {
                timerPlaceholder.replaceWith(timerDisplay);
            } else {
                // If there's no placeholder, append or prepend the timerDisplay to a suitable container.
                document.body.appendChild(timerDisplay);
            }
        }

        timerDisplay.textContent = '00:00.000'; // Initialize with 0 minutes, 0 seconds, and 0 milliseconds.

        const startTime = Date.now(); // Record the start time.
        timerInterval = setInterval(() => {
            const elapsedTime = Date.now() - startTime; // Calculate elapsed time.
            const minutes = Math.floor(elapsedTime / 60000);
            const seconds = Math.floor((elapsedTime % 60000) / 1000);
            const milliseconds = elapsedTime % 1000;
            timerDisplay.textContent = `${pad(minutes)}:${pad(seconds)}.${padMilliseconds(milliseconds)}`; // Update the timer text.
        }, 1); // Update every millisecond.
    }

    /**
     * Sets the state of the buttons and inputs to enabled or disabled.
     */
    function setButtonsState(enabled) {
        closeInfoCard();

        // Before changing the state, store the currently focused element if it's a button or input.
        if (!enabled && ['BUTTON', 'INPUT'].includes(document.activeElement?.tagName)) {
            lastFocusedElement = document.activeElement;
        }

        // Disable or enable all buttons.
        document.querySelectorAll('button, input').forEach(button => {
            button.disabled = !enabled;
        });

        // After re-enabling, restore focus to the last focused element if applicable.
        if (enabled) {
            if (lastFocusedElement) {
                lastFocusedElement.focus();
                lastFocusedElement = null; // Clear the reference to ensure it's only used once.
            }

            // Clear the timer when buttons are enabled.
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
        } else {
            // Start the timer only when buttons are disabled.
            startTimer();
        }
    }

    /**
     * Helper function to pad numbers with leading zeros.
     */
    function pad(number) {
        return number.toString().padStart(2, '0');
    }

    /**
     * Helper function to pad numbers with leading zeros for milliseconds.
     */
    function padMilliseconds(number) {
        return number.toString().padStart(3, '0');
    }

    /**
     * Sets up the interactivity of the table rows.
     * It adds an event listener to display the info card when a row is clicked.
     */
    function setupTableInteractivity() {
        document.querySelectorAll('.table-row').forEach(row => {
            row.removeEventListener('click', showInfoCard); // Clear existing event listeners to avoid duplicates.
            row.addEventListener('click', showInfoCard); // Add event listener to show info card.
        });
    }

    /**
     * Shows an info card with the JSON data of the employment record.
     * @param event - The click event on the table row.
     */
    function showInfoCard(event) {
        // Close any existing info card.
        closeInfoCard();

        // Prevent immediate closing due to the document-wide event listener.
        event.stopPropagation();

        // Create and configure the new info card element.
        const infoCard = document.createElement('div');
        infoCard.className = 'info-card';
        const employmentData = JSON.parse(event.currentTarget.getAttribute('data-employment'));
        infoCard.innerHTML = `<pre>${JSON.stringify(employmentData, null, 2)}</pre>`;
        document.body.appendChild(infoCard);

        // Make the info card visible and appropriately positioned.
        infoCard.style.display = 'block';
        infoCard.style.left = `${event.pageX + 320}px`;
        infoCard.style.top = `${event.pageY}px`;

        // Reposition if offscreen.
        const rect = infoCard.getBoundingClientRect();
        if (rect.right > window.innerWidth) {
            infoCard.style.left = `${window.innerWidth - rect.width - 20}px`; // Adjust left position
        }
        if (rect.bottom > window.innerHeight) {
            infoCard.style.top = `${window.innerHeight - rect.height - 20}px`; // Adjust top position
        }

        // Add an event listener to the info card to check for text selection before closing.
        infoCard.addEventListener('click', function (event) {
            const selection = window.getSelection();
            // Close the card only if there is no text selected.
            if (!selection.toString().length) {
                closeInfoCard();
            }
            // Prevent the click inside the card from propagating to the document.
            event.stopPropagation();
        });
    }

    /**
     * Closes the info card if it's open.
     */
    function closeInfoCard() {
        const existingInfoCard = document.querySelector('.info-card');
        if (existingInfoCard) {
            existingInfoCard.remove();
        }
    }
});
