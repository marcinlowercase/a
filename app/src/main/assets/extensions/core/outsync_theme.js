// assets/extensions/core/outsync_theme.js
(async function () {
  // Prevent injection on blank or internal extension pages
  if (
    !window.location.href ||
    window.location.href === "about:blank" ||
    window.location.protocol === "moz-extension:"
  ) {
    return;
  }

  /**
   * Fetches the host device's live design tokens and layout metrics from Kotlin.
   * Retries up to 20 times (500ms max) to handle early lifecycle timing.
   */
  async function getSettingsFromKotlin() {
    for (let i = 0; i < 20; i++) {
      try {
        const rawResponse = await browser.runtime.sendNativeMessage("browser", {
          type: "getSettings",
        });

        const response = typeof rawResponse === "string"
          ? JSON.parse(rawResponse)
          : rawResponse;

        if (response && response.enabled !== undefined) return response;
      } catch (e) {
        const errorMsg = e ? (e.message || String(e)) : "";
        if (errorMsg.includes("Actor 'Conduits' destroyed")) {
          return null;
        }
        await new Promise((res) => setTimeout(res, 25));
      }
    }
    return null;
  }

  // Signature cache to prevent layout thrashing and redundant style re-calculations
  let lastInjectedSignature = "";

  /**
   * Reads settings from the Kotlin engine and injects live CSS variables into :root.
   * Dynamically adjusts scaling between mobile viewport and desktop viewport (980px).
   */
  async function applyTheme() {
    try {
      const response = await getSettingsFromKotlin();
      if (!response || !response.enabled) return;

      const screenW = response.screenWidth;
      const viewportW = document.documentElement.clientWidth || window.innerWidth || 980;

      // Unique state signature
      const currentSignature = `${screenW}|${viewportW}|${response.radius}|${response.color}`;
      if (lastInjectedSignature === currentSignature) {
        return; // No layout or color change detected
      }
      lastInjectedSignature = currentSignature;

      let scale = 1.0;
      const isDesktop = response.isDesktop;

      // Calculate Desktop Mode scale factor (scales mobile tokens proportionally up to 980px)
      if (isDesktop && viewportW > screenW) {
        scale = viewportW / screenW;
      }

      const scaledRadius = response.radius * scale;
      const scaledPadding = response.padding * scale;
      const scaledLineHeight = response.lineHeight * scale;

      // Inject standardized outsync design tokens
      const rootStyle = document.documentElement.style;
      rootStyle.setProperty("--device-corner-radius", `${scaledRadius}px`);
      rootStyle.setProperty("--padding", `${scaledPadding}px`);
      rootStyle.setProperty("--single-line-height", `${scaledLineHeight}px`);
      rootStyle.setProperty("--highlight-color", response.color);
      rootStyle.setProperty("--on-highlight", response.onHighlight);
      rootStyle.setProperty("--off-highlight", response.offHighlight);
      rootStyle.setProperty("--animation-speed", `${response.animationSpeed}ms`);

      // Backward-compatible JS property & optional consumer render hook
      window.deviceCornerRadius = scaledRadius;
      if (typeof window.render === "function") {
        window.render(scaledRadius);
      }
    } catch (_) {
      // Silently catch to avoid breaking consumer scripts
    }
  }

  // Initial execution on document_start
  await applyTheme();

  // Re-calculate on screen rotation / window resize (debounced 250ms)
  let resizeTimer;
  window.addEventListener("resize", () => {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(async () => {
      await applyTheme();
    }, 250);
  });

  // Event fired natively from Kotlin when system Material You colors or settings update
  window.addEventListener("update_theme_from_kotlin", () => {
    lastInjectedSignature = ""; // Invalidate cache
    setTimeout(async () => {
      await applyTheme();
    }, 50);
  });
})();