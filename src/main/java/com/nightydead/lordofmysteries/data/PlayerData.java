package com.nightydead.lordofmysteries.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家非凡数据类 - 存储并持久化玩家的全部神秘学核心属性
 */
public class PlayerData {

    /** Codec 编解码器：用于玩家非凡数据的序列化/反序列化，支持所有神秘学核心属性的持久化存储 */
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("sanity").forGetter(PlayerData::getSanity),
                    Codec.INT.fieldOf("max_sanity").forGetter(PlayerData::getMaxSanity),
                    Codec.INT.fieldOf("digestion").forGetter(PlayerData::getDigestion),
                    Codec.INT.fieldOf("spirituality").forGetter(PlayerData::getSpirituality),
                    Codec.INT.fieldOf("max_spirituality").forGetter(PlayerData::getMaxSpiritual),
                    Codec.STRING.fieldOf("currentPathway").forGetter(PlayerData::getCurrentPathway),
                    Codec.INT.fieldOf("currentSequence").forGetter(PlayerData::getCurrentSequence),
                    Codec.INT.fieldOf("sdcTicks").forGetter(PlayerData::getSdcTicks),
                    Codec.STRING.listOf().fieldOf("absorbedCharacteristics").forGetter(PlayerData::getAbsorbedCharacteristics),
                    Codec.BOOL.fieldOf("visionActive").forGetter(PlayerData::isVisionActive), // 👁️ 1. 注册 CODEC 编解码字段
                    Codec.INT.fieldOf("sameSeqStackCount").forGetter(PlayerData::getSameSeqStackCount) // 📚 同序列堆叠次数
            ).apply(instance, PlayerData::new)
    );

    /** 当前理智值 */
    private int sanity;
    /** 理智上限值 */
    private int maxSanity;
    /** 魔药消化进度（绝对值，上限由序列决定） */
    private int digestion;
    /** 当前灵性值 */
    private int spirituality;
    /** 灵性上限值 */
    private int maxSpirituality;
    /** 当前途径 ID（"none" 表示凡人） */
    private String currentPathway;
    /** 当前序列号（0~9，10表示凡人） */
    private int currentSequence;
    /** 失控倒计时 Tick 数（-1表示正常，>0表示倒计时中，-2表示已触发死亡） */
    private int sdcTicks;
    /** 已吸收的非凡特性历史记录列表（格式 "pathway:sequence"） */
    private List<String> absorbedCharacteristics;
    /** 👁️ 当前是否开启了灵视 */
    private boolean visionActive;
    /** 📚 同序列非凡特性堆叠次数（0=单份，1=两份，以此类推），影响灵性上限/消耗/能力/消化度上限/理智上限 */
    private int sameSeqStackCount;

    /** 初始凡人状态构造 */
    public PlayerData() {
        reset();
    }

    /** 重置所有超凡状态为凡人 */
    public void reset() {
        this.maxSanity = 100;
        this.sanity = 100;
        this.digestion = 0;
        this.maxSpirituality = 20;
        this.spirituality = 20;
        this.currentPathway = "none";
        this.currentSequence = 10;
        this.sdcTicks = -1;
        this.absorbedCharacteristics = new ArrayList<>();
        this.visionActive = false; // 👁️ 2. 重置时默认关闭灵视
        this.sameSeqStackCount = 0; // 📚 重置时归零堆叠次数
    }

    /** 全参数构造函数（Codec 反序列化与深拷贝底层驱动） */
    public PlayerData(int sanity, int maxSanity, int digestion, int spirituality, int maxSpirituality,
                      String currentPathway, int currentSequence, int sdcTicks, List<String> absorbedCharacteristics,
                      boolean visionActive, int sameSeqStackCount) { // 👁️ 3. 构造函数追加灵视参数 // 📚 堆叠次数
        this.maxSanity = maxSanity;
        this.maxSpirituality = maxSpirituality;
        this.sanity = Math.clamp(sanity, 0, maxSanity);
        this.currentSequence = currentSequence;
        this.sameSeqStackCount = sameSeqStackCount; // 📚 先赋值堆叠次数，后续消化度裁剪需要用到
        this.digestion = Math.clamp(digestion, 0, getEffectiveMaxDigestion());
        this.spirituality = Math.clamp(spirituality, 0, maxSpirituality);
        this.currentPathway = currentPathway;
        this.sdcTicks = sdcTicks;
        this.absorbedCharacteristics = new ArrayList<>(absorbedCharacteristics);
        this.visionActive = visionActive; // 👁️ 4. 赋值字段
    }

    // ==================== 神秘学辅助工具方法 ====================

    /**
     * 添加一条非凡特性吸收记录
     *
     * @param pathway  途径 ID
     * @param sequence 序列号
     */
    public void addAbsorbedRecord(String pathway, int sequence) {
        this.absorbedCharacteristics.add(pathway + ":" + sequence);
    }

    /** 深拷贝当前玩家数据实例 */
    public PlayerData copy() {
        return new PlayerData(this.sanity, this.maxSanity, this.digestion, this.spirituality, this.maxSpirituality,
                this.currentPathway, this.currentSequence, this.sdcTicks, this.absorbedCharacteristics, this.visionActive,
                this.sameSeqStackCount); // 👁️ 5. 拷贝时保留灵视状态 // 📚 保留堆叠次数
    }

    /** 解析已吸收的非凡特性历史记录为结构化对象列表 */
    public List<CharacteristicRecord> getParsedCharacteristics() {
        return this.absorbedCharacteristics.stream().map(s -> {
            String[] p = s.split(":");
            return new CharacteristicRecord(p[0], Integer.parseInt(p[1]));
        }).toList();
    }

    /** 非凡特性记录 - 存储途径 ID 和序列号的不可变记录类 */
    public record CharacteristicRecord(String pathway, int sequence) {}

    // ==================== Getters & Setters ====================

    /** 获取当前理智值 */
    public int getSanity() { return this.sanity; }
    /** 设置当前理智值，自动裁剪到合法范围 */
    public void setSanity(int sanity) { this.sanity = Math.clamp(sanity, 0, this.maxSanity); }
    /** 获取理智上限值 */
    public int getMaxSanity() { return this.maxSanity; }
    /** 设置理智上限值 */
    public void setMaxSanity(int maxSanity) { this.maxSanity = maxSanity; }

    /** 获取魔药消化进度（绝对值） */
    public int getDigestion() { return this.digestion; }
    /** 设置魔药消化进度，自动裁剪到 [0, 当前序列有效上限] 范围（含堆叠倍率） */
    public void setDigestion(int digestion) { this.digestion = Math.clamp(digestion, 0, getEffectiveMaxDigestion()); }

    /** 增加理智值（可为负数） */
    public void addSanity(int amount) { setSanity(this.sanity + amount); }
    /** 增加消化进度（可为负数） */
    public void addDigestion(int amount) { setDigestion(this.digestion + amount); }

    // ==================== 消化度工具方法 ====================

    /**
     * 获取当前消化进度相对于当前序列上限的比例（0.0F~1.0F）
     * 用于晋升成功率计算等需要归一化消化度的场景
     */
    public float getDigestionRatio() {
        int max = getEffectiveMaxDigestion();
        return max > 0 ? Math.clamp((float) this.digestion / max, 0.0F, 1.0F) : 0.0F;
    }

    /**
     * 根据序列号计算该序列的魔药消化度上限
     * 指数增长：序列9 = 100，序列0 = 10000
     * 公式：100 × 100^((9-seq)/9)
     *
     * @param sequence 序列号（0~9，10表示凡人，返回0）
     * @return 该序列的消化度上限
     */
    public static int getMaxDigestion(int sequence) {
        if (sequence >= 10) return 0;
        if (sequence < 0) sequence = 0;
        return (int) Math.round(100 * Math.pow(100, (9 - sequence) / 9.0));
    }

    /**
     * 占卜成功固定增加的消化度
     *
     * @param sequence 当前序列号（保留参数以兼容调用方）
     * @return 始终返回 1
     */
    public static int getDigestionGain(int sequence) {
        return 1;
    }

    /** 获取当前灵性值 */
    public int getSpirituality() { return this.spirituality; }
    /** 设置当前灵性值，自动裁剪到合法范围 */
    public void setSpirituality(int spirituality) { this.spirituality = Math.clamp(spirituality, 0, this.maxSpirituality); }

    /** 获取灵性上限值 */
    public int getMaxSpiritual() { return this.maxSpirituality; }
    /** 设置灵性上限值 */
    public void setMaxSpirituality(int maxSpirituality) { this.maxSpirituality = maxSpirituality; }

    /** 增加灵性值（可为负数） */
    public void addSpirituality(int amount) { setSpirituality(this.spirituality + amount); }

    /** 获取当前途径 ID */
    public String getCurrentPathway() { return this.currentPathway; }
    /** 设置当前途径 ID */
    public void setCurrentPathway(String pathway) { this.currentPathway = pathway; }

    /** 获取当前序列号 */
    public int getCurrentSequence() { return this.currentSequence; }
    /** 设置当前序列号 */
    public void setCurrentSequence(int sequence) { this.currentSequence = sequence; }

    /** 获取失控倒计时 Tick数 */
    public int getSdcTicks() { return this.sdcTicks; }
    /** 设置失控倒计时 Tick数 */
    public void setSdcTicks(int ticks) { this.sdcTicks = ticks; }

    /** 获取已吸收的非凡特性历史记录列表 */
    public List<String> getAbsorbedCharacteristics() { return this.absorbedCharacteristics; }
    /** 设置已吸收的非凡特性历史记录列表 */
    public void setAbsorbedCharacteristics(List<String> list) {
        this.absorbedCharacteristics = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /** 👁️ 获取当前是否激活了灵视 */
    public boolean isVisionActive() { return this.visionActive; }
    /** 👁️ 设置当前是否激活了灵视 */
    public void setVisionActive(boolean active) { this.visionActive = active; }

    // ==================== 📚 同序列堆叠系统 ====================

    /** 获取同序列非凡特性堆叠次数（0=单份，1=两份，以此类推） */
    public int getSameSeqStackCount() { return this.sameSeqStackCount; }

    /** 设置同序列非凡特性堆叠次数 */
    public void setSameSeqStackCount(int count) { this.sameSeqStackCount = Math.max(0, count); }

    /** 堆叠后的总份数（≥1） */
    public int getTotalCopies() { return this.sameSeqStackCount + 1; }

    /**
     * 获取堆叠后的有效消化度上限
     * 基础消化度上限 × 总份数（消化度翻倍，能力增强但更难消化）
     */
    public int getEffectiveMaxDigestion() {
        return getMaxDigestion(this.currentSequence) * getTotalCopies();
    }

    /**
     * 获取堆叠后的灵性消耗倍率
     * 每多一份特性减少10%消耗（例：1份=1.0，2份=0.9，3份=0.8）
     */
    public float getStackCostMultiplier() {
        return Math.max(0.1F, 1.0F - 0.1F * this.sameSeqStackCount);
    }

    /**
     * 获取堆叠后的能力增强倍率
     * 每多一份特性增强10%能力（例：1份=1.0，2份=1.1，3份=1.2）
     */
    public float getStackPowerMultiplier() {
        return 1.0F + 0.1F * this.sameSeqStackCount;
    }
}