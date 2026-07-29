package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.item.ModItems.PathwayDef;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Map;

/**
 * 英文语言文件数据提供者
 * 负责生成 en_us.json 语言文件，定义所有模组内容的英文翻译
 * 包括物品名称、方块名称、创造模式标签页名称等
 */
public class ModEnUsLangProvider extends LanguageProvider {

    /**
     * 构造英文语言文件提供者
     *
     * @param output 数据包输出目录
     */
    public ModEnUsLangProvider(PackOutput output) {
        super(output, LordofMysteries.MODID, "en_us");
    }

    /**
     * 注册所有英文翻译条目
     * 按类别分组：非凡特性、魔药主材、方块、创造模式标签页
     */
    @Override
    protected void addTranslations() {
        // 聚合非凡特性
        add(ModItems.AGGREGATED_CHARACTERISTIC.get(), "Aggregated Characteristic");

        // 批量生成全途径英文翻译
        for (Map.Entry<String, PathwayDef> pwEntry : ModItems.getPathwayData().entrySet()) {
            PathwayDef def = pwEntry.getValue();
            String[] engNames = def.engNames();
            for (int seq = 9; seq >= 0; seq--) {
                int idx = 9 - seq;
                String eng = engNames[idx];
                String displayName = toDisplayName(eng);
                // 非凡特性: "Seer Characteristic"
                Item characteristic = ModItems.getPureCharacteristic(def.pathwayId(), seq);
                if (characteristic != null) {
                    add(characteristic, displayName + " Characteristic");
                }
                // 魔药: "Seer Potion"
                Item potion = ModItems.getPurePotion(def.pathwayId(), seq);
                if (potion != null) {
                    add(potion, displayName + " Potion");
                }
            }
        }

        // 魔药主材与特殊物品
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "Lava Octopus Blood");
        add(ModItems.STAR_CRYSTAL.get(), "Star Crystal");
        add(ModItems.PURE_WATER.get(), "Pure Water");
        add(ModItems.RITUAL_DAGGER.get(), "Ritual Dagger");

        // 魔药辅助材料
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "Night Perfume Juice");
        add(ModItems.GOLD_MINT_LEAF.get(), "Gold Mint Leaf");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "Poison Hemlock Juice");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "Dragon Blood Powder");

        // 方块
        add(ModBlocks.EXAMPLE_BLOCK.get(), "Example Block");
        // 神秘学草药方块
        add(ModBlocks.NIGHT_PERFUME_HERB.get(), "Night Perfume Herb");
        add(ModBlocks.GOLD_MINT_HERB.get(), "Gold Mint Herb");
        add(ModBlocks.POISON_HEMLOCK_HERB.get(), "Poison Hemlock");
        add(ModBlocks.DRAGON_BLOOD_HERB.get(), "Dragon Blood Herb");
        // 炼药锅
        add(ModBlocks.ALCHEMY_CAULDRON.get(), "Alchemy Cauldron");

        // 炼药锅交互消息
        add("message.lordofmysteries.cauldron.item_added", "§a[Alchemy Cauldron] §fMaterial added.");
        add("message.lordofmysteries.cauldron.item_removed", "§e[Alchemy Cauldron] §fMaterial removed.");
        add("message.lordofmysteries.cauldron.brew.success", "§6[Alchemy Cauldron] §aBrewing succeeded! A potion is forming...");
        add("message.lordofmysteries.cauldron.brew.failed", "§c[Alchemy Cauldron] §4Brewing failed! The materials have been corrupted...");
        add("message.lordofmysteries.cauldron.brew.penalty", "§4[Alchemy Cauldron] §cThe failed brew surges with chaotic energy, draining your vitality and sanity!");
        add("message.lordofmysteries.cauldron.potion_taken", "§a[Alchemy Cauldron] §fYou collected the potion from the cauldron.");
        add("message.lordofmysteries.cauldron.characteristic_taken", "§e[Alchemy Cauldron] §fYou retrieved the aggregated characteristic.");
        add("message.lordofmysteries.cauldron.use_bottle", "§e[Alchemy Cauldron] §fUse a glass bottle to collect the potion.");
        add("message.lordofmysteries.cauldron.need_beyonder", "§c[Alchemy Cauldron] §4Only a Beyonder can infuse spirituality to trigger brewing. (Shift + Right-click with empty hand)");
        add("message.lordofmysteries.cauldron.need_spirituality", "§c[Alchemy Cauldron] §4Not enough spirituality! You need at least 5 points.");
        add("message.lordofmysteries.cauldron.brewed_block", "§e[Alchemy Cauldron] §fThe cauldron has finished brewing. Collect the result first.");

        // 仪式匕首消息
        add("message.lordofmysteries.ritual_dagger.cauldron_brew", "§5[Ritual Dagger] §fYou channel spirituality through the dagger into the cauldron...");

        // 失败酿造 Tooltip
        add("tooltip.lordofmysteries.characteristic.failed_brew_title", "--- Failed Brew: Magic Main Material ---");
        add("tooltip.lordofmysteries.characteristic.failed_brew_material", "Main Material: %s (Seq %d)");

        // 创造模式标签页
        add("itemGroup.characteristic_tab", "Characteristic");
        add("itemGroup.potion_tab", "Potion");
        add("itemGroup.mod_block_tab", "Block");
        add("itemGroup.potion_material_tab", "Potion Material");
        add("itemGroup.potion_auxiliary_material_tab", "Potion Auxiliary Material");
    }

    /**
     * 将 snake_case 英文名转换为首字母大写的显示名称
     * 如 "bizarro_sorcerer" → "Bizarro Sorcerer"
     */
    private static String toDisplayName(String snakeCase) {
        StringBuilder sb = new StringBuilder();
        for (String word : snakeCase.split("_")) {
            if (!word.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }
}
