//oo1_bridge.js

(function () {
    if (!window.location.href || window.location.href === "about:blank" || window.location.protocol === "moz-extension:") {
        return;
    }

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

    // Inject into the page's unprivileged window scope (crossing Xray Vision)
    if (typeof cloneInto !== "undefined" && window.wrappedJSObject) {
        const pageWin = window.wrappedJSObject;
        if (!pageWin.oo1) {
            pageWin.oo1 = cloneInto({}, pageWin);
        }

        const driveObj = cloneInto({}, pageWin);
        exportFunction(saveText, driveObj, { defineAs: "saveText" });
        exportFunction(readText, driveObj, { defineAs: "readText" });

        pageWin.oo1.drive = driveObj;
    } else {
        window.oo1 = window.oo1 || {};
        window.oo1.drive = { saveText, readText };
    }
})();