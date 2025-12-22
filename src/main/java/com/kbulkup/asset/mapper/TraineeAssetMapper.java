package com.kbulkup.asset.mapper;

import com.kbulkup.asset.domain.Composition;
import com.kbulkup.asset.domain.Snapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TraineeAssetMapper {

    List<Snapshot> getSnapshotsByTraineeId(@Param("traineeId") Long TraineeId);

    Composition getCompositionsByTraineeId(@Param("traineeId") Long TraineeId);

    void insertFintechUseNum(@Param("userId") Long userId, @Param("bank") String bank, @Param("fintechUseNum") String fintechUseNum);

    void insertSnapshots(@Param("traineeId") Long traineeId, @Param("snapshots") List<Snapshot> snapshots);

    void insertComposition(@Param("traineeId") Long traineeId, @Param("composition") Composition composition);

    Long findUserIdByRoomID(@Param("roomId") String roomId);

    List<String> getFintechUseNumsByUserId(@Param("userId") Long userId);

    void deleteSnapshotsWindow(@Param("userId") Long id, @Param("startDate")  LocalDateTime startDateTime, @Param("endDate") LocalDateTime endDateTime);
}
