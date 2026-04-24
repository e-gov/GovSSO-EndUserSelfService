'use strict';

const NOTICE_PARAM = 'sessionNotice';
const NOTICE_SINGLE_SUCCESS = 'session-ended';
const NOTICE_ALL_SUCCESS = 'all-sessions-ended';

const FLAG_ICONS_VERSION = '7.5.0';  // Keep in sync with pom.xml flag-icons.version

const endAllSessions = function () {
	endSessionsRequest('/api/sessions', NOTICE_ALL_SUCCESS, 'endAllSessionsError');
};

const endSession = function (sessionId) {
	endSessionsRequest(
		'/api/sessions/' + encodeURIComponent(sessionId),
		NOTICE_SINGLE_SUCCESS,
		'endSessionError'
	);
};

const endSessionsRequest = async function (url, successNotice, errorMessage) {
	try {
		const response = await fetch(url, {
			method: 'DELETE',
			headers: createCsrfHeader()
		});

		if (!response.ok) {
			showError(errorMessage);
			return;
		}

		reloadWithNotice(successNotice);
	} catch (error) {
		showError(errorMessage, error);
	}
};

const getActiveSessionsElement = function () {
	return document.querySelector('.active-sessions');
};

const getActiveSessionsConfig = function () {
	const configEl = document.getElementById('active-sessions-config');
	if (!configEl) {
		return {};
	}

	return JSON.parse(configEl.textContent);
};

const getActiveSessionsMessage = function (key) {
	return getActiveSessionsConfig()[key] || null;
};

const reloadWithNotice = function (notice) {
	const url = new URL(window.location.href);
	url.searchParams.set(NOTICE_PARAM, notice);
	window.location.assign(url.toString());
};

const showSuccessNoticeFromUrl = function () {
	const url = new URL(window.location.href);
	const notice = url.searchParams.get(NOTICE_PARAM);

	if (!notice) {
		return;
	}

	const messageKeyByNotice = {
		[NOTICE_SINGLE_SUCCESS]: 'endSessionSuccess',
		[NOTICE_ALL_SUCCESS]: 'endAllSessionsSuccess'
	};

	const messageKey = messageKeyByNotice[notice];

	if (!messageKey) {
		return;
	}

	showNotice(messageKey, 'success');

	url.searchParams.delete(NOTICE_PARAM);
	window.history.replaceState({}, document.title, url.toString());
};

const showError = function (messageKey, error) {
	if (error) {
		console.error('Error', error);
	}

	showNotice(messageKey, 'error');
};

const showNotice = function (messageKey, type) {
	const message = getActiveSessionsMessage(messageKey);

	if (!message) {
		return;
	}

	const alertContainerEl = document.getElementById('session-alert-container');
	if (!alertContainerEl) {
		return;
	}

	const alertClass = type === 'success' ? 'alert-success' : 'alert-error';

	alertContainerEl.className = 'active-sessions__alert-container alert ' + alertClass;
	alertContainerEl.textContent = message;
};

const createCsrfHeader = function () {
	const token = $('meta[name="_csrf"]').attr('content');
	const headerName = $('meta[name="_csrf_header"]').attr('content');
	return {[headerName]: token};
};

const performLogout = function () {
	$('#logoutForm').submit();
};

$('[data-function="end-all-sessions"]').on('click', event => {
	event.preventDefault();
	endAllSessions();
});

$('[data-function="end-session"]').on('click', event => {
	event.preventDefault();
	const sessionId = $(event.delegateTarget).attr('data-session-id');
	endSession(sessionId);
});

$('[data-function="toggle-session-expansion"]').on('click', event => {
	event.preventDefault();
	const target = $(event.delegateTarget).closest('.active-sessions__session');
	$(target).toggleClass('active-sessions__session--expanded');
	const expandCollapseIcon = $(target).find('.active-sessions__session-expand-toggle .icon');
	$(expandCollapseIcon).toggleClass('icon-expand');
	$(expandCollapseIcon).toggleClass('icon-collapse');
});

$('[data-function="logout"]').on('click', event => {
	event.preventDefault();
	performLogout();
});

document.addEventListener('DOMContentLoaded', () => {
	showSuccessNoticeFromUrl();

	const locale = document.documentElement.lang || 'et';

	const hasDisplayNames = window.Intl && Intl.DisplayNames;
	const regionNames = hasDisplayNames
		? new Intl.DisplayNames([locale], {type: 'region'})
		: null;

	const unknownCountry = getActiveSessionsMessage('unknownCountry') || '';

	function isValidCountryCode(code) {
    	return /^[A-Za-z]{2}$/.test(code);
    }

	document
		.querySelectorAll('.active-sessions__session-location-text')
		.forEach(locationTextEl => {
			const code = locationTextEl.dataset.countryCode;
			const flagEl = locationTextEl
				.closest('.active-sessions__session-location')
				?.querySelector('.active-sessions__session-flag');

			if (!code || code.trim() === '') {
				locationTextEl.textContent = unknownCountry;
				if (flagEl) {
					flagEl.remove();
				}
				return;
			}

			let countryName = code;

            if (regionNames && isValidCountryCode(code)) {
                countryName = regionNames.of(code) || code;
            }

            locationTextEl.textContent = countryName;

			if (flagEl) {
				flagEl.src = `/webjars/flag-icons/${FLAG_ICONS_VERSION}/flags/4x3/${code.toLowerCase()}.svg`;
				flagEl.alt = '';
			}
		});
});

document.addEventListener("DOMContentLoaded", function () {
    // https://github.com/ua-parser/uap-core/blob/master/regexes.yaml
	const OS_ICONS = [
        {match: "windows", icon: "windows.svg"},

        {match: "mac", icon: "apple.svg"},
        {match: "ios", icon: "apple.svg"},
        {match: "atv os x", icon: "apple.svg"},
        {match: "watchos", icon: "apple.svg"},
        {match: "tvos", icon: "apple.svg"},

        {match: "android", icon: "android.svg"},

        {match: "linux", icon: "linux.svg"},
        {match: "debian", icon: "linux.svg"},
        {match: "gentoo", icon: "linux.svg"},
        {match: "chrome os", icon: "linux.svg"},
        {match: "firefox os", icon: "linux.svg"},
        {match: "kaios", icon: "linux.svg"},
        {match: "red hat", icon: "linux.svg"}
    ];

    // https://github.com/ua-parser/uap-core/blob/master/regexes.yaml
	const BROWSER_ICONS = [
		{match: "chrome", icon: "chrome.svg"},
		{match: "edge", icon: "edge.svg"},
		{match: "firefox", icon: "firefox.svg"},
		{match: "opera", icon: "opera.svg"},
		{match: "safari", icon: "safari.svg"},
		{match: "samsung", icon: "samsung-internet.svg"}
	];

	function resolveIcon(value, icons) {
		if (!value) {
			return null;
		}

		const normalized = value.toLowerCase();

		const match = icons.find(entry =>
			normalized.includes(entry.match)
		);

		return match ? match.icon : null;
	}

	document.querySelectorAll("[data-os]").forEach(img => {
		const os = img.dataset.os;
		const icon = resolveIcon(os, OS_ICONS);

		if (icon) {
			img.src = "/devices/" + icon;
		}
	});

	document.querySelectorAll("[data-browser]").forEach(img => {
		const browser = img.dataset.browser;
		const icon = resolveIcon(browser, BROWSER_ICONS);

		if (icon) {
			img.src = "/browsers/" + icon;
		}
	});
});