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
        .then((res) => {
          if (res === "SUCCESS") {
            resolve(res);
          } else {
            // Rejects so the web app's catch block triggers!
            reject(new Error(res));
          }
        })
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
   // 5. CAMERA & SCANNER MODULE (Native Loopback Stream & ML Kit)
   // ==========================================
   function startCamera(options = {}) {
     return new window.Promise((resolve) => {
       send("cameraStart", { torch: Boolean(options.torch) })
       .then((res) => resolve(res === "SUCCESS"))
       .catch(() => resolve(false));
     });
   }

   function stopCamera() {
     return new window.Promise((resolve) => {
       send("cameraStop")
       .then((res) => resolve(res === "SUCCESS"))
       .catch(() => resolve(false));
     });
   }

  // ==========================================
  // 6. EXACT ALARM MODULE (Android AlarmManager)
  // ==========================================

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

    // 5. Camera & Scanner
    const cameraObj = cloneInto({}, pageWin);
    exportFunction(startCamera, cameraObj, { defineAs: "start" });
    exportFunction(stopCamera, cameraObj, { defineAs: "stop" });
    root.camera = cameraObj;

    // 6. Alarm


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
      camera: {
              start: startCamera,
              stop: stopCamera
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

  var lastSilentVideo = null;

  function isControllableVideo(v) {
    if (!v || v.tagName !== "VIDEO") return false;
    if (document.fullscreenElement) return true;
    if (v.loop && !v.hasAttribute("controls")) return false;
    if (isFinite(v.duration) && v.duration > 0 && v.duration <= 3) return false;
    var rect = v.getBoundingClientRect ? v.getBoundingClientRect() : null;
    if ((v.videoWidth || 0) < 200 && (!rect || rect.width < 200)) return false;
    return true;
  }

  function emitSilentVideoState(v, eventName, forcedPaused) {
    if (!v) return;
    var isPaused = typeof forcedPaused === "boolean" ? forcedPaused : Boolean(v.paused);
    send("silentVideoState", {
      event: eventName || "",
      paused: isPaused,
      position: isFinite(v.currentTime) ? v.currentTime : 0,
      duration: isFinite(v.duration) && v.duration > 0 ? v.duration : -1,
      title: document.title || ""
    }).catch(function () {});
  }

  ["play", "pause", "seeked", "ended"].forEach(function (evName) {
    document.addEventListener(evName, function (e) {
      try {
        var v = e.target;
        if (!isControllableVideo(v)) return;
        lastSilentVideo = v;
        emitSilentVideoState(v, evName, evName === "ended" ? true : undefined);
      } catch (_) {}
    }, true);
  });

  function collectAllMedia(root, out) {
    if (!root || !root.querySelectorAll) return;
    var list = root.querySelectorAll("video, audio");
    for (var i = 0; i < list.length; i++) {
      out.push(list[i]);
    }
    var all = root.querySelectorAll("*");
    for (var j = 0; j < all.length; j++) {
      if (all[j].shadowRoot) {
        collectAllMedia(all[j].shadowRoot, out);
      }
    }
  }

  function findActiveMediaElement() {
    var fsEl = document.fullscreenElement;
    if (fsEl) {
      if (fsEl.tagName === "IFRAME") {
        return null;
      }
      if (fsEl.tagName === "VIDEO" || fsEl.tagName === "AUDIO") return fsEl;
      var fsMedia = [];
      collectAllMedia(fsEl, fsMedia);
      var fsPlaying = fsMedia.find(function (el) { return !el.paused && el.readyState > 0; });
      if (fsPlaying) return fsPlaying;
      if (fsMedia.length > 0) return fsMedia[0];
    }

    if (lastSilentVideo && document.contains(lastSilentVideo) && !lastSilentVideo.paused) {
      return lastSilentVideo;
    }

    var allMedia = [];
    collectAllMedia(document, allMedia);

    var playing = allMedia.find(function (el) {
      return !el.paused && el.readyState > 0;
    });
    if (playing) return playing;

    if (lastSilentVideo && document.contains(lastSilentVideo)) {
      return lastSilentVideo;
    }

    return allMedia.find(function (el) {
      return el.readyState > 0 && isFinite(el.duration) && el.duration > 0;
    }) || null;
  }

  var pendingSeekFallbackTimer = null;

  function forwardCommandToFrames(payload) {
    try {
      var fsEl = document.fullscreenElement;
      if (fsEl && fsEl.tagName === "IFRAME" && fsEl.contentWindow) {
        fsEl.contentWindow.postMessage({ __browserVideoCmd: payload }, "*");
      }
      for (var i = 0; i < window.frames.length; i++) {
        try {
          window.frames[i].postMessage({ __browserVideoCmd: payload }, "*");
        } catch (_) {}
      }
    } catch (_) {}
  }

  function handleBrowserVideoCommand(payload, fromParent) {
    try {
      if (!payload || !payload.cmd) return;

      if (payload.cmd === "query_track_caps" && window === window.top) {
        var nextBtnCap = document.querySelector(
          "button.ytp-next-button, .ytp-next-button, button[aria-label='Next video'], button[aria-label='Next'], [data-tooltip-target-id='ytp-next-button']"
        );
        var hasNextCap = Boolean(nextBtnCap && nextBtnCap.getAttribute("aria-disabled") !== "true");
        if (!hasNextCap && location.hostname.indexOf("youtube.com") !== -1 && location.pathname.indexOf("/watch") === 0) {
          var curVid = new URL(location.href).searchParams.get("v");
          var links = document.querySelectorAll(
            "ytm-video-with-context-renderer a[href*='/watch?v='], ytm-compact-video-renderer a[href*='/watch?v='], ytd-compact-video-renderer a[href*='/watch?v='], a[href*='/watch?v=']"
          );
          for (var j = 0; j < links.length; j++) {
            try {
              var tid = new URL(links[j].href, location.origin).searchParams.get("v");
              if (tid && tid !== curVid) {
                hasNextCap = true;
                break;
              }
            } catch (_) {}
          }
        }
        var prevBtnCap = document.querySelector(
          "button.ytp-prev-button, .ytp-prev-button, button[aria-label='Previous video'], button[aria-label='Previous']"
        );
        var hasPrevCap = Boolean(prevBtnCap && prevBtnCap.getAttribute("aria-disabled") !== "true");
        send("trackCaps", {
          canPrev: hasPrevCap,
          canNext: hasNextCap
        }).catch(function () {});
        return;
      }

      if (payload.cmd === "next_track_fallback" && window === window.top) {
        var nextBtn = document.querySelector(
          "button.ytp-next-button, .ytp-next-button, button[aria-label='Next video'], button[aria-label='Next'], [data-tooltip-target-id='ytp-next-button']"
        );
        if (nextBtn && nextBtn.getAttribute("aria-disabled") !== "true") {
          nextBtn.click();
          return;
        }
        var currentVid = new URL(location.href).searchParams.get("v");
        var watchLinks = document.querySelectorAll(
          "ytm-video-with-context-renderer a[href*='/watch?v='], ytm-compact-video-renderer a[href*='/watch?v='], ytd-compact-video-renderer a[href*='/watch?v='], a[href*='/watch?v=']"
        );
        for (var i = 0; i < watchLinks.length; i++) {
          try {
            var targetUrl = new URL(watchLinks[i].href, location.origin);
            var targetVid = targetUrl.searchParams.get("v");
            if (targetVid && targetVid !== currentVid) {
              watchLinks[i].click();
              return;
            }
          } catch (_) {}
        }
        return;
      }

      if (payload.cmd === "prev_track_fallback" && window === window.top) {
        var prevBtn = document.querySelector(
          "button.ytp-prev-button, .ytp-prev-button, button[aria-label='Previous video'], button[aria-label='Previous']"
        );
        if (prevBtn && prevBtn.getAttribute("aria-disabled") !== "true") {
          prevBtn.click();
          return;
        }
        var activeV = findActiveMediaElement();
        if (activeV && (activeV.currentTime || 0) > 3) {
          activeV.currentTime = 0;
          return;
        }
        if (document.referrer && document.referrer.indexOf(location.host) !== -1 && document.referrer.indexOf("/watch") !== -1) {
          window.history.back();
          return;
        }
        if (activeV) {
          activeV.currentTime = 0;
        }
        return;
      }

      var v = findActiveMediaElement();
      if (!v) {
        // forward into child iframes when active video lives inside an embed
        if (window.frames.length > 0) {
          forwardCommandToFrames(payload);
        }
        if (payload.cmd === "seek_delta_native_fallback" && window === window.top) {
          if (window.frames.length === 0) {
            send("mediaSeekResult", {
              found: false,
              delta: payload.value || 0
            }).catch(function () {});
          } else {
            if (pendingSeekFallbackTimer) clearTimeout(pendingSeekFallbackTimer);
            pendingSeekFallbackTimer = setTimeout(function () {
              pendingSeekFallbackTimer = null;
              send("mediaSeekResult", {
                found: false,
                delta: payload.value || 0
              }).catch(function () {});
            }, 60);
          }
        }
        return;
      }

      lastSilentVideo = v;

      switch (payload.cmd) {
        case "play":
          v.play();
          break;
        case "pause":
          v.pause();
          break;
        case "seek_delta":
        case "seek_delta_native_fallback": {
          var dur = isFinite(v.duration) && v.duration > 0 ? v.duration : 1e9;
          v.currentTime = Math.max(0, Math.min(dur, (v.currentTime || 0) + (payload.value || 0)));
          break;
        }
        case "seek_to": {
          var dur2 = isFinite(v.duration) && v.duration > 0 ? v.duration : 1e9;
          v.currentTime = Math.max(0, Math.min(dur2, payload.value || 0));
          break;
        }
        case "enter_fullscreen": {
          var fsTarget = v.closest(".html5-video-player, #movie_player, ytm-player") || v;
          if (fsTarget.requestFullscreen) {
            fsTarget.requestFullscreen().catch(function () {
              if (v.requestFullscreen) v.requestFullscreen().catch(function () {});
            });
          } else if (v.requestFullscreen) {
            v.requestFullscreen().catch(function () {});
          }
          break;
        }
        case "start_2x": {
          if (v.dataset.__browserHold2x !== "1") {
            v.dataset.__browserPrevRate = String(v.playbackRate || 1);
            v.dataset.__browserHold2x = "1";
          }
          v.playbackRate = 2.0;
          break;
        }
        case "stop_2x": {
          if (v.dataset.__browserHold2x === "1") {
            var prevRate = parseFloat(v.dataset.__browserPrevRate || "1");
            delete v.dataset.__browserHold2x;
            delete v.dataset.__browserPrevRate;
            v.playbackRate = (isFinite(prevRate) && prevRate > 0) ? prevRate : 1.0;
          }
          break;
        }
        case "query":
          break;
      }

      if (payload.cmd === "seek_delta_native_fallback") {
        if (fromParent && window.top) {
          try {
            window.top.postMessage({ __browserSeekAck: true }, "*");
          } catch (_) {}
        }
        send("mediaSeekResult", {
          found: true,
          paused: Boolean(v.paused),
          position: isFinite(v.currentTime) ? v.currentTime : 0,
          duration: isFinite(v.duration) && v.duration > 0 ? v.duration : -1
        }).catch(function () {});
        return;
      }

      var forcedPaused = payload.cmd === "play"
        ? false
        : (payload.cmd === "pause" ? true : undefined);
      emitSilentVideoState(v, payload.cmd, forcedPaused);
    } catch (_) {}
  }

  // controller for live dom media seek and silent video fallback
  window.addEventListener("browser_video_cmd", function (e) {
    try {
      if (window !== window.top && !document.fullscreenElement && !lastSilentVideo) return;
      var payload = typeof e.detail === "string" ? JSON.parse(e.detail) : e.detail;
      handleBrowserVideoCommand(payload, false);
    } catch (_) {}
  });

  window.addEventListener("message", function (e) {
    try {
      if (!e.data) return;
      if (e.data.__browserSeekAck && window === window.top) {
        if (pendingSeekFallbackTimer) {
          clearTimeout(pendingSeekFallbackTimer);
          pendingSeekFallbackTimer = null;
        }
        return;
      }
      if (e.data.__browserVideoCmd) {
        handleBrowserVideoCommand(e.data.__browserVideoCmd, true);
      }
    } catch (_) {}
  });
})();