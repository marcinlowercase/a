// assets/extensions/core/outsync_logic.js
(function () {
  // Prevent execution on blank or extension pages
  if (
    !window.location.href ||
    window.location.href === "about:blank" ||
    window.location.protocol === "moz-extension:"
  ) {
    return;
  }

  /**
   * Helper: Dispatches native messages across the GeckoView WebExtension IPC bridge.
   */
  function send(type, payload = {}) {
    return browser.runtime.sendNativeMessage("browser", Object.assign({ type }, payload));
  }

  // ==========================================
  // 1. PUBLIC FILE STORAGE (Global Downloads/Pictures)
  // ==========================================
  function saveFile(filename, base64Data, mimeType, folder) {
    return new window.Promise((resolve, reject) => {
      send("saveFile", {
        filename: filename || "download",
        base64Data: base64Data || "",
        mimeType: mimeType || "application/octet-stream",
        folder: folder || "DOWNLOADS"
      })
      .then((res) => resolve(res))
      .catch((err) => reject(err ? err.toString() : "IPC Error"));
    });
  }

  // ==========================================
  // 2. GOOGLE DRIVE MODULE (BYOS - Bring Your Own Storage)
  // ==========================================
  function driveSaveText(filename, content, mimeType) {
    return new window.Promise((resolve, reject) => {
      send("driveSaveText", {
        filename: filename || "data.json",
        content: content || "",
        mimeType: mimeType || "application/json"
      })
      .then((res) => resolve(res))
      .catch((err) => reject(err ? err.toString() : "IPC Error"));
    });
  }

  function driveReadText(filename) {
    return new window.Promise((resolve, reject) => {
      send("driveReadText", { filename: filename || "data.json" })
      .then((res) => resolve(res))
      .catch((err) => reject(err ? err.toString() : "IPC Error"));
    });
  }

  function driveListFiles() {
    return new window.Promise((resolve, reject) => {
      send("driveListFiles").then((res) => {
        try {
          let parsed = typeof res === "string" ? JSON.parse(res) : res;
          if (typeof cloneInto !== "undefined" && window.wrappedJSObject) {
            parsed = cloneInto(parsed, window.wrappedJSObject);
          }
          resolve(parsed);
        } catch {
          resolve([]);
        }
      }).catch((err) => reject(err ? err.toString() : "IPC Error"));
    });
  }

  function driveDeleteFile(filename) {
    return new window.Promise((resolve, reject) => {
      send("driveDeleteFile", { filename: filename || "" })
      .then((res) => resolve(res))
      .catch((err) => reject(err ? err.toString() : "IPC Error"));
    });
  }

  // ==========================================
  // 3. HARDWARE HAPTICS MODULE
  // ==========================================
  function vibrate(type) {
    return new window.Promise((resolve) => {
      send("hapticVibrate", { hapticType: type || "click" })
      .then((res) => resolve(res))
      .catch(() => resolve("FAIL"));
    });
  }

  // ==========================================
  // 4. AUDIO NOTIFICATION MODULE
  // ==========================================
  function playAudio(sound) {
    return new window.Promise((resolve) => {
      send("audioPlay", { sound: sound || "beep" })
      .then((res) => resolve(res))
      .catch(() => resolve("FAIL"));
    });
  }

  // ==========================================
  // 5. HARDWARE SCANNER MODULE (Google ML Kit)
  // ==========================================
  function scanBarcode() {
    return new window.Promise((resolve) => {
      send("scannerBarcode")
      .then((res) => resolve(res))
      .catch(() => resolve("ERROR"));
    });
  }

  // ==========================================
  // 6. EXACT ALARM MODULE (Android AlarmManager)
  // ==========================================
  function scheduleAlarm(id, delayMs, title, message) {
    return new window.Promise((resolve) => {
      send("alarmSchedule", {
        id: id || "reminder",
        delayMs: delayMs || 5000,
        title: title || "Alarm",
        message: message || "Time is up!"
      })
      .then((res) => resolve(res))
      .catch(() => resolve("FAIL"));
    });
  }

  function cancelAlarm(id) {
    return new window.Promise((resolve) => {
      send("alarmCancel", { id: id || "reminder" })
      .then((res) => resolve(res))
      .catch(() => resolve("FAIL"));
    });
  }

  // ==========================================
  // 7. HARDWARE FLASHLIGHT MODULE
  // ==========================================
  function toggleFlashlight(enabled) {
    return new window.Promise((resolve) => {
      const payload = {};
      if (typeof enabled === "boolean") payload.enabled = enabled;
      send("flashlightToggle", payload)
      .then((res) => resolve(res))
      .catch(() => resolve("FAIL"));
    });
  }

  function turnOnFlashlight() {
    return toggleFlashlight(true);
  }

  function turnOffFlashlight() {
    return toggleFlashlight(false);
  }

  function getMaxStrength() {
    return new window.Promise((resolve) => {
      send("flashlightGetMaxStrength")
      .then((res) => resolve(parseInt(res, 10) || 1))
      .catch(() => resolve(1));
    });
  }

  function setStrength(level) {
    return new window.Promise((resolve) => {
      send("flashlightSetStrength", {
        level: typeof level === "number" ? Math.round(level) : 1
      })
      .then((res) => resolve(res))
      .catch(() => resolve("FAIL"));
    });
  }

  // ==========================================
  // 8. DEVICE / SYSTEM STATE MODULE
  // ==========================================
  function isScreenOn() {
    return new window.Promise((resolve) => {
      send("deviceGetScreenState")
      .then((res) => resolve(res === "ON"))
      .catch(() => resolve(true));
    });
  }

  function isConnected() {
    return new window.Promise((resolve) => {
      send("deviceGetNetworkState")
      .then((res) => {
        try {
          const parsed = typeof res === "string" ? JSON.parse(res) : res;
          resolve(Boolean(parsed.connected));
        } catch {
          resolve(false);
        }
      })
      .catch(() => resolve(false));
    });
  }

  // =========================================================================
  // XRAY SANDBOX BOUNDARY EXPORTS (Supporting camelCase AND snake_case)
  // =========================================================================

  if (typeof cloneInto !== "undefined" && window.wrappedJSObject) {
    const pageWin = window.wrappedJSObject;
    const root = cloneInto({}, pageWin);

    // 1. Storage
    const storageObj = cloneInto({}, pageWin);
    exportFunction(saveFile, storageObj, { defineAs: "save" });
    root.storage = storageObj;

    // 2. Drive
    const driveObj = cloneInto({}, pageWin);
    exportFunction(driveSaveText, driveObj, { defineAs: "saveText" });
    exportFunction(driveSaveText, driveObj, { defineAs: "save_text" });
    exportFunction(driveReadText, driveObj, { defineAs: "readText" });
    exportFunction(driveReadText, driveObj, { defineAs: "read_text" });
    exportFunction(driveListFiles, driveObj, { defineAs: "listFiles" });
    exportFunction(driveListFiles, driveObj, { defineAs: "list_files" });
    exportFunction(driveDeleteFile, driveObj, { defineAs: "deleteFile" });
    exportFunction(driveDeleteFile, driveObj, { defineAs: "delete_file" });
    root.drive = driveObj;

    // 3. Haptics
    const hapticObj = cloneInto({}, pageWin);
    exportFunction(vibrate, hapticObj, { defineAs: "vibrate" });
    root.haptic = hapticObj;

    // 4. Audio
    const audioObj = cloneInto({}, pageWin);
    exportFunction(playAudio, audioObj, { defineAs: "play" });
    root.audio = audioObj;

    // 5. Scanner
    const scannerObj = cloneInto({}, pageWin);
    exportFunction(scanBarcode, scannerObj, { defineAs: "scan" });
    root.scanner = scannerObj;

    // 6. Alarm
    const alarmObj = cloneInto({}, pageWin);
    exportFunction(scheduleAlarm, alarmObj, { defineAs: "schedule" });
    exportFunction(cancelAlarm, alarmObj, { defineAs: "cancel" });
    root.alarm = alarmObj;

    // 7. Flashlight
    const flashObj = cloneInto({}, pageWin);
    exportFunction(toggleFlashlight, flashObj, { defineAs: "toggle" });
    exportFunction(turnOnFlashlight, flashObj, { defineAs: "on" });
    exportFunction(turnOffFlashlight, flashObj, { defineAs: "off" });
    exportFunction(getMaxStrength, flashObj, { defineAs: "getMaxStrength" });
    exportFunction(getMaxStrength, flashObj, { defineAs: "get_max_strength" });
    exportFunction(setStrength, flashObj, { defineAs: "setStrength" });
    exportFunction(setStrength, flashObj, { defineAs: "set_strength" });
    root.flashlight = flashObj;

    // 8. Device State
    const deviceObj = cloneInto({}, pageWin);
    exportFunction(isScreenOn, deviceObj, { defineAs: "isScreenOn" });
    exportFunction(isScreenOn, deviceObj, { defineAs: "is_screen_on" });
    exportFunction(isConnected, deviceObj, { defineAs: "isConnected" });
    exportFunction(isConnected, deviceObj, { defineAs: "is_connected" });
    root.device = deviceObj;

    // Mount to window
    pageWin.outsync = root;
  } else {
    // Non-Xray fallback scope (Testing / Chromium DevTools)
    const api = {
      storage: {
        save: saveFile
      },
      drive: {
        saveText: driveSaveText, save_text: driveSaveText,
        readText: driveReadText, read_text: driveReadText,
        listFiles: driveListFiles, list_files: driveListFiles,
        deleteFile: driveDeleteFile, delete_file: driveDeleteFile
      },
      haptic: {
        vibrate
      },
      audio: {
        play: playAudio
      },
      scanner: {
        scan: scanBarcode
      },
      alarm: {
        schedule: scheduleAlarm,
        cancel: cancelAlarm
      },
      flashlight: {
        toggle: toggleFlashlight,
        on: turnOnFlashlight,
        off: turnOffFlashlight,
        getMaxStrength, get_max_strength: getMaxStrength,
        setStrength, set_strength: setStrength
      },
      device: {
        isScreenOn, is_screen_on: isScreenOn,
        isConnected, is_connected: isConnected
      }
    };

    window.outsync = api;
  }
})();