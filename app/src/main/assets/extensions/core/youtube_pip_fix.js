(function () {
  if (typeof window.wrappedJSObject === "undefined") return;

  const pageWin = window.wrappedJSObject;
  const pageDoc = document.wrappedJSObject || pageWin.document;

  const PIP_CLASS = "__browser_pip_active";
  let pipStyleEl = null;

  let nativePipFlag = false;
  let backgroundPlaybackEnabled = false;
  let pipOverridesApplied = false;
  let visibilityOverridesApplied = false;
  let origOuterWDesc = null;
  let origOuterHDesc = null;

  let isPointerHolding = false;
  let normalPlaybackRate = 1;
  let blockPointer = false;
  let blockTouch = false;
  let lastBlockedStartMs = 0;
  let explicitExitUntilMs = 0;

  let maxArea = (window.outerWidth || 0) * (window.outerHeight || 0);
  let lastNormalOuterW = window.outerWidth || (window.screen && window.screen.availWidth) || 0;
  let lastNormalOuterH = window.outerHeight || (window.screen && window.screen.availHeight) || 0;

  function getActiveVideo() {
    const fsEl = document.fullscreenElement;
    if (fsEl) {
      if (fsEl.tagName === "VIDEO") return fsEl;
      const inside = fsEl.querySelector("video");
      if (inside) return inside;
    }
    return document.querySelector(".html5-main-video") || document.querySelector("video");
  }

  function sendVideoAspectRatio() {
    try {
      const v = getActiveVideo();
      if (v && v.videoWidth > 0 && v.videoHeight > 0) {
        browser.runtime.sendNativeMessage("browser", {
          type: "videoAspectRatio",
          width: v.videoWidth,
          height: v.videoHeight
        }).catch(() => {});
      }
    } catch (_) {}
  }

  function cancelHoldSpeedGesture() {
    try {
      const v = getActiveVideo();
      const targets = [document.fullscreenElement, v, document].filter(Boolean);
      targets.forEach((t) => {
        ["pointercancel", "pointerup", "mouseup"].forEach((evName) => {
          try {
            t.dispatchEvent(new window.PointerEvent(evName, { bubbles: true, cancelable: true }));
          } catch (_) {}
        });
        ["touchcancel", "touchend"].forEach((evName) => {
          try {
            t.dispatchEvent(new window.Event(evName, { bubbles: true, cancelable: true }));
          } catch (_) {}
        });
      });

      if (v && (isPointerHolding || (v.playbackRate === 2 && normalPlaybackRate !== 2))) {
        v.playbackRate = normalPlaybackRate;
      }
      isPointerHolding = false;
    } catch (_) {}
  }

  function ensurePipStyle() {
    if (pipStyleEl || !document.documentElement) return;
    const style = document.createElement("style");
    style.textContent = `
      html.${PIP_CLASS},
      html.${PIP_CLASS} body,
      html.${PIP_CLASS} :fullscreen {
        width: 100% !important;
        height: 100% !important;
        margin: 0 !important;
        padding: 0 !important;
        overflow: hidden !important;
        background: #000 !important;
      }
      html.${PIP_CLASS} :fullscreen,
      html.${PIP_CLASS} #player-container-id,
      html.${PIP_CLASS} .player-container,
      html.${PIP_CLASS} ytm-player,
      html.${PIP_CLASS} #movie_player,
      html.${PIP_CLASS} .html5-video-player,
      html.${PIP_CLASS} .html5-video-container {
        position: fixed !important;
        inset: 0 !important;
        width: 100% !important;
        height: 100% !important;
        max-width: 100% !important;
        max-height: 100% !important;
        margin: 0 !important;
        padding: 0 !important;
        transform: none !important;
      }
      html.${PIP_CLASS} .ytm-fullscreen-watch-next-renderer,
      html.${PIP_CLASS} ytm-fullscreen-watch-next-renderer,
      html.${PIP_CLASS} .fullscreen-recommendations-wrapper,
      html.${PIP_CLASS} .ytp-fullscreen-grid,
      html.${PIP_CLASS} .ytp-ce-element,
      html.${PIP_CLASS} .ytp-cards-teaser {
        display: none !important;
      }
      .${PIP_CLASS} :fullscreen video,
      .${PIP_CLASS} video:fullscreen,
      .${PIP_CLASS} .html5-video-player video {
        position: fixed !important;
        inset: 0 !important;
        top: 0 !important;
        right: 0 !important;
        bottom: 0 !important;
        left: 0 !important;
        width: 100% !important;
        height: 100% !important;
        max-width: 100% !important;
        max-height: 100% !important;
        margin: auto !important;
        transform: none !important;
        object-fit: contain !important;
        object-position: center center !important;
        background: #000 !important;
      }
      .${PIP_CLASS} .ytp-chrome-top,
      .${PIP_CLASS} .ytp-chrome-bottom,
      .${PIP_CLASS} .ytp-gradient-top,
      .${PIP_CLASS} .ytp-gradient-bottom,
      .${PIP_CLASS} .ytp-bezel,
      .${PIP_CLASS} .player-controls-top,
      .${PIP_CLASS} .player-controls-bottom,
      .${PIP_CLASS} .player-controls-middle,
      .${PIP_CLASS} ytm-custom-control,
      .${PIP_CLASS} .ytm-autonav-bar {
        opacity: 0 !important;
        pointer-events: none !important;
      }
    `;
    (document.head || document.documentElement).appendChild(style);
    pipStyleEl = style;
  }

  function isInPip() {
    if (nativePipFlag) return true;
    // ignore shrink heuristic briefly while expanding back from pip
    if (Date.now() < explicitExitUntilMs) return false;
    const w = window.outerWidth || 0;
    const h = window.outerHeight || 0;
    const area = w * h;
    if (area > maxArea) {
      maxArea = area;
      lastNormalOuterW = w;
      lastNormalOuterH = h;
    }
    // detect pip shrink while fullscreen video is active
    if (document.fullscreenElement && maxArea > 0 && area > 0 && area < maxArea * 0.65) {
      return true;
    }
    return false;
  }

  function syncVisibilityOverrides() {
    const shouldSpoof = isInPip() || backgroundPlaybackEnabled;
    if (shouldSpoof && !visibilityOverridesApplied) {
      visibilityOverridesApplied = true;
      try {
        Object.defineProperty(pageDoc, "hidden", {
          configurable: true,
          get: exportFunction(function () {
            return (isInPip() || backgroundPlaybackEnabled) ? false : document.hidden;
          }, pageWin)
        });
        Object.defineProperty(pageDoc, "visibilityState", {
          configurable: true,
          get: exportFunction(function () {
            return (isInPip() || backgroundPlaybackEnabled) ? "visible" : document.visibilityState;
          }, pageWin)
        });
      } catch (_) {}
    } else if (!shouldSpoof && visibilityOverridesApplied) {
      visibilityOverridesApplied = false;
      try { delete pageDoc.hidden; } catch (_) {}
      try { delete pageDoc.visibilityState; } catch (_) {}
    }
  }

  function applyPipOverrides() {
    ensurePipStyle();
    if (document.documentElement) {
      document.documentElement.classList.add(PIP_CLASS);
    }

    cancelHoldSpeedGesture();
    sendVideoAspectRatio();
    syncVisibilityOverrides();

    if (pipOverridesApplied) return;
    pipOverridesApplied = true;

    try {
      origOuterWDesc = Object.getOwnPropertyDescriptor(pageWin, "outerWidth");
      origOuterHDesc = Object.getOwnPropertyDescriptor(pageWin, "outerHeight");

      Object.defineProperty(pageWin, "outerWidth", {
        configurable: true,
        get: exportFunction(function () {
          return isInPip() ? lastNormalOuterW : window.outerWidth;
        }, pageWin)
      });
      Object.defineProperty(pageWin, "outerHeight", {
        configurable: true,
        get: exportFunction(function () {
          return isInPip() ? lastNormalOuterH : window.outerHeight;
        }, pageWin)
      });
    } catch (_) {}
  }

  function removePipOverrides() {
    if (document.documentElement) {
      document.documentElement.classList.remove(PIP_CLASS);
    }

    if (pipOverridesApplied) {
      pipOverridesApplied = false;

      try {
        if (origOuterWDesc) Object.defineProperty(pageWin, "outerWidth", origOuterWDesc);
        else delete pageWin.outerWidth;
      } catch (_) {}

      try {
        if (origOuterHDesc) Object.defineProperty(pageWin, "outerHeight", origOuterHDesc);
        else delete pageWin.outerHeight;
      } catch (_) {}
    }

    syncVisibilityOverrides();
  }

  // fetch initial background playback setting from kotlin
  browser.runtime.sendNativeMessage("browser", { type: "getSettings" })
    .then((raw) => {
      const res = typeof raw === "string" ? JSON.parse(raw) : raw;
      if (res && typeof res.backgroundPlayback === "boolean") {
        backgroundPlaybackEnabled = res.backgroundPlayback;
        syncVisibilityOverrides();
      }
    })
    .catch(() => {});

  window.addEventListener("browser_bg_playback_state", (e) => {
    backgroundPlaybackEnabled = Boolean(e && e.detail);
    syncVisibilityOverrides();
  });

  window.addEventListener("pointerdown", (e) => {
    if (!isInPip()) {
      const now = Date.now();
      if (document.fullscreenElement && (e.clientY >= (window.innerHeight - 20) || (now - lastBlockedStartMs) < 30)) {
        lastBlockedStartMs = now;
        blockPointer = true;
        isPointerHolding = false;
        e.stopImmediatePropagation();
        return;
      }
      blockPointer = false;
      isPointerHolding = true;
    }
  }, true);

  window.addEventListener("touchstart", (e) => {
    if (!isInPip()) {
      const now = Date.now();
      const y = e.touches && e.touches[0] ? e.touches[0].clientY : 0;
      if (document.fullscreenElement && (y >= (window.innerHeight - 20) || (now - lastBlockedStartMs) < 30)) {
        lastBlockedStartMs = now;
        blockTouch = true;
        isPointerHolding = false;
        e.stopImmediatePropagation();
        return;
      }
      blockTouch = false;
      isPointerHolding = true;
    }
  }, true);

  window.addEventListener("pointermove", (e) => {
    if (blockPointer && document.fullscreenElement) {
      e.stopImmediatePropagation();
    }
  }, true);

  window.addEventListener("touchmove", (e) => {
    if (blockTouch && document.fullscreenElement) {
      e.stopImmediatePropagation();
    }
  }, true);

  ["pointerup", "pointercancel"].forEach((ev) => {
    window.addEventListener(ev, (e) => {
      if (blockPointer) {
        e.stopImmediatePropagation();
        blockPointer = false;
      }
      isPointerHolding = false;
    }, true);
  });

  ["touchend", "touchcancel"].forEach((ev) => {
    window.addEventListener(ev, (e) => {
      if (blockTouch) {
        e.stopImmediatePropagation();
        blockTouch = false;
      }
      isPointerHolding = false;
    }, true);
  });
  document.addEventListener("ratechange", (e) => {
    if (!isPointerHolding && !isInPip() && e && e.target && e.target.tagName === "VIDEO" && !e.target.dataset.__browserHold2x) {
      normalPlaybackRate = e.target.playbackRate || 1;
    }
  }, true);

  window.addEventListener("browser_pip_state", (e) => {
    nativePipFlag = Boolean(e && e.detail);
    if (nativePipFlag) {
      explicitExitUntilMs = 0;
      applyPipOverrides();
    } else {
      explicitExitUntilMs = Date.now() + 800;
      isPointerHolding = false;
      blockPointer = false;
      blockTouch = false;
      removePipOverrides();
      const w = window.outerWidth || 0;
      const h = window.outerHeight || 0;
      if (w > 0 && h > 0) {
        lastNormalOuterW = w;
        lastNormalOuterH = h;
      }
      // tell youtube player to re-measure expanded fullscreen dimensions
      setTimeout(() => {
        try {
          window.dispatchEvent(new Event("resize"));
        } catch (_) {}
      }, 150);
    }
  });

  window.addEventListener("resize", () => {
    if (isInPip()) {
      applyPipOverrides();
    } else {
      removePipOverrides();
      const w = window.outerWidth || 0;
      const h = window.outerHeight || 0;
      if (w > 0 && h > 0) {
        lastNormalOuterW = w;
        lastNormalOuterH = h;
        if (w * h > maxArea) maxArea = w * h;
      }
    }
  }, true);

  // block youtube from exiting fullscreen while in pip
  try {
    const docProto = pageWin.Document && pageWin.Document.prototype;
    if (docProto) {
      ["exitFullscreen", "mozCancelFullScreen", "webkitExitFullscreen"].forEach((fnName) => {
        const orig = docProto[fnName];
        if (typeof orig === "function") {
          Object.defineProperty(docProto, fnName, {
            configurable: true,
            writable: true,
            value: exportFunction(function () {
              if (isInPip()) {
                return pageWin.Promise ? pageWin.Promise.resolve() : undefined;
              }
              return orig.call(this);
            }, pageWin)
          });
        }
      });
    }
  } catch (_) {}

  document.addEventListener("fullscreenchange", sendVideoAspectRatio, true);
  document.addEventListener("loadedmetadata", sendVideoAspectRatio, true);

  document.addEventListener("visibilitychange", (e) => {
    if (isInPip() || backgroundPlaybackEnabled) {
      e.stopImmediatePropagation();
    }
  }, true);
})();