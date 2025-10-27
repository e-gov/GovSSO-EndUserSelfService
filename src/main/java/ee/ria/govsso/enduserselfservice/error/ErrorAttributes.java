package ee.ria.govsso.enduserselfservice.error;

import ee.ria.govsso.enduserselfservice.util.TimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static ee.ria.govsso.enduserselfservice.logging.TraceIdLoggingFilter.REQUEST_ATTRIBUTE_NAME_REQUEST_ID;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorAttributes extends DefaultErrorAttributes {
    public static final String ERROR_ATTR_MESSAGE = "message";
    public static final String ERROR_ATTR_ERROR_CODE = "error";
    public static final String ERROR_ATTR_INCIDENT_NR = "incident_nr";
    public static final String ERROR_ATTR_OFFSET_TIMESTAMP = "offset_timestamp";

    private final MessageSource messageSource;

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> attr = super.getErrorAttributes(webRequest, options);

        Throwable error = getError(webRequest);
        HttpStatus status = HttpStatus.resolve((int) attr.get("status"));

        ErrorCode errorCode;
        String incidentNumber = null;
        if (error instanceof ApplicationException applicationException) {
            errorCode = applicationException.getErrorCode();
        } else {
            if (status != null && status.is4xxClientError()) {
                errorCode = ErrorCode.USER_INPUT;
            } else {
                errorCode = ErrorCode.TECHNICAL_GENERAL;
                incidentNumber = (String) webRequest.getAttribute(REQUEST_ATTRIBUTE_NAME_REQUEST_ID, SCOPE_REQUEST);
            }
        }

        String messageCode = "error." + errorCode.name().toLowerCase(Locale.ROOT);
        String defaultMessage = "??" + messageCode + "??";
        String message = messageSource.getMessage(messageCode, null, defaultMessage, getLocale());

        Date errorTimestamp = (Date) attr.get("timestamp");
        if (errorTimestamp == null) {
            errorTimestamp = new Date();
        }
        OffsetDateTime timestampWithOffset = TimeUtil.toOffsetDateTime(errorTimestamp);

        attr.put(ERROR_ATTR_MESSAGE, message);
        attr.put(ERROR_ATTR_INCIDENT_NR, incidentNumber);
        attr.put(ERROR_ATTR_ERROR_CODE, errorCode.name());
        attr.put(ERROR_ATTR_OFFSET_TIMESTAMP, timestampWithOffset);
        return attr;
    }

    // webRequest.getLocale() returns wrong locale.
    private static Locale getLocale() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver == null) {
            // TODO When is this null?
            return null;
        }
        return localeResolver.resolveLocale(request);
    }
}
