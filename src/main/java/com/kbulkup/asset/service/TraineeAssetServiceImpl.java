package com.kbulkup.asset.service;

import com.kbulkup.asset.client.OpenBankingClient;
import com.kbulkup.asset.domain.Composition;
import com.kbulkup.asset.domain.Snapshot;
import com.kbulkup.asset.dto.cache.StaticAssetData;
import com.kbulkup.asset.dto.response.*;
import com.kbulkup.asset.mapper.TraineeAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TraineeAssetServiceImpl implements TraineeAssetService {

    private final OpenBankingClient openBankingClient;
    private final TraineeAssetMapper traineeAssetMapper;
    private final TraineeAssetCacheService cacheService;

    @Override
    public TraineeAssetDetailResponseDTO getTraineeAsset(Long id) {
        // 1. 캐시에서 정적 데이터 조회 -> 캐시 히트 시 바로 반환
        StaticAssetData staticData = cacheService.getStaticData(id);

        // 2. 정적 데이터 캐시 미스 시 DB 조회 및 캐시 저장
        if (staticData == null) {
            List<Snapshot> snapshots = traineeAssetMapper.getSnapshotsByTraineeId(id);
            Composition composition = traineeAssetMapper.getCompositionsByTraineeId(id);

            staticData = new StaticAssetData(snapshots, composition);
            cacheService.saveStaticData(id, staticData);
        }

        // 3. 캐시에서 거래내역 데이터 조회 -> 캐시 히트 시 바로 반환
        List<TransactionListResponseDTO> transactions = cacheService.getTransactions(id);

        // 4. 거래 내역 데이터 캐시 미스 시 DB 조회 및 캐시 저장
        if (transactions == null) {
            transactions = fetchTransactionsFromExternalAPI(id);
            cacheService.saveTransactions(id, transactions);
        }

        return TraineeAssetDetailResponseDTO.toDTO(transactions, staticData.getSnapshots(), staticData.getComposition());
    }

    @Override
    public TraineeAccountResponseDTO createAccount(String bank, String accountNumber, String accountHolderName, Long userId) {
        String externalAccessToken = "test-auth-token-1234";  //redis에서 userId로부터 외부 서비스 토큰 꺼내기

        return openBankingClient.createAccounts(bank, accountNumber, accountHolderName, externalAccessToken);
    }

    @Override
    public TraineeAssetDetailResponseDTO findTraineeAssetDetailByRoomID(String roomId) {
        Long userId = traineeAssetMapper.findUserIdByRoomID(roomId);
        return getTraineeAsset(userId);
    }

    private List<TransactionListResponseDTO> fetchTransactionsFromExternalAPI(Long userId) {
        List<TransactionListResponseDTO> transactions = new ArrayList<>();
        List<String> fintechUseNums = traineeAssetMapper.getFintechUseNumsByUserId(userId);

        String externalAccessToken = "test-auth-token-1234";  //redis에서 userId로부터 외부 서비스 토큰 꺼내기
        for (String fintechUseNum : fintechUseNums) {
            List<TransactionListResponseDTO> externalTransactions = openBankingClient.getTransactions(fintechUseNum, externalAccessToken);

            if (externalTransactions != null && !externalTransactions.isEmpty()) {
                transactions.addAll(externalTransactions);
            }
        }

        return transactions;
    }
}
