package com.kbulkup.asset.service;

import com.kbulkup.asset.client.OpenBankingClient;
import com.kbulkup.asset.domain.Composition;
import com.kbulkup.asset.domain.Snapshot;
import com.kbulkup.asset.dto.request.TokenRequestDTO;
import com.kbulkup.asset.dto.response.*;
import com.kbulkup.asset.mapper.TraineeAssetMapper;
import com.kbulkup.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TraineeAssetServiceImpl implements TraineeAssetService {

    private final TraineeAssetMapper traineeAssetMapper;
    private final OpenBankingClient openBankingClient;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TraineeAssetDetailResponseDTO getTraineeAsset(Long id) {
        //캐시 히트시 리턴

        List<String> fintechUseNums = traineeAssetMapper.getFintechUseNumsByUserId(id);
        List<TransactionListResponseDTO> transactions = new ArrayList<>();

        String externalAccessToken = "test-auth-token-1234";  //redis에서 userId로부터 외부 서비스 토큰 꺼내기
        for(String fintechUseNum : fintechUseNums){
            List<TransactionListResponseDTO> externalTransactions = openBankingClient.getTransactions(fintechUseNum, externalAccessToken);
            if (externalTransactions != null && !externalTransactions.isEmpty()) transactions.addAll(externalTransactions);
        }

        List<Snapshot> snapshots = traineeAssetMapper.getSnapshotsByTraineeId(id);
        Composition composition = traineeAssetMapper.getCompositionsByTraineeId(id);

        //캐시에 저장

        return TraineeAssetDetailResponseDTO.toDTO(transactions, snapshots, composition);
    }

    private ExternalTokenResponseDTO getAccessTokenAndFintechUseNum(String bank, String accountNumber) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl("http://13.125.89.72:9080") //http://13.125.89.72:9080
                .build();
        return webClient.post()
                .uri("/external-api/create-user")
                .bodyValue(TokenRequestDTO.create(bank, accountNumber))
                .retrieve()
                .bodyToMono(ExternalTokenResponseDTO.class)
                .block();
    }

    private ExternalAccessTokenResponseDTO getAccessToken(String fintechUseNum) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl("http://13.125.89.72:9080") //http://13.125.89.72:9080
                .build();
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/external-api/token")
                        .queryParam("fintechUseNum", fintechUseNum) // 쿼리 파라미터 추가
                        .build())
                .retrieve()
                .bodyToMono(ExternalAccessTokenResponseDTO.class)
                .block();
    }

    private ExternalAssetResponseDTO getUserAssetData(String token, String fintechUseNum) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl("http://13.125.89.72:9080") //http://13.125.89.72:9080
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/external-api/user-data")
                        .queryParam("fintechUseNum", fintechUseNum)
                        .build()
                )
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(ExternalAssetResponseDTO.class)
                .block();
    }

    @Override
    public TraineeAssetDetailResponseDTO findTraineeAssetDetailByRoomID(String roomId) {
        Long userId = traineeAssetMapper.findUserIdByRoomID(roomId);
        return getTraineeAsset(userId);
    }
}
