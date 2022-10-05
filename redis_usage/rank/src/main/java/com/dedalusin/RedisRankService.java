package com.dedalusin;

import com.dedalusin.model.RankModel;

import java.util.List;

public interface RedisRankService {
    void updateRankUserData(RankModel rankModel);
    List<RankModel> getRankList();
}
