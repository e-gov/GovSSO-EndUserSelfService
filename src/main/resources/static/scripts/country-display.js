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
        .querySelectorAll('.active-sessions__session-location-text')
        .forEach(locationTextEl => {
            const code = locationTextEl.dataset.countryCode;
            const unknownCountry = locationTextEl.dataset.unknownCountry;
            const flagEl = locationTextEl
                .closest('.active-sessions__session-location')
                ?.querySelector('.active-sessions__session-flag');
            if (!code || code.trim() === "") {
                locationTextEl.textContent = unknownCountry;
                if (flagEl) {
                    flagEl.remove();
                }
                return;
            }

            const countryName = regionNames.of(code);
            if (countryName) {
                locationTextEl.textContent = countryName;
                if (flagEl) {
                    flagEl.src = `/webjars/flag-icons/flags/4x3/${code.toLowerCase()}.svg`;
                    flagEl.alt = "";
                }
            } else {
                locationTextEl.textContent = code;
                if (flagEl) {
                    flagEl.remove();
                }
            }
        });
});
