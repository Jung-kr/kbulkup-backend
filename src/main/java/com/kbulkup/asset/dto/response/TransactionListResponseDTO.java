package com.kbulkup.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionListResponseDTO {
    private LocalDateTime tranDate;
    private String inoutType;
    private Long tranAmt;
    private Long afterBalanceAmt;
    private String printedContent;
}
