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

            const countryName = regionNames.of(code);
            if (countryName) {
                locationTextEl.textContent = countryName;
            }

            const flagEl = locationTextEl
                .closest('.active-sessions__session-location')
                ?.querySelector('.active-sessions__session-flag');

            if (flagEl) {
                flagEl.src = `/webjars/flag-icons/flags/4x3/${code.toLowerCase()}.svg`;
                flagEl.alt = "";
            }
        });
});
