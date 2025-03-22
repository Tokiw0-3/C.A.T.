// ==UserScript==
// @name         C.A.T.
// @version      2.2
// @description  Custom Aidungeon Themes with library, and optimization
// @author       Tokiw0_3
// @match        https://play.aidungeon.com/*
// @match        https://beta.aidungeon.com/*
// @grant        GM_addStyle
// @grant        GM_getValue
// @grant        GM_setValue
// @grant        GM_registerMenuCommand
// @grant        GM_xmlhttpRequest
// @run-at       document-end
// ==/UserScript==

(function () {
    'use strict';

    const imgurClientId = '88e2ea160284220'; // Your Imgur Client ID
    const imgurAlbumHash = 'e1JN714'; // Imgur Album hash
    const scriptDelay = 6000; // Delay before executing the script

    // Load saved settings or use default values
    let defaultRGBValue = GM_getValue('rgbValue', [255, 0, 0]); // Default text color RGB
    let defaultImageURL = GM_getValue('imageURL', ''); // Default image URL

    // Function to fetch Imgur images
    function fetchImgurImages(albumHash, callback) {
        GM_xmlhttpRequest({
            method: 'GET',
            url: `https://api.imgur.com/3/album/${albumHash}/images`,
            headers: {
                Authorization: `Client-ID ${imgurClientId}`
            },
            onload: function (response) {
                const data = JSON.parse(response.responseText);
                if (data.success) {
                    const images = data.data.map(image => ({
                        original: image.link,
                        preview: image.link.replace(/\.gif$/, 'h.png') // Convert GIFs to static PNGs for preview
                    }));
                    callback(images);
                } else {
                    console.error('Failed to fetch images from Imgur:', data);
                    alert('Failed to fetch images from Imgur.');
                }
            },
            onerror: function (error) {
                console.error('Error fetching images from Imgur:', error);
                alert('Error fetching images from Imgur.');
            }
        });
    }

    function createPanel(themeImages) {
        const panel = document.createElement('div');
        Object.assign(panel.style, {
            position: 'fixed',
            bottom: '0',
            left: '-300px',
            width: '300px',
            padding: '15px',
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            color: 'white',
            transition: 'left 0.3s ease-in-out',
            zIndex: '9998',
            borderTopRightRadius: '8px',
            borderTopLeftRadius: '8px'
        });

        // Create the small arrow button
        const button = document.createElement('div');
        button.innerHTML = '&#9654;'; // Unicode for right arrow
        Object.assign(button.style, {
            position: 'fixed',
            top: '50%',
            left: '10px',
            backgroundColor: 'rgba(0, 0, 0, 0.6)',
            color: 'white',
            border: 'none',
            padding: '5px 8px',
            borderRadius: '0 5px 5px 0',
            cursor: 'pointer',
            zIndex: '9999',
            fontSize: '16px',
            textAlign: 'center',
            lineHeight: '1.2',
            transition: 'left 0.3s'
        });

        function adjustButtonPosition() {
            const panelRect = panel.getBoundingClientRect();
            button.style.top = `${panelRect.top + (panelRect.height / 2) - (button.offsetHeight / 2)}px`;
        }

        // RGB input field
        const rgbInput = document.createElement('input');
        rgbInput.type = 'text';
        rgbInput.placeholder = 'Enter RGB value (e.g., 255, 0, 0)';
        rgbInput.value = defaultRGBValue.join(', ');
        Object.assign(rgbInput.style, {
            width: '100%',
            marginBottom: '10px',
            padding: '8px',
            borderRadius: '5px',
            border: '1px solid #ddd'
        });

        // Image URL input field
        const imageInput = document.createElement('input');
        imageInput.type = 'text';
        imageInput.placeholder = 'Enter image URL';
        imageInput.value = defaultImageURL;
        Object.assign(imageInput.style, {
            width: '100%',
            marginBottom: '10px',
            padding: '8px',
            borderRadius: '5px',
            border: '1px solid #ddd'
        });

        // Theme selection container
        const themeSelection = document.createElement('div');
        themeSelection.style.display = 'flex';
        themeSelection.style.flexWrap = 'wrap';
        themeSelection.style.marginBottom = '10px';

        themeImages.forEach(({ original, preview }) => {
            const img = document.createElement('img');
            img.src = preview; // Use the static PNG preview
            img.style.width = '50px';
            img.style.height = '50px';
            img.style.margin = '5px';
            img.style.cursor = 'pointer';
            img.style.objectFit = 'cover'; // Ensure image fits within the square
            img.style.border = '2px solid white';
            img.style.borderRadius = '5px';

            img.addEventListener('click', () => {
                imageInput.value = original; // Set the input to the full-sized original image
                updateStyles();
            });

            themeSelection.appendChild(img);
        });

        // Apply changes button
        const applyButton = document.createElement('button');
        applyButton.innerHTML = 'Apply Changes';
        Object.assign(applyButton.style, {
            width: '100%',
            padding: '10px',
            backgroundColor: '#444',
            color: 'white',
            border: 'none',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '14px',
            transition: 'background-color 0.3s'
        });

        applyButton.addEventListener('mouseenter', () => applyButton.style.backgroundColor = '#555');
        applyButton.addEventListener('mouseleave', () => applyButton.style.backgroundColor = '#444');

        // Save settings button
        const saveButton = document.createElement('button');
        saveButton.innerHTML = 'Save Settings';
        Object.assign(saveButton.style, {
            width: '100%',
            padding: '10px',
            backgroundColor: '#555',
            color: 'white',
            border: 'none',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '14px',
            marginTop: '10px',
            transition: 'background-color 0.3s'
        });

        saveButton.addEventListener('mouseenter', () => saveButton.style.backgroundColor = '#666');
        saveButton.addEventListener('mouseleave', () => saveButton.style.backgroundColor = '#555');

        document.body.appendChild(button);
        document.body.appendChild(panel);
        panel.appendChild(rgbInput);
        panel.appendChild(imageInput);
        panel.appendChild(themeSelection);
        panel.appendChild(applyButton);
        panel.appendChild(saveButton);

        adjustButtonPosition();
        window.addEventListener('resize', adjustButtonPosition);

        button.addEventListener('click', function () {
            if (panel.style.left === '0px') {
                panel.style.left = '-300px';
                button.innerHTML = '&#9654;'; // Right arrow
                button.style.left = '10px';
            } else {
                panel.style.left = '0';
                button.innerHTML = '&#9664;'; // Left arrow
                button.style.left = '300px';
            }
            adjustButtonPosition();
        });

        function updateStyles() {
            const newRGBValue = rgbInput.value.split(',').map(value => parseInt(value.trim(), 10));
            if (newRGBValue.length !== 3 || newRGBValue.some(value => isNaN(value) || value < 0 || value > 255)) {
                alert('Invalid RGB value. Please enter a comma-separated list of three numbers between 0 and 255.');
                return;
            }

            const newImageURL = imageInput.value || defaultImageURL;
            document.documentElement.style.setProperty('--custom-text-color', `rgb(${newRGBValue.join(',')})`);

            const styleTag = document.getElementById('custom-style-tag') || document.createElement('style');
            styleTag.id = 'custom-style-tag';
            document.head.appendChild(styleTag);
            styleTag.innerHTML = `
                :root {
                    --custom-text-color: rgb(${newRGBValue.join(',')});
                }
                ._col-871727775, [style*="color"] {
                    color: var(--custom-text-color) !important;
                }
            `;

            updateDynamicStyles();
        }

        function updateDynamicStyles() {
            document.querySelectorAll('[style*="background-image"]').forEach(div => {
                const currentBgImage = div.style.backgroundImage;
                if (currentBgImage.includes('theme') || currentBgImage.includes('imgur.com')) {
                    div.style.backgroundImage = `url(${imageInput.value})`;
                }
            });
        }

        // MutationObserver to apply styles on any change in the DOM
        const observer = new MutationObserver(() => {
            updateDynamicStyles();
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true,
            attributes: true
        });

        // Load styles and apply initial setup
        setTimeout(() => {
            updateStyles();
        }, scriptDelay);

        // Event listener for apply button
        applyButton.addEventListener('click', updateStyles);

        // Event listener for save button
        saveButton.addEventListener('click', () => {
            GM_setValue('rgbValue', rgbInput.value.split(',').map(value => parseInt(value.trim(), 10)));
            GM_setValue('imageURL', imageInput.value);
            alert('Settings saved!');
        });

        // Initial load of dynamic styles
        updateDynamicStyles();
    }

    // Fetch theme images and create panel
    fetchImgurImages(imgurAlbumHash, createPanel);
})();
