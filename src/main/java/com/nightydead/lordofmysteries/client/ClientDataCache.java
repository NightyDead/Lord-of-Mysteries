package com.nightydead.lordofmysteries.client;

/**
 * 客户端非凡数据大本营
 * 汇集三大核心神秘学属性，专供 HUD 每帧提取渲染
 */
public class ClientDataCache {

    private static int sanity = 100;
    private static int spirituality = 0;
    private static int maxSpirituality = 0;
    private static float digestion = 0.0F;
    private static String pathway = "none";
    private static int sequence = 10;

    // 控制是否开启“灵视”界面的开关（未来可以用快捷键绑定切换）
    private static boolean spiritVisionActive = false;

    // --- 理智 (Sanity) ---
    public static void setSanity(int value) { sanity = value; }
    public static int getSanity() { return sanity; }

    // --- 灵性 (Spirituality) ---
    public static void setSpirituality(int value) { spirituality = value; }
    public static int getSpirituality() { return spirituality; }

    public static void setMaxSpirituality(int value) { maxSpirituality = value; }
    public static int getMaxSpirituality() { return maxSpirituality; }

    // --- 消化度 (Digestion) ---
    public static void setDigestion(float value) { digestion = value; }
    public static float getDigestion() { return digestion; }

    // --- 灵视状态 ---
    public static void setSpiritVisionActive(boolean active) { spiritVisionActive = active; }
    public static boolean isSpiritVisionActive() { return spiritVisionActive; }

    // （默认设置为凡人状态）
    public static void setPathway(String value) { pathway = value; }
    public static String getPathway() { return pathway; }

    public static void setSequence(int value) { sequence = value; }
    public static int getSequence() { return sequence; }
}