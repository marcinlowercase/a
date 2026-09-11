// assets/extensions/core/oo1_bridge.js

(function () {
    if (!window.location.href || window.location.href === "about:blank" || window.location.protocol === "moz-extension:") {
        return;
    }

    // ==========================================
    // 1. GOOGLE DRIVE MODULE
    // ==========================================

    function saveText(filename, content, mimeType) {
        return new window.Promise((resolve, reject) => {
            browser.runtime.sendNativeMessage("browser", {
                type: "driveSaveText",
                filename: filename || "data.json",
                content: content || "",
                mimeType: mimeType || "application/json"
            })
            .then(res => resolve(res))
            .catch(err => reject(err ? err.toString() : "IPC Error"));
        });
    }

    function readText(filename) {
        return new window.Promise((resolve, reject) => {
            browser.runtime.sendNativeMessage("browser", {
                type: "driveReadText",
                filename: filename || "data.json"
            })
            .then(res => resolve(res))
            .catch(err => reject(err ? err.toString() : "IPC Error"));
        });
    }

    function listFiles() {
        return new window.Promise((resolve, reject) => {
            browser.runtime.sendNativeMessage("browser", {
                type: "driveListFiles"
            })
            .then(res => {
                try {
                    let parsed = typeof res === "string" ? JSON.parse(res) : res;
                    if (typeof cloneInto !== "undefined" && window.wrappedJSObject) {
                        parsed = cloneInto(parsed, window.wrappedJSObject);
                    }
                    resolve(parsed);
                } catch (e) {
                    resolve([]);
                }
            })
            .catch(err => reject(err ? err.toString() : "IPC Error"));
        });
    }

    function deleteFile(filename) {
        return new window.Promise((resolve, reject) => {
            browser.runtime.sendNativeMessage("browser", {
                type: "driveDeleteFile",
                filename: filename || ""
            })
            .then(res => resolve(res))
            .catch(err => reject(err ? err.toString() : "IPC Error"));
        });
    }

    // ==========================================
    // 2. HARDWARE HAPTICS MODULE
    // ==========================================

    function vibrate(type) {
        return new window.Promise((resolve) => {
            browser.runtime.sendNativeMessage("browser", {
                type: "hapticVibrate",
                hapticType: type || "click"
            })
            .then(res => resolve(res))
            .catch(() => resolve("FAIL"));
        });
    }

    // ==========================================
    // 3. INJECT INTO WEBPAGE (XRAY SANDBOX BRIDGE)
    // ==========================================

    if (typeof cloneInto !== "undefined" && window.wrappedJSObject) {
        const pageWin = window.wrappedJSObject;
        if (!pageWin.oo1) {
            pageWin.oo1 = cloneInto({}, pageWin);
        }

        // Export Drive
        const driveObj = cloneInto({}, pageWin);
        exportFunction(saveText, driveObj, { defineAs: "saveText" });
        exportFunction(readText, driveObj, { defineAs: "readText" });
        exportFunction(listFiles, driveObj, { defineAs: "listFiles" });
        exportFunction(deleteFile, driveObj, { defineAs: "deleteFile" });
        pageWin.oo1.drive = driveObj;

        // Export Haptic
        const hapticObj = cloneInto({}, pageWin);
        exportFunction(vibrate, hapticObj, { defineAs: "vibrate" });
        pageWin.oo1.haptic = hapticObj;

    } else {
        // Fallback scope
        window.oo1 = window.oo1 || {};
        window.oo1.drive = { saveText, readText, listFiles, deleteFile };
        window.oo1.haptic = { vibrate };
    }
})();