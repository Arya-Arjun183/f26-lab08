package edu.cmu.cs214.campusreserve.core.domain;

import java.time.Instant;
import java.util.Objects;

/** An immutable record of one successful, non-no-op change to a booking. */
public record BookingModification(
    TimeInterval previousInterval,
    int previousAttendees,
    TimeInterval newInterval,
    int newAttendees,
    Instant modifiedAt) {

    public BookingModification {
        Objects.requireNonNull(previousInterval, "previousInterval");
        Objects.requireNonNull(newInterval, "newInterval");
        Objects.requireNonNull(modifiedAt, "modifiedAt");
        if (previousAttendees <= 0 || newAttendees <= 0) {
            throw new IllegalArgumentException("Attendees must be positive.");
        }
    }
}
