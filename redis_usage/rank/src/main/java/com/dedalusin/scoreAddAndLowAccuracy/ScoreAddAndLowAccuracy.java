package com.dedalusin.scoreAddAndLowAccuracy;

import com.alibaba.fastjson.JSON;
import com.dedalusin.RedisRankService;
import com.dedalusin.model.RankModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.DAYS;

/**
 * 场景1：分数只增不减，对精确性要求不高
 */
public class ScoreAddAndLowAccuracy implements RedisRankService {
    @Override
    public void updateRankUserData(RankModel rankModel) {
        System.out.println("---------update------------");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(8);
        config.setMaxTotal(18);
        JedisPool pool = new JedisPool(config, "127.0.0.1", 6379, 2000);
        Jedis jedis = pool.getResource();
        Pipeline p = jedis.pipelined();
        // 1. 对全量用户数据进行增减
        Response<Long> scoreResult = p.hincrBy("userDataTable", rankModel.getName(), rankModel.getDelta());
        p.pexpire(rankModel.getName(), DAYS.toMillis(1));
        p.sync();
        // 2. 更新rank表
        long updatedScore = scoreResult.get();
        rankModel.setScore(updatedScore);
        String jsonString = JSON.toJSONString(rankModel);
        p.zadd("rank", updatedScore, jsonString);
        Response<Long> rankSizeResult = p.zcard("rank");
        p.sync();
        // 3. 裁剪
        long size = rankSizeResult.get();
        if (size > 10) {
            // 假设为10的榜单
            p.zremrangeByRank("rank", 0, -11);
        }
        p.sync();
        jedis.close();
        pool.close();
        System.out.println("---------update finished------------");
    }

    @Override
    public List<RankModel> getRankList() {
        System.out.println("---------get------------");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(8);
        config.setMaxTotal(18);
        JedisPool pool = new JedisPool(config, "127.0.0.1", 6379, 2000);
        Jedis jedis = pool.getResource();
        Pipeline p = jedis.pipelined();
        Response<List<String>> result = p.zrevrange("rank", 0, -1);
        p.sync();
        jedis.close();
        pool.close();
        System.out.println("---------update finished------------");
        return result.get().stream().map(jsonString -> JSON.parseObject(jsonString, RankModel.class)).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        RedisRankService rankService = new ScoreAddAndLowAccuracy();
        IntStream.range(2, 20).forEach(i -> {
            RankModel rankModel = new RankModel();
            rankModel.setName(String.valueOf(i));
            rankModel.setDelta(i);
            rankService.updateRankUserData(rankModel);
        });
        rankService.getRankList().stream().map(e -> e.getName()+" score: "+e.getScore()).forEach(System.out::println);
    }
}
