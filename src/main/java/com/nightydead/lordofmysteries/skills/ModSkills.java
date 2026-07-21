package com.nightydead.lordofmysteries.skills;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 模组技能注册中心
 * 定义所有可用的主动技能及其解锁条件，供技能轮盘 UI 查询使用
 * <p>
 * 凡人（无途径）→ 返回空列表，轮盘显示灰色空盘 + "凡人之躯"提示
 * 占卜家（愚者途径·序列9）→ 返回占卜技能
 * 灵视为通用技能，由 V 键直接切换，不走轮盘
 */
public class ModSkills {

    /** 技能唯一标识 — 占卜 */
    public static final String ID_DIVINATION = "divination";

    /**
     * 技能条目：封装技能 ID、显示名称、扇区颜色
     */
    public record SkillEntry(String id, Component name, int color) {}

    /** 占卜 — 淡金色调 */
    public static final SkillEntry DIVINATION = new SkillEntry(
            ID_DIVINATION,
            Component.translatable("skill.lordofmysteries.divination"),
            0xFFCCAA44
    );

    /**
     * 根据玩家当前途径和序列号，返回可用技能列表
     *
     * @param pathway  途径 ID（如 "fool"），null 或 "none" 表示凡人
     * @param sequence 序列号（0~9 非凡者，10 凡人）
     * @return 可用技能列表，凡人或未实现途径返回空列表
     */
    public static List<SkillEntry> getAvailableSkills(String pathway, int sequence) {
        if (pathway == null || pathway.equals("none") || sequence >= 10) {
            return List.of();
        }
        if (pathway.equals("fool") && sequence <= 9) {
            var skills = new ArrayList<SkillEntry>();
            skills.add(DIVINATION);
            return skills;
        }
        return List.of();
    }
}
