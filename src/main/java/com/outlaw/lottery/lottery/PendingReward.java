package com.outlaw.lottery.lottery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record PendingReward(UUID ticketId, UUID owner, List<Integer> numbers, double amount, long claimDeadline) {
    public PendingReward {
        List<Integer> copy = new ArrayList<>(numbers);
        Collections.sort(copy);
        numbers = Collections.unmodifiableList(copy);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > claimDeadline;
    }

    public long remainingMillis() {
        return Math.max(0L, claimDeadline - System.currentTimeMillis());
    }

    public String formatNumbers() {
        return numbers.stream().map(n -> String.format("%02d", n)).collect(java.util.stream.Collectors.joining("-"));
    }

    public Instant claimDeadlineInstant() {
        return Instant.ofEpochMilli(claimDeadline);
    }
}
