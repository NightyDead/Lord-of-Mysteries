package com.nightydead.lordofmysteries.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 客户端非凡数据缓存中心
 * 存储从服务端通过网络包同步过来的神秘学属性数据
 * 专供 HUD 渲染器（{@link ModHUDOverlay}）每帧提取使用
 * <p>
 * 所有字段均为 static，因为客户端只有一个玩家，无需多实例管理
 */
public class ClientDataCache {

    /** 当前理智值，范围 0~100 */
    private static int sanity = 100;
    /** 当前灵性值 */
    private static int spirituality = 0;
    /** 灵性上限值 */
    private static int maxSpirituality = 0;
    /** 魔药消化进度，范围 0.0F~1.0F（1.0F 表示完全消化） */
    private static float digestion = 0.0F;
    /** 当前途径 ID（如 "fool"），"none" 表示凡人 */
    private static String pathway = "none";
    /** 当前序列号（0~9，数字越小等级越高），10 表示凡人 */
    private static int sequence = 10;

    /** 灵视界面开关（预留功能，未来可绑定快捷键切换） */
    private static boolean spiritVisionActive = false;

    /** 已学魔药配方集合，格式 "pathway:sequence" */
    private static Set<String> learnedRecipes = new HashSet<>();

    // ==================== 理智 (Sanity) ====================

    /** 设置当前理智值 */
    public static void setSanity(int value) { sanity = value; }
    /** 获取当前理智值 */
    public static int getSanity() { return sanity; }

    // ==================== 灵性 (Spirituality) ====================

    /** 设置当前灵性值 */
    public static void setSpirituality(int value) { spirituality = value; }
    /** 获取当前灵性值 */
    public static int getSpirituality() { return spirituality; }

    /** 设置灵性上限值 */
    public static void setMaxSpirituality(int value) { maxSpirituality = value; }
    /** 获取灵性上限值 */
    public static int getMaxSpirituality() { return maxSpirituality; }

    // ==================== 消化度 (Digestion) ====================

    /** 设置魔药消化进度 */
    public static void setDigestion(float value) { digestion = value; }
    /** 获取魔药消化进度 */
    public static float getDigestion() { return digestion; }

    // ==================== 灵视状态 (Spirit Vision) ====================

    /** 设置灵视界面开关状态 */
    public static void setSpiritVisionActive(boolean active) { spiritVisionActive = active; }
    /** 获取灵视界面是否激活 */
    public static boolean isSpiritVisionActive() { return spiritVisionActive; }

    // ==================== 途径与序列 (Pathway & Sequence) ====================

    /** 设置当前途径 ID */
    public static void setPathway(String value) { pathway = value; }
    /** 获取当前途径 ID */
    public static String getPathway() { return pathway; }

    /** 设置当前序列号 */
    public static void setSequence(int value) { sequence = value; }
    /** 获取当前序列号 */
    public static int getSequence() { return sequence; }

    // ==================== 已学配方 (Learned Recipes) ====================

    /** 设置已学配方集合 */
    public static void setLearnedRecipes(Set<String> recipes) { learnedRecipes = new HashSet<>(recipes); }
    /** 获取已学配方集合的不可变视图 */
    public static Set<String> getLearnedRecipes() { return Collections.unmodifiableSet(learnedRecipes); }
    /** 检查是否已学习某个配方 */
    public static boolean hasLearnedRecipe(String pathway, int sequence) {
        return learnedRecipes.contains(pathway + ":" + sequence);
    }
}
