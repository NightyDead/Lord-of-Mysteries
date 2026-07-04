package com.nightydead.lordofmysteries.client;

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

    // ==================== 理智 (Sanity) ====================

    public static void setSanity(int value) { sanity = value; }
    public static int getSanity() { return sanity; }

    // ==================== 灵性 (Spirituality) ====================

    public static void setSpirituality(int value) { spirituality = value; }
    public static int getSpirituality() { return spirituality; }

    public static void setMaxSpirituality(int value) { maxSpirituality = value; }
    public static int getMaxSpirituality() { return maxSpirituality; }

    // ==================== 消化度 (Digestion) ====================

    public static void setDigestion(float value) { digestion = value; }
    public static float getDigestion() { return digestion; }

    // ==================== 灵视状态 (Spirit Vision) ====================

    public static void setSpiritVisionActive(boolean active) { spiritVisionActive = active; }
    public static boolean isSpiritVisionActive() { return spiritVisionActive; }

    // ==================== 途径与序列 (Pathway & Sequence) ====================

    public static void setPathway(String value) { pathway = value; }
    public static String getPathway() { return pathway; }

    public static void setSequence(int value) { sequence = value; }
    public static int getSequence() { return sequence; }
}
