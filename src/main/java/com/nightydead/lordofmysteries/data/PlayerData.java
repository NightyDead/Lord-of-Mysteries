package com.nightydead.lordofmysteries.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家非凡数据类 - 存储并持久化玩家的全部神秘学核心属性
 */
public class PlayerData {

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("sanity").forGetter(PlayerData::getSanity),
                    Codec.INT.fieldOf("max_sanity").forGetter(PlayerData::getMaxSanity),
                    Codec.FLOAT.fieldOf("digestion").forGetter(PlayerData::getDigestion),
                    Codec.INT.fieldOf("spirituality").forGetter(PlayerData::getSpirituality),
                    Codec.INT.fieldOf("max_spirituality").forGetter(PlayerData::getMaxSpiritual),
                    Codec.STRING.fieldOf("currentPathway").forGetter(PlayerData::getCurrentPathway),
                    Codec.INT.fieldOf("currentSequence").forGetter(PlayerData::getCurrentSequence),
                    Codec.INT.fieldOf("sdcTicks").forGetter(PlayerData::getSdcTicks),
                    Codec.STRING.listOf().fieldOf("absorbedCharacteristics").forGetter(PlayerData::getAbsorbedCharacteristics)
            ).apply(instance, PlayerData::new)
    );

    private int sanity;
    private int maxSanity;
    private float digestion;
    private int spirituality;
    private int maxSpirituality;
    private String currentPathway;
    private int currentSequence;
    private int sdcTicks;
    private List<String> absorbedCharacteristics;

    /** 初始凡人状态构造 */
    public PlayerData() {
        reset();
    }

    /** 重置所有超凡状态为凡人 */
    public void reset() {
        this.maxSanity = 100;
        this.sanity = 100;
        this.digestion = 0.0F;
        this.maxSpirituality = 20;
        this.spirituality = 20;
        this.currentPathway = "none";
        this.currentSequence = 10;
        this.sdcTicks = -1;
        this.absorbedCharacteristics = new ArrayList<>();
    }

    /** 全参数构造函数（Codec 反序列化与深拷贝底层驱动） */
    public PlayerData(int sanity, int maxSanity, float digestion, int spirituality, int maxSpirituality,
                      String currentPathway, int currentSequence, int sdcTicks, List<String> absorbedCharacteristics) {
        this.maxSanity = maxSanity;
        this.maxSpirituality = maxSpirituality;
        this.sanity = clamp(sanity, 0, maxSanity);
        this.digestion = Math.max(0.0F, Math.min(digestion, 1.0F));
        this.spirituality = clamp(spirituality, 0, maxSpirituality);
        this.currentPathway = currentPathway;
        this.currentSequence = currentSequence;
        this.sdcTicks = sdcTicks;
        this.absorbedCharacteristics = new ArrayList<>(absorbedCharacteristics);
    }

    // ==================== 神秘学辅助工具方法 ====================

    public void addAbsorbedRecord(String pathway, int sequence) {
        this.absorbedCharacteristics.add(pathway + ":" + sequence);
    }

    public PlayerData copy() {
        return new PlayerData(this.sanity, this.maxSanity, this.digestion, this.spirituality, this.maxSpirituality,
                this.currentPathway, this.currentSequence, this.sdcTicks, this.absorbedCharacteristics);
    }

    public List<CharacteristicRecord> getParsedCharacteristics() {
        return this.absorbedCharacteristics.stream().map(s -> {
            String[] p = s.split(":");
            return new CharacteristicRecord(p[0], Integer.parseInt(p[1]));
        }).toList();
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

    public record CharacteristicRecord(String pathway, int sequence) {}

    // ==================== Getters & Setters ====================

    public int getSanity() { return this.sanity; }
    public void setSanity(int sanity) { this.sanity = clamp(sanity, 0, this.maxSanity); }
    public int getMaxSanity() { return this.maxSanity; }
    public void setMaxSanity(int maxSanity) { this.maxSanity = maxSanity; }

    public float getDigestion() { return this.digestion; }
    public void setDigestion(float digestion) { this.digestion = Math.max(0.0F, Math.min(digestion, 1.0F)); }

    public void addSanity(int amount) { setSanity(this.sanity + amount); }
    public void addDigestion(float amount) { setDigestion(this.digestion + amount); }

    public int getSpirituality() { return this.spirituality; }
    public void setSpirituality(int spirituality) { this.spirituality = clamp(spirituality, 0, this.maxSpirituality); }

    public int getMaxSpiritual() { return this.maxSpirituality; }
    public void setMaxSpirituality(int maxSpirituality) { this.maxSpirituality = maxSpirituality; }

    public void addSpirituality(int amount) { setSpirituality(this.spirituality + amount); }

    public String getCurrentPathway() { return this.currentPathway; }
    public void setCurrentPathway(String pathway) { this.currentPathway = pathway; }

    public int getCurrentSequence() { return this.currentSequence; }
    public void setCurrentSequence(int sequence) { this.currentSequence = sequence; }

    public int getSdcTicks() { return this.sdcTicks; }
    public void setSdcTicks(int ticks) { this.sdcTicks = ticks; }

    public List<String> getAbsorbedCharacteristics() { return this.absorbedCharacteristics; }
    public void setAbsorbedCharacteristics(List<String> list) {
        this.absorbedCharacteristics = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }
}