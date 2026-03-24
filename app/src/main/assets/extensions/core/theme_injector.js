// theme_injector.js
(async function () {
  // 1. CRITICAL: Do not run on Gecko's hidden background frames or blank pages!
  if (
    !window.location.href || window.location.href === "about:blank" ||
    window.location.protocol === "moz-extension:"
  ) {
    return;
  }

//  console.log(
//    "OutSync Protocol: Theme Injection Script Started for: " +
//      window.location.hostname,
//  );

  async function getSettingsFromKotlin() {
    for (let i = 0; i < 20; i++) {
      try {
        let rawResponse = await browser.runtime.sendNativeMessage("browser", {
          type: "getSettings",
        });

        // BYPASS BUG: If Kotlin sends a String, parse it back into an object
        let response = typeof rawResponse === "string"
          ? JSON.parse(rawResponse)
          : rawResponse;

        if (response && response.enabled !== undefined) return response;
      } catch (e) {
        let errorMsg = e ? (e.message || String(e)) : "undefined error";
        if (errorMsg.includes("Actor 'Conduits' destroyed")) {
//          console.warn(
//            "OutSync Protocol: Page navigated away, stopping injection.",
//          );
          return null;
        }
        // Wait 25ms and try again
        await new Promise((res) => setTimeout(res, 25));
      }
    }
    return null; // Give up gracefully
  }
  try {
    let response = await getSettingsFromKotlin();
    if (!response || !response.enabled) return;

    let script = document.createElement("script");
    script.textContent = `
            (function() {
                let scale = 1.0;
                let isDesktop = ${response.isDesktop};

                if (isDesktop) {
                    let screenW = window.screen.width;
                    if (!screenW || screenW >= 980) { screenW = 390; }
                    let viewportW = document.documentElement.clientWidth || window.innerWidth || 980;
                    if (viewportW > screenW) {
                        scale = viewportW / screenW;
                    }
                }

                let scaledRadius = ${response.radius} * scale;
                let scaledPadding = ${response.padding} * scale;
                let scaledLineHeight = ${response.lineHeight} * scale;

                document.documentElement.style.setProperty('--device-corner-radius', scaledRadius + 'px');
                document.documentElement.style.setProperty('--padding', scaledPadding + 'px');
                document.documentElement.style.setProperty('--single-line-height', scaledLineHeight + 'px');
                document.documentElement.style.setProperty('--highlight-color', '${response.color}');

                window.deviceCornerRadius = scaledRadius;
                if (typeof window.render === 'function') window.render(scaledRadius);

//                console.log("OutSync Protocol: SUCCESS! Variables applied. Radius: " + scaledRadius);
            })();
        `;
    document.documentElement.appendChild(script);
    script.remove();
  } catch (e) {
//    console.error("OutSync Protocol Error:", e);
  }
})();
