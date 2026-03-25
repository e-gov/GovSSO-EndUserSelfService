'use strict';

const endAllSessions = function () {
	fetch(
		'/api/sessions',
		{
			method: 'DELETE',
			headers: createCsrfHeader()
		}
	).then(
		() => location.reload(),
		(err) => showError(err) // TODO
	)
};

const endSession = function (sessionId) {
	fetch(
		'/api/sessions/' + encodeURIComponent(sessionId),
		{
			method: 'DELETE',
			headers: createCsrfHeader()
		}
	).then(
		() => location.reload(),
		(err) => showError(err) // TODO
	)
};

const showError = function (error) {
	console.error('Error', error);
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
	const target = $(event.delegateTarget).closest(".active-sessions__session");
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
	const locale = document.documentElement.lang || 'et';

	if (!window.Intl || !Intl.DisplayNames) {
		return;
	}

	const regionNames = new Intl.DisplayNames(
		[locale],
		{type: 'region'}
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
			}
		});
});

document.addEventListener("DOMContentLoaded", function () {
	const OS_ICONS = [
		{match: "windows", icon: "windows.svg"},
		{match: "mac", icon: "apple.svg"},
		{match: "ios", icon: "apple.svg"},
		{match: "linux", icon: "linux.svg"},
		{match: "android", icon: "android.svg"}
	];

	const BROWSER_ICONS = [
		{match: "chrome", icon: "chrome.svg"},
		{match: "edge", icon: "edge.svg"},
		{match: "firefox", icon: "firefox.svg"},
		{match: "opera", icon: "opera.svg"},
		{match: "safari", icon: "safari.svg"}
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