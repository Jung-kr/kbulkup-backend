package com.kbulkup.asset.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(description = "자산 연동 은행 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequestDTO {
    @ApiModelProperty(value = "은행 코드/식별자")
    private String bankCode;

    @ApiModelProperty(value = "계좌 번호")
    private String accountNum;

    @ApiModelProperty(value = "사용자 이름")
    private String accountHolderName;

    public static TokenRequestDTO create(String bankCode, String accountNum, String accountHolderName) {
        return TokenRequestDTO.builder()
                .bankCode(bankCode)
                .accountNum(accountNum)
                .accountHolderName(accountHolderName)
                .build();
    }
}
