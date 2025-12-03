package marcinlowercase.a.core.constant

const val inject_corner_radius = """
    (function() {
        render(___corner-radius___) 
    })();
"""

const val favicon_discovery = """
    (function() {
        var links = document.querySelectorAll("link[rel*='icon']");
        var bestHref = null;
        var maxScore = 0;

        for (var i = 0; i < links.length; i++) {
            var link = links[i];
            var sizes = link.getAttribute('sizes');
            var type = link.getAttribute('type');
            var rel = link.getAttribute('rel');
            var currentScore = 0;

            // 1. SVGs are infinite resolution (Score: 1,000,000)
            if (sizes === 'any' || type === 'image/svg+xml') {
                currentScore = 1000000;
            } 
            // 2. Parse explicit sizes (Score: width * height)
            else if (sizes) {
                try {
                    var dim = parseInt(sizes.split('x')[0]);
                    if (!isNaN(dim)) {
                        currentScore = dim * dim;
                    }
                } catch (e) {}
            }
            
            // 3. Apple Touch Icons (Score: ~180x180 = 32400)
            // Give them a boost if no size was found, as they are usually high res.
            if (currentScore === 0 && rel && rel.indexOf('apple-touch-icon') > -1) {
                currentScore = 32400; 
            }

            // 4. Update best
            if (currentScore > maxScore || (bestHref === null && link.href)) {
                maxScore = currentScore;
                bestHref = link.href;
            }
        }
        
        // Fallback
        if (!bestHref) {
             bestHref = window.location.origin + "/favicon.ico";
        }

        // Ensure we send an absolute URL
        FaviconJavascriptInterface.passFaviconUrl(bestHref); 
    })();
"""