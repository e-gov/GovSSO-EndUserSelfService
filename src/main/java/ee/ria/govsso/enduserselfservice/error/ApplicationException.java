package ee.ria.govsso.enduserselfservice.error;

import lombok.Getter;

import static ee.ria.govsso.enduserselfservice.error.ErrorCode.TECHNICAL_GENERAL;

@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = TECHNICAL_GENERAL;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = TECHNICAL_GENERAL;
    }

    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
