package com.kbulkup.asset.service;

import com.kbulkup.asset.dto.cache.StaticAssetData;
import com.kbulkup.asset.dto.response.TransactionListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TraineeAssetCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STATIC_CACHE_KEY_PREFIX = "trainee:asset:static:";
    private static final String TX_CACHE_KEY_PREFIX = "trainee:asset:transactions:";

    //정적 데이터 캐시 조회
    public StaticAssetData getStaticData(Long userId) {
        String key = STATIC_CACHE_KEY_PREFIX + userId;
        return (StaticAssetData) redisTemplate.opsForValue().get(key);
    }

    //정적 데이터 캐시 저장
    public void saveStaticData(Long userId,StaticAssetData data) {
        String key = STATIC_CACHE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, data, 24, TimeUnit.HOURS);
    }

    //거래 내역 캐시 조회
    public List<TransactionListResponseDTO> getTransactions(Long userId) {
        String key = TX_CACHE_KEY_PREFIX + userId;
        return (List<TransactionListResponseDTO>) redisTemplate.opsForValue().get(key);
    }

    //거래 내역 캐시 저장
    public void saveTransactions(Long userId, List<TransactionListResponseDTO> transactions) {
        String key = TX_CACHE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, transactions, 10, TimeUnit.MINUTES);
    }

    // 캐시 무효화
    public void evictStaticData(Long userId) {
        String key = STATIC_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    // 외부 토큰 조회
    public String getExternalAccessToken(Long userId) {
        String key = "user:token:" + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }
}
