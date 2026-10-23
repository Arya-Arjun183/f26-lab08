package edu.cmu.cs214.campusreserve.core.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** The result of asking to modify an existing booking. */
public sealed interface ModificationResult {
    record Modified(Booking previous, Booking booking, List<CancellationResult.Promotion> promotions)
        implements ModificationResult {
        public Modified {
            Objects.requireNonNull(previous, "previous");
            Objects.requireNonNull(booking, "booking");
            promotions = List.copyOf(promotions);
        }
    }

    /** A valid request that already matches the stored booking. */
    record Unchanged(Booking booking) implements ModificationResult {
        public Unchanged {
            Objects.requireNonNull(booking, "booking");
        }
    }

    record Rejected(RejectionReason reason, String detail) implements ModificationResult {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
            detail = detail == null ? reason.message() : detail;
        }
    }

    static ModificationResult rejected(RejectionReason reason) {
        return new Rejected(reason, reason.message());
    }

    default boolean isModified() {
        return this instanceof Modified;
    }

    default boolean isUnchanged() {
        return this instanceof Unchanged;
    }

    default Optional<Booking> modifiedBooking() {
        if (this instanceof Modified modified) {
            return Optional.of(modified.booking());
        }
        if (this instanceof Unchanged unchanged) {
            return Optional.of(unchanged.booking());
        }
        return Optional.empty();
    }

    default Optional<RejectionReason> rejectionReason() {
        return this instanceof Rejected rejected ? Optional.of(rejected.reason()) : Optional.empty();
    }
}
