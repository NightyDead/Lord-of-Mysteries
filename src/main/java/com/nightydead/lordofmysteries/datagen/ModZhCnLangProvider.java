package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModZhCnLangProvider extends LanguageProvider {
    public ModZhCnLangProvider(PackOutput output) {
        super(output, LordofMysteries.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
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
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "拉瓦章鱼血液");
        add(ModItems.STAR_CRYSTAL.get(), "星水晶");
        add(ModItems.PURE_WATER.get(), "纯水");
        add(ModItems.SEER_POTION.get(), "占卜家魔药");
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "夜香果汁");
        add(ModItems.GOLD_MINT_LEAF.get(), "金薄荷叶");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "毒芹果汁");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "龙血粉");

        add(ModBlocks.EXAMPLE_BLOCK.get(), " 示例方块");

        // 添加这四个植物方块的中文翻译
        add(ModBlocks.NIGHT_PERFUME_HERB.get(), "夜香草");
        add(ModBlocks.GOLD_MINT_HERB.get(), "金薄荷");
        add(ModBlocks.POISON_HEMLOCK_HERB.get(), "毒堇");
        add(ModBlocks.DRAGON_BLOOD_HERB.get(), "龙血草");

        add("itemGroup.characteristic_tab", "非凡特性");
        add("itemGroup.potion_tab", "魔药");
        add("itemGroup.mod_block_tab", "模组方块");
        add("itemGroup.potion_main_material_tab", "魔药主材");
        add("itemGroup.potion_auxiliary_material_tab", "魔药辅助材");
    }
}
