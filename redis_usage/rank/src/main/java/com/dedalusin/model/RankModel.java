package com.dedalusin.model;

import java.io.Serializable;

/**
 * redis model 序列化可以考虑使用protobuf，这里就简单随便搞个了
 */
public class RankModel implements Serializable {
    String name;
    long score;
    long delta;

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
