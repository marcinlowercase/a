// theme_injector.js
(async function () {
  if (
    !window.location.href || window.location.href === "about:blank" ||
    window.location.protocol === "moz-extension:"
  ) {
    return;
  }

  async function getSettingsFromKotlin() {
    for (let i = 0; i < 20; i++) {
      try {
        let rawResponse = await browser.runtime.sendNativeMessage("browser", {
          type: "getSettings",
        });

        let response = typeof rawResponse === "string"
          ? JSON.parse(rawResponse)
          : rawResponse;

        if (response && response.enabled !== undefined) return response;
      } catch (e) {
        let errorMsg = e ? (e.message || String(e)) : "undefined error";
        if (errorMsg.includes("Actor 'Conduits' destroyed")) {
          return null;
        }
        await new Promise((res) => setTimeout(res, 25));
      }
    }
    return null;
  }

  // Keep track of what we applied to prevent CSS spam
  let lastInjectedSignature = "";

  async function applyTheme() {
    try {
      let response = await getSettingsFromKotlin();
      if (!response || !response.enabled) return;

      // In Desktop Mode, viewportW is ALWAYS ~980.
      // But screenW is now dynamically ~390 (Portrait) or ~850 (Landscape) from Kotlin!
      let screenW = response.screenWidth;
      let viewportW = document.documentElement.clientWidth || window.innerWidth || 980;

      // Create a signature of the current state
      let currentSignature = screenW + "|" + viewportW + "|" + response.radius;

      // SPAM PREVENTION: If the values are identical to last time, do nothing!
      if (lastInjectedSignature === currentSignature) {
          return;
      }
      lastInjectedSignature = currentSignature;

      let scale = 1.0;
      let isDesktop = response.isDesktop;

      // MATH EXPLANATION:
      // Portrait: scale = 980 / 390 = 2.51
      // Landscape: scale = 980 / 850 = 1.15
      if (isDesktop && viewportW > screenW) {
          scale = viewportW / screenW;
      }

      let scaledRadius = response.radius * scale;
      let scaledPadding = response.padding * scale;
      let scaledLineHeight = response.lineHeight * scale;

      document.documentElement.style.setProperty('--device-corner-radius', scaledRadius + 'px');
      document.documentElement.style.setProperty('--padding', scaledPadding + 'px');
      document.documentElement.style.setProperty('--single-line-height', scaledLineHeight + 'px');
      document.documentElement.style.setProperty('--highlight-color', response.color);
      document.documentElement.style.setProperty('--on-highlight', response.onHighlight);
      document.documentElement.style.setProperty('--off-highlight', response.offHighlight);
      document.documentElement.style.setProperty('--animation-speed', response.animationSpeed);

      window.deviceCornerRadius = scaledRadius;
      if (typeof window.render === 'function') window.render(scaledRadius);

    } catch (e) {
    }
  }

  // Run on page load
  await applyTheme();

  let resizeTimer;
  window.addEventListener('resize', () => {
    // We don't check window.innerWidth here anymore, because it's locked to 980.
    // We just wait 250ms for the screen rotation to settle, then run applyTheme.
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(async () => {
      await applyTheme();
    }, 250);
  });

    window.addEventListener('update_theme_from_kotlin', async () => {
      // 1. Clear the signature so the SPAM PREVENTION allows the update
      lastInjectedSignature = "";

      // 2. Wait 50ms to ensure Jetpack Compose has finished updating
      // the dynamic Material You colors in the background before we fetch
      setTimeout(async () => {
        await applyTheme();
      }, 50);
    });

})();