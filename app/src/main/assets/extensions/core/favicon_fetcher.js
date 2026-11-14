//favicon_fetcher.js
(function () {
  function findBestFavicon() {
    var links = document.querySelectorAll("link[rel*='icon']");
    var bestHref = null;
    var maxScore = 0;

    for (var i = 0; i < links.length; i++) {
      var link = links[i];
      var sizes = link.getAttribute("sizes");
      var type = link.getAttribute("type");
      var rel = link.getAttribute("rel");
      var currentScore = 0;

      // 1. SVGs are infinite resolution (Score: 1,000,000)
      if (sizes === "any" || type === "image/svg+xml") {
        currentScore = 1000000;
      } // 2. Parse explicit sizes (Score: width * height)
      else if (sizes) {
        try {
          // Extract "192" from "192x192"
          var dim = parseInt(sizes.split("x")[0]);
          if (!isNaN(dim)) {
            currentScore = dim * dim;
          }
        } catch (e) {}
      }

      // 3. Apple Touch Icons (Score: ~180x180 = 32400)
      // Give them a boost if no size was found, as they are usually high res.
      if (currentScore === 0 && rel && rel.indexOf("apple-touch-icon") > -1) {
        currentScore = 32400;
      }

      // 4. Update best
      // If this score is higher, OR if we haven't found anything yet
      if (currentScore > maxScore || (bestHref === null && link.href)) {
        maxScore = currentScore;
        bestHref = link.href; // .href returns the absolute URL automatically
      }
    }

    // Fallback: If no tags found, assume root favicon.ico
    if (!bestHref) {
      bestHref = window.location.origin + "/favicon.ico";
    }

    // --- SEND TO KOTLIN ---
    if (bestHref) {
      // Optional: console.log("GeckoFavicon: Best icon found: " + bestHref);

      // "browser" matches the nativeApp name in your Kotlin setMessageDelegate
      browser.runtime.sendNativeMessage("browser", {
        type: "favicon",
        url: bestHref,
      }).catch((error) => {
        // Ignore errors (happens if the native side isn't ready yet)
      });
    }
  }

  // 1. Run immediately on load
  findBestFavicon();

  // 2. Watch for changes (Dynamic SPAs like YouTube/Gmail change icons at runtime)
  var observer = new MutationObserver(function (mutations) {
    findBestFavicon();
  });

  var head = document.querySelector("head");
  if (head) {
    observer.observe(head, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["href"],
    });
  }

  // notify native when root or body styles change or large layout transitions end
  var visualTimer = null;
  function notifyVisualChange() {
    if (visualTimer) clearTimeout(visualTimer);
    visualTimer = setTimeout(function () {
      visualTimer = null;
      browser.runtime.sendNativeMessage("browser", {
        type: "visualChange",
      }).catch(function () {});
    }, 100);
  }

  var rootObserver = new MutationObserver(function () {
    notifyVisualChange();
  });

  if (document.documentElement) {
    rootObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ["style", "class", "data-theme", "theme", "bgcolor"],
    });
  }
  if (document.body) {
    rootObserver.observe(document.body, {
      attributes: true,
      attributeFilter: ["style", "class", "data-theme", "theme", "bgcolor"],
    });
  }

  document.addEventListener("transitionend", function (e) {
    try {
      var target = e.target;
      if (target && target.getBoundingClientRect) {
        var rect = target.getBoundingClientRect();
        if (rect.width >= window.innerWidth * 0.5) {
          notifyVisualChange();
        }
      }
    } catch (_) {}
  }, true);
})();
