package com.nightydead.lordofmysteries.data;

import com.mojang.serialization.Codec;

import java.util.*;

/**
 * 玩家已学习魔药配方数据
 * 通过 NeoForge Attachment 持久化挂载到 Player，死亡不掉
 * 每个元素格式为 "pathway:sequence"（如 "fool:9"）
 */
public class LearnedRecipesData {

    /** Codec 编解码器：Set<String> ↔ LearnedRecipesData */
    public static final Codec<LearnedRecipesData> CODEC = Codec.STRING.listOf()
            .xmap(
                    list -> {
                        LearnedRecipesData data = new LearnedRecipesData();
                        data.setAll(list);
                        return data;
                    },
                    data -> new ArrayList<>(data.getAll())
            );

    private final Set<String> learned = new HashSet<>();

    /** 检查是否已学习指定途径序列的配方 */
    public boolean hasLearned(String pathway, int sequence) {
        return learned.contains(pathway + ":" + sequence);
    }

    /**
     * 学习配方
     * @return true 表示新学会的，false 表示已学过（重复）
     */
    public boolean learn(String pathway, int sequence) {
        return learned.add(pathway + ":" + sequence);
    }

    /** 获取所有已学配方的不变集合 */
    public Set<String> getAll() {
        return Collections.unmodifiableSet(learned);
    }

    /** 批量设置已学配方（用于序列化还原） */
    public void setAll(Collection<String> entries) {
        learned.clear();
        learned.addAll(entries);
    }

    /** 获取已学配方数量 */
    public int size() {
        return learned.size();
    }
}
