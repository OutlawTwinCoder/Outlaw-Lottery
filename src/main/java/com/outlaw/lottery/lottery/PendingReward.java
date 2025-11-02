package com.outlaw.lottery.lottery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record PendingReward(UUID ticketId, UUID owner, List<Integer> numbers, double amount, long availableAt) {
    public PendingReward {
        List<Integer> copy = new ArrayList<>(numbers);
        Collections.sort(copy);
        numbers = Collections.unmodifiableList(copy);
    }

    public boolean isReady() {
        return System.currentTimeMillis() >= availableAt;
    }

    public long remainingMillis() {
        return Math.max(0L, availableAt - System.currentTimeMillis());
    }

    public String formatNumbers() {
        return numbers.stream().map(n -> String.format("%02d", n)).collect(java.util.stream.Collectors.joining("-"));
    }

    public Instant availableAtInstant() {
        return Instant.ofEpochMilli(availableAt);
    }
}
