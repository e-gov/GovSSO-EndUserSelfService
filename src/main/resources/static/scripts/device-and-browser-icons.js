document.addEventListener("DOMContentLoaded", function () {

    const OS_MAP = [
        { match: "windows", icon: "windows.svg" },
        { match: "mac", icon: "apple.svg" },
        { match: "ios", icon: "apple.svg" },
        { match: "linux", icon: "linux.svg" },
        { match: "android", icon: "android.svg" }
    ];

    const BROWSER_MAP = [
        { match: "chrome", icon: "chrome.svg" },
        { match: "edge", icon: "edge.svg" },
        { match: "firefox", icon: "firefox.svg" },
        { match: "opera", icon: "opera.svg" },
        { match: "safari", icon: "safari.svg" }
    ];

    function resolveIcon(value, map) {
        if (!value) {
            return null;
        }

        const normalized = value.toLowerCase();

        const match = map.find(entry =>
            normalized.includes(entry.match)
        );

        return match ? match.icon : null;
    }

    document.querySelectorAll(".js-os-icon").forEach(img => {
        const os = img.dataset.os;
        const icon = resolveIcon(os, OS_MAP);

        if (icon) {
            img.src = "/devices/" + icon;
        } else {
            img.style.display = "none";
        }
    });

    document.querySelectorAll(".js-browser-icon").forEach(img => {
        const browser = img.dataset.browser;
        const icon = resolveIcon(browser, BROWSER_MAP);

        if (icon) {
            img.src = "/browsers/" + icon;
        } else {
            img.style.display = "none";
        }
    });

});
