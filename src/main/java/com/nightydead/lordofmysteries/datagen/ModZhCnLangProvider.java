package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 中文语言文件数据提供者
 * 负责生成 zh_cn.json 语言文件，定义所有模组内容的简体中文翻译
 * 包括物品名称、方块名称、创造模式标签页名称等
 */
public class ModZhCnLangProvider extends LanguageProvider {

    /**
     * 构造中文语言文件提供者
     *
     * @param output 数据包输出目录
     */
    public ModZhCnLangProvider(PackOutput output) {
        super(output, LordofMysteries.MODID, "zh_cn");
    }

    /**
     * 注册所有中文翻译条目
     * 按类别分组：非凡特性、魔药主材、方块、创造模式标签页
     */
    @Override
    protected void addTranslations() {
        // 非凡特性物品
        add(ModItems.AGGREGATED_CHARACTERISTIC.get(), "聚合的非凡特性");
        add(ModItems.SEER_CHARACTERISTIC.get(), "占卜家非凡特性");
        add(ModItems.CLOWN_CHARACTERISTIC.get(), "小丑非凡特性");
        add(ModItems.MAGICIAN_CHARACTERISTIC.get(), "魔术师非凡特性");
        add(ModItems.FACELESS_CHARACTERISTIC.get(), "无面人非凡特性");
        add(ModItems.MARIONETTIST_CHARACTERISTIC.get(), "秘偶大师非凡特性");
        add(ModItems.BIZARRO_SORCERER_CHARACTERISTIC.get(), "诡法师非凡特性");
        add(ModItems.SCHOLAR_OF_YORE_CHARACTERISTIC.get(), "古代学者非凡特性");
        add(ModItems.MIRACLE_INVOKER_CHARACTERISTIC.get(), "奇迹师非凡特性");
        add(ModItems.ATTENDANT_OF_MYSTERIES_CHARACTERISTIC.get(), " 诡秘侍者非凡特性");

        // 魔药主材与特殊物品
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "拉瓦章鱼血液");
        add(ModItems.STAR_CRYSTAL.get(), "星水晶");
        add(ModItems.PURE_WATER.get(), "纯水");
        add(ModItems.SEER_POTION.get(), "占卜家魔药");
        add(ModItems.RITUAL_DAGGER.get(), "仪式匕首");

        // 魔药辅助材料
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "夜香草汁液");
        add(ModItems.GOLD_MINT_LEAF.get(), "金薄荷叶");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "毒堇汁");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "龙血草粉末");

        // 方块
        add(ModBlocks.EXAMPLE_BLOCK.get(), " 示例方块");
        // 神秘学草药方块
        add(ModBlocks.NIGHT_PERFUME_HERB.get(), "夜香草");
        add(ModBlocks.GOLD_MINT_HERB.get(), "金薄荷");
        add(ModBlocks.POISON_HEMLOCK_HERB.get(), "毒堇");
        add(ModBlocks.DRAGON_BLOOD_HERB.get(), "龙血草");
        // 炼药锅
        add(ModBlocks.ALCHEMY_CAULDRON.get(), "炼药锅");

        // 炼药锅交互消息
        add("message.lordofmysteries.cauldron.item_added", "§a[炼药锅] §f材料已放入。");
        add("message.lordofmysteries.cauldron.item_removed", "§e[炼药锅] §f材料已取出。");
        add("message.lordofmysteries.cauldron.brew.success", "§6[炼药锅] §a炼制成功！魔药正在成形...");
        add("message.lordofmysteries.cauldron.brew.failed", "§c[炼药锅] §4炼制失败！材料已被污染扭曲...");
        add("message.lordofmysteries.cauldron.brew.penalty", "§4[炼药锅] §c失败的炼制涌出混乱的能量，抽取了你的生命与理智！");
        add("message.lordofmysteries.cauldron.potion_taken", "§a[炼药锅] §f你从锅中取出了魔药。");
        add("message.lordofmysteries.cauldron.characteristic_taken", "§e[炼药锅] §f你取出了聚合的非凡特性。");
        add("message.lordofmysteries.cauldron.use_bottle", "§e[炼药锅] §f请使用玻璃瓶来收集魔药。");
        add("message.lordofmysteries.cauldron.need_beyonder", "§c[炼药锅] §4只有非凡者才能注入灵性触发炼制。（Shift + 空手右键）");
        add("message.lordofmysteries.cauldron.need_spirituality", "§c[炼药锅] §4灵性不足！你至少需要 5 点灵性。");
        add("message.lordofmysteries.cauldron.brewed_block", "§e[炼药锅] §f炼药锅已完成炼制，请先取出结果。");

        // 仪式匕首消息
        add("message.lordofmysteries.ritual_dagger.cauldron_brew", "§5[仪式匕首] §f你通过匕首将灵性注入到炼药锅中...");

        // 失败酿造 Tooltip
        add("tooltip.lordofmysteries.characteristic.failed_brew_title", "--- 炼制失败：魔药主材 ---");
        add("tooltip.lordofmysteries.characteristic.failed_brew_material", "主材来源: %s（序列 %d）");

        // 创造模式标签页
        add("itemGroup.characteristic_tab", "非凡特性");
        add("itemGroup.potion_tab", "魔药");
        add("itemGroup.mod_block_tab", "模组方块");
        add("itemGroup.potion_main_material_tab", "魔药主材");
        add("itemGroup.potion_auxiliary_material_tab", "魔药辅助材");
    }
}
