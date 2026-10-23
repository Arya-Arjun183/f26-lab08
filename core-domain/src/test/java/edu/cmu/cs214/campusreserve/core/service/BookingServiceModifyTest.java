package edu.cmu.cs214.campusreserve.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.campusreserve.core.domain.BookingModificationRequest;
import edu.cmu.cs214.campusreserve.core.domain.BookingOutcome;
import edu.cmu.cs214.campusreserve.core.domain.ModificationResult;
import edu.cmu.cs214.campusreserve.core.domain.RejectionReason;
import edu.cmu.cs214.campusreserve.core.events.BookingModifiedEvent;
import edu.cmu.cs214.campusreserve.core.support.Fixtures;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingServiceModifyTest extends BookingServiceTestBase {

    @Test
    void ownerCanMoveTheirBookingWithoutConflictingWithThemselves() {
        String id = bookAndGetId("r-1", "u-1", Fixtures.interval("10:00", "11:00"));
        ModificationResult result = service.modifyBooking(new BookingModificationRequest(
            id, "u-1", Fixtures.interval("10:15", "11:15"), 4));

        assertTrue(result.isModified());
        assertEquals(Fixtures.interval("10:15", "11:15"), result.modifiedBooking().orElseThrow().interval());
        assertEquals(1, result.modifiedBooking().orElseThrow().modificationHistory().size());
        assertEquals(List.of("booking-created", "booking-modified"), publisher.types());
    }

    @Test
    void rejectedConflictLeavesTheOriginalBookingAndHistoryUntouched() {
        String id = bookAndGetId("r-1", "u-1", Fixtures.interval("10:00", "11:00"));
        assertTrue(book("r-1", "u-2", Fixtures.interval("11:00", "12:00")).isConfirmed());

        ModificationResult result = service.modifyBooking(new BookingModificationRequest(
            id, "u-1", Fixtures.interval("10:30", "11:30"), 4));

        assertEquals(RejectionReason.ROOM_UNAVAILABLE, result.rejectionReason().orElseThrow());
        assertEquals(Fixtures.interval("10:00", "11:00"), service.findBooking(id).orElseThrow().interval());
        assertTrue(service.findBooking(id).orElseThrow().modificationHistory().isEmpty());
        assertEquals(List.of("booking-created", "booking-created"), publisher.types());
    }

    @Test
    void noOpRecordsNoHistoryEventOrPromotion() {
        String id = bookAndGetId("r-1", "u-1", Fixtures.interval("10:00", "11:00"));
        ModificationResult result = service.modifyBooking(new BookingModificationRequest(
            id, "u-1", Fixtures.interval("10:00", "11:00"), 4));

        assertTrue(result.isUnchanged());
        assertTrue(result.modifiedBooking().orElseThrow().modificationHistory().isEmpty());
        assertEquals(List.of("booking-created"), publisher.types());
    }

    @Test
    void shorteningPromotesTheFirstWaiterForFreedTime() {
        String id = bookAndGetId("r-1", "u-1", Fixtures.interval("10:00", "12:00"));
        BookingOutcome waitlisted = book("r-1", "u-2", Fixtures.interval("11:00", "12:00"));
        assertTrue(waitlisted.isWaitlisted());

        ModificationResult result = service.modifyBooking(new BookingModificationRequest(
            id, "u-1", Fixtures.interval("10:00", "11:00"), 4));

        assertTrue(result.isModified());
        assertEquals(1, ((ModificationResult.Modified) result).promotions().size());
        assertTrue(service.activeBookingsForUser("u-2").stream()
            .anyMatch(booking -> booking.interval().equals(Fixtures.interval("11:00", "12:00"))));
    }

    @Test
    void attendeeOnlyChangeDoesNotRunPromotionButPublishesOldAndNewDetails() {
        String id = bookAndGetId("r-1", "u-1", Fixtures.interval("10:00", "11:00"));
        ModificationResult result = service.modifyBooking(new BookingModificationRequest(
            id, "u-1", Fixtures.interval("10:00", "11:00"), 5));

        assertTrue(result.isModified());
        assertTrue(((ModificationResult.Modified) result).promotions().isEmpty());
        BookingModifiedEvent event = publisher.ofType(BookingModifiedEvent.class).get(0);
        assertEquals(4, event.previousAttendees());
        assertEquals(5, event.attendees());
        assertEquals(Fixtures.interval("10:00", "11:00"), event.previousInterval());
    }
}
