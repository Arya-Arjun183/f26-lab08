package edu.cmu.cs214.campusreserve.notifications.templates;

import edu.cmu.cs214.campusreserve.notifications.NotificationKind;

/** The message a person gets when one of their bookings changes. */
public final class BookingModifiedTemplate extends TextMessageTemplate {
    private static final String SUBJECT = "Booking for room {roomId} was updated";
    private static final String BODY = """
        Hi {name},

        Your booking of room {roomId} changed from {previousWhen} to {when}.
        The booking number is {bookingId}.
        """;

    public BookingModifiedTemplate() {
        super(NotificationKind.BOOKING_MODIFIED, SUBJECT, BODY);
    }
}
