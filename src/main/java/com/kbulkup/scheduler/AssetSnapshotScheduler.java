package com.kbulkup.scheduler;

import com.kbulkup.asset.client.OpenBankingClient;
import com.kbulkup.asset.domain.Composition;
import com.kbulkup.asset.domain.Snapshot;
import com.kbulkup.asset.dto.cache.StaticAssetData;
import com.kbulkup.asset.dto.response.TransactionListResponseDTO;
import com.kbulkup.asset.mapper.TraineeAssetMapper;
import com.kbulkup.asset.service.TraineeAssetCacheService;
import com.kbulkup.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetSnapshotScheduler {

    private final UserMapper userMapper;
    private final TraineeAssetMapper traineeAssetMapper;
    private final OpenBankingClient  openBankingClient;
    private final TraineeAssetCacheService cacheService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void createDailySnapShot() {

        // 0. trainee 목록 조회
        List<Long> traineeIds = userMapper.getTraineeIds();

        for (Long traineeId : traineeIds) {
            try {
                // 1. 사용자의 계좌 목록 조회 (fintechUseNum)
                List<String> fintechUseNums = traineeAssetMapper.getFintechUseNumsByUserId(traineeId);
                if (fintechUseNums == null || fintechUseNums.isEmpty()) {
                    continue;
                }

                // 2. 외부 API에서 거래내역 조회
                List<TransactionListResponseDTO> transactions = new ArrayList<>();
                String externalAccessToken = cacheService.getExternalAccessToken(traineeId);

                for (String fintechUseNum : fintechUseNums){
                    List<TransactionListResponseDTO> externalTransactions = openBankingClient.getTransactions(fintechUseNum, externalAccessToken);
                    if (externalTransactions != null && !externalTransactions.isEmpty()) {
                        transactions.addAll(externalTransactions);
                    }
                }

                // 3. 스냅샷 생성
                List<Snapshot> snapshots = new ArrayList<>();
                for (TransactionListResponseDTO transaction : transactions) {
                    snapshots.add(new Snapshot(traineeId, transaction.getAfterBalanceAmt(), transaction.getTranDate()));
                }

                // 4. 스냅샷 저장
                traineeAssetMapper.insertSnapshots(traineeId, snapshots);

                // 5. redis 캐시 갱신 (warm up 방식)
                List<Snapshot> snapshot = traineeAssetMapper.getSnapshotsByTraineeId(traineeId);
                Composition compositions = traineeAssetMapper.getCompositionsByTraineeId(traineeId);

                StaticAssetData staticAssetData = new StaticAssetData(snapshot, compositions);
                cacheService.saveStaticData(traineeId, staticAssetData);
            } catch (Exception e) {
                log.error("스냅샷 생성 실패 traineeId: {}", traineeId, e);
            }
        }
    }
}
