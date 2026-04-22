// assets/extensions/core/save_file.js

function triggerNativeSave(filename, base64Data, mimeType, folder) {
    // We return a Promise to the page. GeckoView will automatically safely
    // clone this Promise across the Xray Vision sandbox boundary.
    return new window.Promise((resolve, reject) => {
        const payload = {
            type: "saveFile",
            filename: filename || "download",
            base64Data: base64Data || "",
            mimeType: mimeType || "image/jpeg",
            folder: folder || "PICTURES"
        };

        // Send the message natively to Kotlin
        browser.runtime.sendNativeMessage("browser", payload)
            .then(response => resolve(response))
            .catch(err => reject(err ? err.toString() : "Unknown IPC Error"));
    });
}

// THE FIX: Use window.wrappedJSObject to inject it directly into the website's scope
if (typeof cloneInto !== "undefined" && window.wrappedJSObject) {
    exportFunction(triggerNativeSave, window.wrappedJSObject, { defineAs: 'saveFileToAndroid' });
} else {
    // Fallback just in case
    window.saveFileToAndroid = triggerNativeSave;
}