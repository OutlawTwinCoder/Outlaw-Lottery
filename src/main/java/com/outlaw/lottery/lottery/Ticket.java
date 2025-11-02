package com.outlaw.lottery.lottery;

import java.util.List;
import java.util.UUID;

public record Ticket(UUID id, UUID owner, List<Integer> numbers, long purchaseTime) {
    public String formatNumbers() {
        return numbers.stream().map(n -> String.format("%02d", n)).collect(java.util.stream.Collectors.joining("-"));
    }
}
