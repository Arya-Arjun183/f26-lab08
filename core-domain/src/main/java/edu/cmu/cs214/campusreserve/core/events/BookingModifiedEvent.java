package edu.cmu.cs214.campusreserve.core.events;

import edu.cmu.cs214.campusreserve.core.domain.Booking;
import edu.cmu.cs214.campusreserve.core.domain.TimeInterval;
import java.time.Instant;
import java.util.Objects;

/** A booking's time interval and/or attendee count changed. */
public record BookingModifiedEvent(
    Instant occurredAt, String bookingId, String roomId, String userId,
    TimeInterval previousInterval, int previousAttendees,
    TimeInterval interval, int attendees) implements DomainEvent {

    public static final String TYPE = "booking-modified";

    public BookingModifiedEvent {
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(bookingId, "bookingId");
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(previousInterval, "previousInterval");
        Objects.requireNonNull(interval, "interval");
    }

    public static BookingModifiedEvent of(Booking previous, Booking modified, Instant occurredAt) {
        return new BookingModifiedEvent(occurredAt, modified.id(), modified.roomId(), modified.userId(),
            previous.interval(), previous.attendees(), modified.interval(), modified.attendees());
    }

    @Override public String type() { return TYPE; }
}
