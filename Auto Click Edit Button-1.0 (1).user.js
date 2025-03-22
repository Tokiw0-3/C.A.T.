// ==UserScript==
// @name         Auto Click Edit Button
// @namespace    http://tampermonkey.net/
// @version      1.0
// @description  Automatically clicks the Edit button when the menu appears
// @author       Tokiw0_3
// @match        play.aidungeon.com/adventure/*
// @match        beta.aidungeon.com/adventure/*
// @grant        none
// ==/UserScript==

(function () {
    'use strict';

    // Function to find and click the "Edit" button
    function clickEditButton() {
        // Find all elements with role="button" and inner text "Edit"
        const editButton = Array.from(document.querySelectorAll('[role="button"]'))
            .find(button => {
                const span = button.querySelector('.is_ButtonText');
                return span && span.innerText.trim().toLowerCase() === 'edit';
            });

        if (editButton) {
            editButton.click();
            console.log("Edit button clicked.");
        }
    }

    // Create a MutationObserver to detect changes in the DOM
    const observer = new MutationObserver((mutations) => {
        for (const mutation of mutations) {
            if (mutation.addedNodes.length) {
                // Check if the pop-up menu has appeared
                const popUpMenu = document.querySelector('div.css-175oi2r');
                if (popUpMenu) {
                    clickEditButton();
                }
            }
        }
    });

    // Start observing the body for changes
    observer.observe(document.body, { childList: true, subtree: true });
})();