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
}

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
}

const showError = function (error) {
	console.error('Error', error);
};

const createCsrfHeader = function() {
	const token = $('meta[name="_csrf"]').attr('content');
	const headerName = $('meta[name="_csrf_header"]').attr('content');
	return { [headerName]: token };
}

$('a[data-function="end-all-sessions"]').on('click', event => {
	event.preventDefault();
	endAllSessions();
})

$('a[data-function="end-session"]').on('click', event => {
	event.preventDefault();
	const sessionId = $(event.delegateTarget).attr('data-session-id');
	endSession(sessionId);
})

