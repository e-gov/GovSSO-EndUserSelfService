console.log('country-display.js loaded');
document.addEventListener('DOMContentLoaded', () => {
    const locale = document.documentElement.lang || 'et';

    if (!window.Intl || !Intl.DisplayNames) {
        return;
    }

    const regionNames = new Intl.DisplayNames(
        [locale],
        { type: 'region' }
    );

    document
        .querySelectorAll('[data-country-code]')
        .forEach(locationTextEl => {
            const code = locationTextEl.dataset.countryCode;
            if (!code) {
                return;
            }

            const normalizedCode = code.toLowerCase();

            // 1️⃣ Localized country name
            const countryName = regionNames.of(code);
            if (countryName) {
                locationTextEl.textContent = countryName;
            }

            // 2️⃣ Flag SVG (local asset)
            const flagImg = locationTextEl
                .closest('.active-sessions__session-location')
                ?.querySelector('.active-sessions__session-flag');

            if (flagImg) {
                flagImg.src = `/flags/4x3/${normalizedCode}.svg`;
                flagImg.alt = countryName || code;
            }
        });
});
