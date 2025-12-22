package com.kbulkup.asset.client;

import com.kbulkup.asset.dto.request.TokenRequestDTO;
import com.kbulkup.asset.dto.response.TraineeAccountResponseDTO;
import com.kbulkup.asset.dto.response.TransactionListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenBankingClient {

    @Value("${external-url:http://localhost:8080}")
    private String baseUrl;

    public List<TransactionListResponseDTO> getTransactions(String fintechUseNum, String externalAccessToken) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl(baseUrl)
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/external/transactions")
                        .queryParam("fintech_use_num", fintechUseNum)
                        .build())
                .header("Authorization", externalAccessToken)
                .retrieve()
                .bodyToFlux(TransactionListResponseDTO.class)
                .collectList()
                .block();
    }

    public TraineeAccountResponseDTO createAccounts(String bankCode, String accountNum, String accountHolderName, String externalAccessToken) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl(baseUrl)
                .build();

        return webClient.post()
                .uri("/external/accounts")
                .bodyValue(TokenRequestDTO.create(bankCode, accountNum, accountHolderName))
                .header("Authorization", externalAccessToken)
                .retrieve()
                .bodyToMono(TraineeAccountResponseDTO.class)
                .block();
    }
}
