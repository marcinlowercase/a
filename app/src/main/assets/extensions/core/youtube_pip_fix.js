(function () {
  if (typeof window.wrappedJSObject === "undefined") return;

  const pageWin = window.wrappedJSObject;
  const pageDoc = document.wrappedJSObject || pageWin.document;

  const PIP_CLASS = "__browser_pip_active";
  let pipStyleEl = null;

  let nativePipFlag = false;
  let pipOverridesApplied = false;
  let origOuterWDesc = null;
  let origOuterHDesc = null;

  let isPointerHolding = false;
  let normalPlaybackRate = 1;

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

  function applyPipOverrides() {
    ensurePipStyle();
    if (document.documentElement) {
      document.documentElement.classList.add(PIP_CLASS);
    }

    cancelHoldSpeedGesture();
    sendVideoAspectRatio();

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

    try {
      Object.defineProperty(pageDoc, "hidden", {
        configurable: true,
        get: exportFunction(function () {
          return isInPip() ? false : document.hidden;
        }, pageWin)
      });
      Object.defineProperty(pageDoc, "visibilityState", {
        configurable: true,
        get: exportFunction(function () {
          return isInPip() ? "visible" : document.visibilityState;
        }, pageWin)
      });
    } catch (_) {}
  }

  function removePipOverrides() {
    if (document.documentElement) {
      document.documentElement.classList.remove(PIP_CLASS);
    }

    if (!pipOverridesApplied) return;
    pipOverridesApplied = false;

    try {
      if (origOuterWDesc) Object.defineProperty(pageWin, "outerWidth", origOuterWDesc);
      else delete pageWin.outerWidth;
    } catch (_) {}

    try {
      if (origOuterHDesc) Object.defineProperty(pageWin, "outerHeight", origOuterHDesc);
      else delete pageWin.outerHeight;
    } catch (_) {}

    try { delete pageDoc.hidden; } catch (_) {}
    try { delete pageDoc.visibilityState; } catch (_) {}
  }

  document.addEventListener("pointerdown", () => {
    if (!isInPip()) isPointerHolding = true;
  }, true);
  document.addEventListener("touchstart", () => {
    if (!isInPip()) isPointerHolding = true;
  }, true);
  ["pointerup", "pointercancel", "touchend", "touchcancel"].forEach((ev) => {
    document.addEventListener(ev, () => {
      isPointerHolding = false;
    }, true);
  });
  document.addEventListener("ratechange", (e) => {
    if (!isPointerHolding && !isInPip() && e && e.target && e.target.tagName === "VIDEO") {
      normalPlaybackRate = e.target.playbackRate || 1;
    }
  }, true);

  window.addEventListener("browser_pip_state", (e) => {
    nativePipFlag = Boolean(e && e.detail);
    if (nativePipFlag) {
      applyPipOverrides();
    } else {
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
    if (isInPip()) {
      e.stopImmediatePropagation();
    }
  }, true);
})();