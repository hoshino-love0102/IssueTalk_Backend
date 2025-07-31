package com.issuetalk.exception.errorcode;

public enum OpenAIErrorCode {
    INVALID_ARGUMENT("유효하지 않은 요청입니다."),
    FAILED_TO_GENERATE("응답 생성에 실패했습니다.");

    private final String message;

    OpenAIErrorCode(String message) {
        this.message = message;
    }

    public RuntimeException defaultException() {
        return new RuntimeException(message);
    }

    public RuntimeException defaultException(Throwable cause) {
        return new RuntimeException(message, cause);
    }
}
