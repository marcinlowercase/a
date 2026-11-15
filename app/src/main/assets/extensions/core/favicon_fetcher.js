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

  function parseCssColor(str) {
    if (!str || str === "transparent") return null;
    var m = str.match(/rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)/i);
    if (!m) return null;
    var a = m[4] !== undefined ? parseFloat(m[4]) : 1.0;
    if (a <= 0.01) return null;
    return {
      r: parseInt(m[1], 10),
      g: parseInt(m[2], 10),
      b: parseInt(m[3], 10),
      a: a
    };
  }

  function toHexColor(c) {
    function hex2(n) {
      var s = Math.max(0, Math.min(255, Math.round(n))).toString(16);
      return s.length === 1 ? "0" + s : s;
    }
    return "#" + hex2(c.r) + hex2(c.g) + hex2(c.b);
  }

  function getBgAtPoint(x, y) {
    var layers = [];
    var el = document.elementFromPoint(x, y);
    var guard = 0;
    while (el && guard++ < 30) {
      var parsed = parseCssColor(getComputedStyle(el).backgroundColor);
      if (parsed) {
        layers.push(parsed);
        if (parsed.a >= 0.99) break;
      }
      el = el.parentElement;
    }
    if (layers.length === 0) {
      var candidates = [document.body, document.documentElement];
      for (var i = 0; i < candidates.length; i++) {
        if (!candidates[i]) continue;
        var c = parseCssColor(getComputedStyle(candidates[i]).backgroundColor);
        if (c) {
          layers.push(c);
          if (c.a >= 0.99) break;
        }
      }
    }
    if (layers.length === 0) return null;

    var base = layers[layers.length - 1];
    var r = base.a >= 0.99 ? base.r : (255 * (1 - base.a) + base.r * base.a);
    var g = base.a >= 0.99 ? base.g : (255 * (1 - base.a) + base.g * base.a);
    var b = base.a >= 0.99 ? base.b : (255 * (1 - base.a) + base.b * base.a);
    for (var j = layers.length - 2; j >= 0; j--) {
      var top = layers[j];
      r = r * (1 - top.a) + top.r * top.a;
      g = g * (1 - top.a) + top.g * top.a;
      b = b * (1 - top.a) + top.b * top.a;
    }
    return toHexColor({ r: r, g: g, b: b });
  }

  function getEdgeBgColors() {
    var w = window.innerWidth || document.documentElement.clientWidth || 0;
    var h = window.innerHeight || document.documentElement.clientHeight || 0;
    if (w <= 8 || h <= 8) return { topBg: null, bottomBg: null };

    var topLeft = getBgAtPoint(2, 2);
    var topRight = getBgAtPoint(w - 3, 2);
    var bottomLeft = getBgAtPoint(2, h - 3);
    var bottomRight = getBgAtPoint(w - 3, h - 3);

    var topBg = topLeft && topRight && topLeft === topRight
      ? topLeft
      : (topLeft || topRight || getBgAtPoint(Math.floor(w / 2), 2));

    var bottomBg = bottomLeft && bottomRight && bottomLeft === bottomRight
      ? bottomLeft
      : (bottomLeft || bottomRight || getBgAtPoint(Math.floor(w / 2), h - 3));

    return { topBg: topBg, bottomBg: bottomBg };
  }

  // notify native with resolved background colors on load, style change, or tap
  var visualTimer = null;
  function notifyVisualChange() {
    if (visualTimer) clearTimeout(visualTimer);
    visualTimer = setTimeout(function () {
      visualTimer = null;
      try {
        var colors = getEdgeBgColors();
        browser.runtime.sendNativeMessage("browser", {
          type: "visualChange",
          topBg: colors.topBg,
          bottomBg: colors.bottomBg,
        }).catch(function () {});
      } catch (_) {}
    }, 100);
  }

  notifyVisualChange();

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

  document.addEventListener("click", function () {
    notifyVisualChange();
  }, true);

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
