package org.example.exception;

import lombok.Getter;

@Getter
public class InsufficientDataException extends RuntimeException {
    private final int requiredDays;
    private final int availableDays;

    public InsufficientDataException(String message, int requiredDays, int availableDays) {
        super(message);
        this.requiredDays = requiredDays;
        this.availableDays = availableDays;
    }
}