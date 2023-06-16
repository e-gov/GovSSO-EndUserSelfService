package ee.ria.govsso.enduserselfservice.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_INPUT(400, false),
    TECHNICAL_GENERAL(500, true);

    private final int httpStatusCode;
    private final boolean logStackTrace;
}
