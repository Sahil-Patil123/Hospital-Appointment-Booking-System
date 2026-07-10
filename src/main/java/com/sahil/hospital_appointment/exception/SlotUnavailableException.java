package com.sahil.hospital_appointment.exception;

public class SlotUnavailableException extends RuntimeException {

    public SlotUnavailableException(String message) {
        super(message);
    }
}