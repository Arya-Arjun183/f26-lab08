package edu.cmu.cs214.campusreserve.core.domain;

import java.util.Objects;

/** The new time and attendance details requested by a booking owner. */
public record BookingModificationRequest(String bookingId, String requesterId, TimeInterval interval, int attendees) {
    public BookingModificationRequest {
        bookingId = Identifiers.require(bookingId, "booking id");
        requesterId = Identifiers.require(requesterId, "requester id");
        Objects.requireNonNull(interval, "interval");
        if (attendees <= 0) {
            throw new IllegalArgumentException("Attendees must be positive: " + attendees);
        }
    }
}
