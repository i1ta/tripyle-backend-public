package com.tripyle.common.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("인증에 실패하였습니다.");
    }
}

