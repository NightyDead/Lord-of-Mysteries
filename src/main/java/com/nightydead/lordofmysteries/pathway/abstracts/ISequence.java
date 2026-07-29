package com.nightydead.lordofmysteries.pathway.abstracts;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

/**
 * 序列行为接口 - 定义每个非凡序列等级应具备的核心行为
 * 每个具体的序列实现（如 Seq9Seer）需实现此接口的所有方法
 * 序列号从 9（最低）到 0（最高），数字越小代表非凡位格越高
 */
public interface ISequence {

    /**
     * 获取当前序列编号
     *
     * @return 序列号（0-9，9 为最低序列，0 为最高序列）
     */
    int getSequenceNumber();

    /**
     * 获取该序列的本地化翻译键
     * 对应语言文件中的键名，用于显示序列名称
     *
     * @return 翻译键字符串（如 "sequence.lordofmysteries.fool.9"）
     */
    String getTranslationKey();

    /**
     * 玩家吸收该序列非凡特性/魔药时触发的回调
     * 用于设置被动能力、修改属性上限等晋升逻辑
     * <p>
     * <b>灵性上限由途径+序列双维度决定：</b>
     * 同序列号不同途径的灵性上限可以不同（例如愚者途径序列9设100，错误途径序列9可设80），
     * 各途径的序列实现类在此方法中各自调用 setMaxSpirituality() 设定专属值。
     *
     * @param player 晋升的玩家实体
     */
    void onAbsorbed(Player player);

    /**
     * 玩家失去该序列时触发的回调
     * 用于剥离被动能力、回退属性上限等降级逻辑
     *
     * @param player 失去序列的玩家实体
     */
    void onRemoved(Player player);

    /**
     * 每游戏刻（tick）执行的持续性逻辑
     * 用于实现序列专属的被动效果（如占卜家夜间自动获得夜视、小丑被动闪避等）
     * 仅在服务端执行，客户端应跳过
     *
     * @param player 当前持有该序列的玩家实体
     */
    void tick(Player player);
}
