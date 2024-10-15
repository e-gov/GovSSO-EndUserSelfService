package ee.ria.govsso.enduserselfservice.error;

import ee.ria.govsso.enduserselfservice.util.ExceptionUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, MethodNotAllowedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void handleMethodNotAllowedException(Exception e) {
    }

    @ExceptionHandler({ConstraintViolationException.class, MissingServletRequestParameterException.class})
    public void handleBindException(Exception e, HttpServletResponse response) {
        handleException(e, response, ErrorCode.USER_INPUT);
    }

    @ExceptionHandler({ApplicationException.class})
    public void handleApplicationException(ApplicationException e, HttpServletResponse response) {
        handleException(e, response, e.getErrorCode());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public void handleNoResourceException(HttpServletResponse response) throws IOException {
        response.sendError(ErrorCode.USER_INVALID_RESOURCE.getHttpStatusCode());
    }

    @ExceptionHandler({Exception.class})
    public void handleAll(Exception e, HttpServletResponse response) {
        handleException(e, response, ErrorCode.TECHNICAL_GENERAL);
    }

    @SneakyThrows
    private static void handleException(Exception e, HttpServletResponse response, ErrorCode errorCode) {
        String format = "Error {}: {}";
        StructuredArgument errorCodeArgument = value("error.code", errorCode.name());
        String causeMessages = ExceptionUtil.getCauseMessages(e);
        if (errorCode.isLogStackTrace() || log.isDebugEnabled()) {
            log.error(format, errorCodeArgument, causeMessages, e);
        } else {
            log.error(format, errorCodeArgument, causeMessages);
        }
        response.sendError(errorCode.getHttpStatusCode());
    }
}
