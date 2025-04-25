package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    WebView myWeb;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "UserSettings";
    private static final String KEY_RGB_VALUE = "rgbValue";
    private static final String KEY_IMAGE_URL = "imageURL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb);
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Use a global layout listener to adjust the WebView's height in real time.
        // This ensures that when the keyboard appears (or hides), the WebView
        // resizes to fit the visible area.
        final View rootView = findViewById(R.id.main);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get visible area of the root view
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int visibleHeight = r.height();

                // Update the WebView's height to match the visible height
                ViewGroup.LayoutParams params = myWeb.getLayoutParams();
                params.height = visibleHeight;
                myWeb.setLayoutParams(params);
            }
        });

        // WebView settings
        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings webSettings = myWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Set the user agent to a desktop browser so the site loads its desktop version
        String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        webSettings.setUserAgentString(desktopUserAgent);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(myWeb, true);

        myWeb.clearCache(true);
        myWeb.clearHistory();

        myWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("WebView", "Loading URL: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("WebView", "Page finished loading: " + url);
                injectUserscript();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("WebViewError", "Error: " + error.getDescription());
            }
        });

        // Load the target URL
        myWeb.loadUrl("https://play.aidungeon.com");
    }

    /**
     * Injects the user script into the webpage once it has finished loading.
     */
    private void injectUserscript() {
        String imgurClientId = "88e2ea160284220";
        String imgurAlbumHash = "e1JN714";
        String rgbValue = preferences.getString(KEY_RGB_VALUE, "255, 0, 0");
        String imageURL = preferences.getString(KEY_IMAGE_URL, "");

        // The injected script positions a panel (hidden by default) and a toggle button.
        // It also provides functions to fetch images from Imgur and update the styles.
        String script = "javascript:(function() {"
                + "if(document.getElementById('cat-panel')) { return; }"
                + "const imgurClientId = '" + imgurClientId + "';"
                + "const imgurAlbumHash = '" + imgurAlbumHash + "';"
                + "const scriptDelay = 6000;"
                + "function fetchImgurImages(callback) {"
                + "    fetch('https://api.imgur.com/3/album/' + imgurAlbumHash + '/images', {"
                + "        headers: { Authorization: 'Client-ID ' + imgurClientId }"
                + "    }).then(response => response.json()).then(data => {"
                + "        if (data.success) {"
                + "            callback(data.data.map(img => ({ original: img.link, preview: img.link.replace(/\\.gif$/, 'h.png') })));"
                + "        } else {"
                + "            console.error('Failed to fetch images from Imgur:', data);"
                + "            alert('Failed to fetch images from Imgur.');"
                + "        }"
                + "    }).catch(error => {"
                + "        console.error('Error fetching images:', error);"
                + "        alert('Error fetching images.');"
                + "    });"
                + "}"
                + "const panel = document.createElement('div');"
                + "panel.id = 'cat-panel';"
                + "panel.style.position = 'fixed';"
                + "panel.style.top = '-350px';"
                + "panel.style.left = '50%';"
                + "panel.style.transform = 'translateX(-50%)';"
                + "panel.style.width = '300px';"
                + "panel.style.padding = '15px';"
                + "panel.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';"
                + "panel.style.color = 'white';"
                + "panel.style.transition = 'top 0.3s ease-in-out';"
                + "panel.style.borderTopRightRadius = '8px';"
                + "panel.style.borderTopLeftRadius = '8px';"
                + "panel.style.zIndex = '9998';"
                + "document.body.appendChild(panel);"
                + "const button = document.createElement('div');"
                + "button.id = 'cat-button';"
                + "button.innerHTML = '&#9660;';"
                + "button.style.position = 'fixed';"
                + "button.style.left = '50%';"
                + "button.style.transform = 'translateX(-50%)';"
                + "button.style.top = '2px';"
                + "button.style.backgroundColor = 'rgba(0, 0, 0, 0.6)';"
                + "button.style.color = 'white';"
                + "button.style.padding = '8px 12px';"
                + "button.style.borderRadius = '5px';"
                + "button.style.cursor = 'pointer';"
                + "button.style.zIndex = '9999';"
                + "button.style.fontSize = '20px';"
                + "document.body.appendChild(button);"
                + "button.onclick = function() {"
                + "    if (panel.style.top === '0px') {"
                + "        panel.style.top = '-350px';"
                + "        button.innerHTML = '&#9660;';"
                + "        button.style.top = '2px';"
                + "    } else {"
                + "        panel.style.top = '0px';"
                + "        button.innerHTML = '&#9650;';"
                + "        button.style.top = '350px';"
                + "    }"
                + "};"
                + "const rgbInput = document.createElement('input');"
                + "rgbInput.type = 'text';"
                + "rgbInput.placeholder = 'Enter RGB value (e.g., 255, 0, 0)';"
                + "rgbInput.value = '" + rgbValue + "';"
                + "panel.appendChild(rgbInput);"
                + "const imageInput = document.createElement('input');"
                + "imageInput.type = 'text';"
                + "imageInput.placeholder = 'Enter image URL';"
                + "imageInput.value = '" + imageURL + "';"
                + "panel.appendChild(imageInput);"
                + "const themeSelection = document.createElement('div');"
                + "themeSelection.style.display = 'flex';"
                + "themeSelection.style.flexWrap = 'wrap';"
                + "panel.appendChild(themeSelection);"
                + "fetchImgurImages(function(themeImages) {"
                + "    themeImages.forEach(function(imgData) {"
                + "        const img = document.createElement('img');"
                + "        img.src = imgData.preview;"
                + "        img.style.width = '50px';"
                + "        img.style.height = '50px';"
                + "        img.style.margin = '5px';"
                + "        img.style.cursor = 'pointer';"
                + "        img.onclick = function() {"
                + "            imageInput.value = imgData.original;"
                + "            updateStyles();"
                + "        };"
                + "        themeSelection.appendChild(img);"
                + "    });"
                + "});"
                + "const applyButton = document.createElement('button');"
                + "applyButton.innerHTML = 'Apply Changes';"
                + "panel.appendChild(applyButton);"
                + "applyButton.onclick = function() {"
                + "    const newRGBValue = rgbInput.value.split(',').map(val => parseInt(val.trim(), 10));"
                + "    if (newRGBValue.length !== 3 || newRGBValue.some(isNaN)) {"
                + "        alert('Invalid RGB value.'); return; "
                + "    }"
                + "    document.documentElement.style.setProperty('--custom-text-color', 'rgb(' + newRGBValue.join(',') + ')');"
                + "    document.body.style.backgroundImage = 'url(' + imageInput.value + ')';"
                + "};"
                + "const saveButton = document.createElement('button');"
                + "saveButton.innerHTML = 'Save Settings';"
                + "panel.appendChild(saveButton);"
                + "saveButton.onclick = function() {"
                + "    localStorage.setItem('rgbValue', rgbInput.value);"
                + "    localStorage.setItem('imageURL', imageInput.value);"
                + "    alert('Settings saved!');"
                + "};"
                + "let storedRGB = localStorage.getItem('rgbValue');"
                + "let storedImage = localStorage.getItem('imageURL');"
                + "if (storedRGB) rgbInput.value = storedRGB;"
                + "if (storedImage) imageInput.value = storedImage;"
                + "document.documentElement.style.setProperty('--custom-text-color', 'rgb(' + (storedRGB || '" + rgbValue + "') + ')');"
                + "document.body.style.backgroundImage = 'url(' + (storedImage || '" + imageURL + "') + ')';"
                + "function updateStyles() {"
                + "    const newRGBValue = rgbInput.value.split(',').map(value => parseInt(value.trim(), 10));"
                + "    if (newRGBValue.length !== 3 || newRGBValue.some(value => isNaN(value) || value < 0 || value > 255)) {"
                + "        alert('Invalid RGB value. Please enter three numbers between 0 and 255.');"
                + "        return;"
                + "    }"
                + "    document.documentElement.style.setProperty('--custom-text-color', 'rgb(' + newRGBValue.join(',') + ')');"
                + "    const styleTag = document.getElementById('custom-style-tag') || document.createElement('style');"
                + "    styleTag.id = 'custom-style-tag';"
                + "    document.head.appendChild(styleTag);"
                + "    styleTag.innerHTML = `:root { --custom-text-color: rgb(${newRGBValue.join(',')}); }"
                + "    ._col-871727775, ._col-rgba3522711191032398, [style*=\"color\"] { color: var(--custom-text-color) !important; }`;"
                + "    updateDynamicStyles();"
                + "} "
                + "function updateDynamicStyles() {"
                + "    document.querySelectorAll('[style*=\"background-image\"]').forEach(div => {"
                + "        const currentBgImage = div.style.backgroundImage;"
                + "        if (currentBgImage.includes('theme') || currentBgImage.includes('imgur.com')) {"
                + "            div.style.backgroundImage = `url(${imageInput.value})`;"
                + "        }"
                + "    });"
                + "}"
                + "const observer = new MutationObserver(() => { updateDynamicStyles(); });"
                + "observer.observe(document.body, { childList: true, subtree: true, attributes: true });"
                + "setTimeout(() => { updateStyles(); }, scriptDelay);"
                + "applyButton.addEventListener('click', updateStyles);"
                + "})();";



        myWeb.evaluateJavascript(script, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("https://play.aidungeon.com");
        Log.d("WebView", "Cookies onResume: " + cookies);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("https://play.aidungeon.com");
        Log.d("WebView", "Cookies onPause: " + cookies);
    }

    @Override
    public void onBackPressed() {
        if (myWeb.canGoBack()) {
            myWeb.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
