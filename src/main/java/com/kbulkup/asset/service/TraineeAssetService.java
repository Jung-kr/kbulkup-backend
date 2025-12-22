package com.kbulkup.asset.service;

import com.kbulkup.asset.dto.response.TraineeAccountResponseDTO;
import com.kbulkup.asset.dto.response.TraineeAssetDetailResponseDTO;
import com.kbulkup.user.domain.User;

public interface TraineeAssetService {

    TraineeAssetDetailResponseDTO getTraineeAsset(Long userId);

    TraineeAssetDetailResponseDTO findTraineeAssetDetailByRoomID(String roomId);

    TraineeAccountResponseDTO createAccount(String bank, String accountNumber, String accountHolderName, Long userId);
}
