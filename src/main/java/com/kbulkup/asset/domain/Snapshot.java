package com.kbulkup.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Snapshot {
    private Long snapshotId;
    private Long userId;
    private Long balance;
    private LocalDateTime snapshotDate;

    public Snapshot(Long userId, Long balance, LocalDateTime snapshotDate) {
        this.userId = userId;
        this.balance = balance;
        this.snapshotDate = snapshotDate;
    }
}
