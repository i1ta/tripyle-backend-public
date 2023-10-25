package com.tripyle.common.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("존재하지 않는 정보입니다.");
    }

    public NotFoundException(String message) {
        super(message);
    }
}

