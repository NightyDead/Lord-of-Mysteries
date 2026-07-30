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
 * 负责生成完整的 en_us.json 语言文件，涵盖所有模组内容的英文翻译
 * <p>
 * 包括：物品/方块名称、途径/序列名称、HUD 文本、UI 界面、
 * 炼药锅消息、非凡者系统消息、结构名称、按键绑定等
 */
public class ModEnUsLangProvider extends LanguageProvider {

    /** 途径 ID → 英文显示名称映射 */
    private static final Map<String, String> PATHWAY_EN = Map.ofEntries(
            Map.entry("fool", "The Fool"),
            Map.entry("error", "The Error"),
            Map.entry("door", "The Door"),
            Map.entry("visionary", "Visionary"),
            Map.entry("hanged_man", "The Hanged Man"),
            Map.entry("tyrant", "Tyrant"),
            Map.entry("sun", "The Sun"),
            Map.entry("white_tower", "White Tower"),
            Map.entry("hermit", "The Hermit"),
            Map.entry("paragon", "Paragon"),
            Map.entry("darkness", "Darkness"),
            Map.entry("death", "Death"),
            Map.entry("twilight_giant", "Twilight Giant"),
            Map.entry("red_priest", "Red Priest"),
            Map.entry("demoness", "Demoness"),
            Map.entry("abyss", "Abyss"),
            Map.entry("chained", "Chained"),
            Map.entry("moon", "The Moon"),
            Map.entry("mother", "Mother Earth"),
            Map.entry("justiciar", "Justiciar"),
            Map.entry("black_emperor", "Black Emperor"),
            Map.entry("wheel_of_fortune", "Wheel of Fortune")
    );

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
     */
    @Override
    protected void addTranslations() {
        // ==================== 聚合并凡特性 ====================
        add(ModItems.AGGREGATED_CHARACTERISTIC.get(), "Aggregated Characteristic");

        // ==================== 途径名称 + 序列名称 + 非凡特性 + 魔药（通过数据驱动批量生成）====================
        for (Map.Entry<String, PathwayDef> pwEntry : ModItems.getPathwayData().entrySet()) {
            PathwayDef def = pwEntry.getValue();
            String pathwayId = def.pathwayId();
            String[] engNames = def.engNames();

            // 途径显示名称
            String pathwayDisplay = PATHWAY_EN.getOrDefault(pathwayId, toDisplayName(pathwayId));
            add("pathway.lordofmysteries." + pathwayId, pathwayDisplay);

            for (int seq = 9; seq >= 0; seq--) {
                int idx = 9 - seq;
                String eng = engNames[idx];
                String displayName = toDisplayName(eng);

                // 序列显示名称
                add("sequence.lordofmysteries." + pathwayId + "." + seq, displayName);

                // 非凡特性
                Item characteristic = ModItems.getPureCharacteristic(pathwayId, seq);
                if (characteristic != null) {
                    add(characteristic, displayName + " Characteristic");
                }
                // 魔药
                Item potion = ModItems.getPurePotion(pathwayId, seq);
                if (potion != null) {
                    add(potion, displayName + " Potion");
                }
            }
        }

        // 额外途径（未在 PathwayDef 中注册的）
        add("pathway.lordofmysteries.none", "None");
        add("pathway.lordofmysteries.morgany", "Mother Tree of Desire");

        // ==================== 魔药配方物品 ====================
        add(ModItems.POTION_RECIPE.get(), "Potion Recipe");

        // ==================== 魔药主材与特殊物品 ====================
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "Lava Octopus Blood");
        add(ModItems.STAR_CRYSTAL.get(), "Star Crystal");
        add(ModItems.PURE_WATER.get(), "Pure Water");
        add(ModItems.RITUAL_DAGGER.get(), "Ritual Dagger");
        add(ModItems.MYSTIC_DUST.get(), "Mystic Dust");
        add("item.lordofmysteries.pure_water.pure_water.effect.empty", "Pure Water");
        add("tooltip.lordofmysteries.pure_water.desc", "The base solvent for potion brewing, pure and impurity-free.");

        // ==================== 魔药辅助材料 ====================
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "Night Perfume Juice");
        add(ModItems.GOLD_MINT_LEAF.get(), "Gold Mint Leaf");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "Poison Hemlock Juice");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "Dragon Blood Powder");

        // ==================== 方块 ====================
        add(ModBlocks.EXAMPLE_BLOCK.get(), "Example Block");
        add(ModBlocks.NIGHT_PERFUME_HERB.get(), "Night Perfume Herb");
        add(ModBlocks.GOLD_MINT_HERB.get(), "Gold Mint Herb");
        add(ModBlocks.POISON_HEMLOCK_HERB.get(), "Poison Hemlock");
        add(ModBlocks.DRAGON_BLOOD_HERB.get(), "Dragon Blood Herb");
        add(ModBlocks.ALCHEMY_CAULDRON.get(), "Alchemy Cauldron");
        add(ModBlocks.RITUAL_ALTAR.get(), "Ritual Altar");

        // ==================== 创造模式标签页 ====================
        add("itemGroup.characteristic_tab", "Characteristic");
        add("itemGroup.potion_tab", "Potion");
        add("itemGroup.mod_block_tab", "Block");
        add("itemGroup.potion_main_material_tab", "Potion Main Material");
        add("itemGroup.potion_auxiliary_materials_tab", "Potion Auxiliary Materials");
        add("itemGroup.natural_item_tab", "Natural Item");
        add("itemGroup.recipe_tab", "Potion Recipes");

        // ==================== 炼药锅交互消息 ====================
        add("message.lordofmysteries.cauldron.item_added", "§a[Alchemy Cauldron] §fMaterial added.");
        add("message.lordofmysteries.cauldron.item_removed", "§e[Alchemy Cauldron] §fMaterial removed.");
        add("message.lordofmysteries.cauldron.brew.success", "§6[Alchemy Cauldron] §aBrewing succeeded! A potion is forming...");
        add("message.lordofmysteries.cauldron.brew.failed", "§c[Alchemy Cauldron] §4Brewing failed! The materials have been corrupted...");
        add("message.lordofmysteries.cauldron.brew.failed_no_main", "§7[Alchemy Cauldron] Brewing failed, but no main materials were involved. The materials have dissipated.");
        add("message.lordofmysteries.cauldron.brew.penalty", "§4[Alchemy Cauldron] §cThe failed brew surges with chaotic energy, draining your vitality and sanity!");
        add("message.lordofmysteries.cauldron.potion_taken", "§a[Alchemy Cauldron] §fYou collected the potion from the cauldron.");
        add("message.lordofmysteries.cauldron.characteristic_taken", "§e[Alchemy Cauldron] §fYou retrieved the aggregated characteristic.");
        add("message.lordofmysteries.cauldron.use_bottle", "§e[Alchemy Cauldron] §fUse a glass bottle to collect the potion.");
        add("message.lordofmysteries.cauldron.need_beyonder", "§c[Alchemy Cauldron] §4Only a Beyonder can infuse spirituality to trigger brewing. (Shift + Right-click with empty hand)");
        add("message.lordofmysteries.cauldron.need_spirituality", "§c[Alchemy Cauldron] §4Not enough spirituality! You need at least 5 points.");
        add("message.lordofmysteries.cauldron.brewed_block", "§e[Alchemy Cauldron] §fThe cauldron has finished brewing. Collect the result first.");

        // ==================== 仪式匕首消息 ====================
        add("message.lordofmysteries.ritual_dagger.cauldron_brew", "§5[Ritual Dagger] §fYou channel spirituality through the dagger into the cauldron...");

        // ==================== 配方学习消息 ====================
        add("message.lordofmysteries.recipe.learned", "§a[Recipe] §fYou learned the potion recipe for %s Pathway · Sequence %d %s!");
        add("message.lordofmysteries.recipe.already_known", "§7[Recipe] You have already mastered this knowledge.");

        // ==================== HUD 显示文本 ====================
        add("hud.lordofmysteries.identity.mortal", "Mortal");
        add("hud.lordofmysteries.identity.format", "%s Pathway (Sequence %s)");
        add("hud.lordofmysteries.sanity.text", "Sanity: %s / 100");
        add("hud.lordofmysteries.spirituality.text", "Spirituality: %s / %s");
        add("hud.lordofmysteries.digestion.text", "Potion Digestion: %s / %s");

        // ==================== 按键绑定 ====================
        add("key.categories.lordofmysteries", "Lord of Mysteries - Powers");
        add("key.lordofmysteries.toggle_vision", "Toggle Spirit Vision");
        add("key.lordofmysteries.skill_wheel", "Skill Wheel");
        add("key.lordofmysteries.knowledge_panel", "Knowledge Panel");

        // ==================== 技能 ====================
        add("skill.lordofmysteries.spirit_vision", "Spirit Vision");
        add("skill.lordofmysteries.divination", "Divination");
        add("skill.lordofmysteries.wheel.mortal_hint", "Mortal");

        // ==================== UI 界面 ====================
        add("screen.lordofmysteries.biome_divination", "Biome Divination");
        add("screen.lordofmysteries.biome_divination.search", "Search biomes…");
        add("screen.lordofmysteries.biome_divination.hint", "↑↓ Select  Enter Confirm  Esc Cancel");
        add("screen.lordofmysteries.structure_divination", "Structure Divination");
        add("screen.lordofmysteries.structure_divination.search", "Search structures…");
        add("screen.lordofmysteries.structure_divination.hint", "↑↓ Select  Enter Confirm  Esc Cancel");
        add("screen.lordofmysteries.structure_divination.loading", "Loading structure list…");
        add("screen.lordofmysteries.knowledge", "Knowledge Panel");
        add("screen.lordofmysteries.knowledge.mortal", "§7You have not yet touched the threshold of the supernatural");
        add("screen.lordofmysteries.knowledge.learned_recipes", "--- Learned Potion Recipes ---");
        add("screen.lordofmysteries.knowledge.current_identity", "--- Current Identity ---");
        add("screen.lordofmysteries.knowledge.skills", "--- Mastered Skills ---");
        add("screen.lordofmysteries.knowledge.main_materials", "Main Materials:");
        add("screen.lordofmysteries.knowledge.aux_materials", "Auxiliary Materials:");
        add("screen.lordofmysteries.knowledge.acquisition", "Acquisition:");

        // ==================== Minecraft 结构名称翻译 ====================
        add("structure.minecraft.pillager_outpost", "Pillager Outpost");
        add("structure.minecraft.mineshaft", "Mineshaft");
        add("structure.minecraft.mineshaft_mesa", "Mesa Mineshaft");
        add("structure.minecraft.woodland_mansion", "Woodland Mansion");
        add("structure.minecraft.mansion", "Woodland Mansion");
        add("structure.minecraft.jungle_pyramid", "Jungle Pyramid");
        add("structure.minecraft.desert_pyramid", "Desert Pyramid");
        add("structure.minecraft.igloo", "Igloo");
        add("structure.minecraft.shipwreck", "Shipwreck");
        add("structure.minecraft.shipwreck_beached", "Beached Shipwreck");
        add("structure.minecraft.swamp_hut", "Swamp Hut");
        add("structure.minecraft.stronghold", "Stronghold");
        add("structure.minecraft.monument", "Ocean Monument");
        add("structure.minecraft.ocean_ruin_cold", "Cold Ocean Ruin");
        add("structure.minecraft.ocean_ruin_warm", "Warm Ocean Ruin");
        add("structure.minecraft.nether_fortress", "Nether Fortress");
        add("structure.minecraft.fortress", "Nether Fortress");
        add("structure.minecraft.end_city", "End City");
        add("structure.minecraft.buried_treasure", "Buried Treasure");
        add("structure.minecraft.village_plains", "Plains Village");
        add("structure.minecraft.village_desert", "Desert Village");
        add("structure.minecraft.village_savanna", "Savanna Village");
        add("structure.minecraft.village_snowy", "Snowy Village");
        add("structure.minecraft.village_taiga", "Taiga Village");
        add("structure.minecraft.ruined_portal", "Ruined Portal");
        add("structure.minecraft.ruined_portal_desert", "Desert Ruined Portal");
        add("structure.minecraft.ruined_portal_jungle", "Jungle Ruined Portal");
        add("structure.minecraft.ruined_portal_mountain", "Mountain Ruined Portal");
        add("structure.minecraft.ruined_portal_nether", "Nether Ruined Portal");
        add("structure.minecraft.ruined_portal_ocean", "Ocean Ruined Portal");
        add("structure.minecraft.ruined_portal_swamp", "Swamp Ruined Portal");
        add("structure.minecraft.bastion_remnant", "Bastion Remnant");
        add("structure.minecraft.nether_fossil", "Nether Fossil");
        add("structure.minecraft.ancient_city", "Ancient City");
        add("structure.minecraft.trail_ruins", "Trail Ruins");
        add("structure.minecraft.trial_chambers", "Trial Chambers");

        // ==================== Tooltip ====================
        add("tooltip.lordofmysteries.main_material.title", "Fantastic Magic Main Material");
        add("tooltip.lordofmysteries.main_material.usage", "This is the core material used to formulate the potion of the %s pathway, sequence %d.");
        add("tooltip.lordofmysteries.recipe.title", "--- Potion Recipe ---");
        add("tooltip.lordofmysteries.recipe.pathway", "Pathway: %s");
        add("tooltip.lordofmysteries.recipe.sequence", "Sequence %d: %s");
        add("tooltip.lordofmysteries.recipe.main_materials_title", "Main Materials:");
        add("tooltip.lordofmysteries.recipe.aux_materials_title", "Auxiliary Materials:");
        add("tooltip.lordofmysteries.recipe.acquisition_title", "Acquisition:");
        add("tooltip.lordofmysteries.characteristic.failed_brew_title", "--- Failed Brew: Magic Main Material ---");
        add("tooltip.lordofmysteries.characteristic.failed_brew_material", "Main Material: %s (Seq %d)");
        add("tooltip.lordofmysteries.characteristic.info", "Pathway: %s · Sequence %d");
        add("tooltip.lordofmysteries.characteristic.law", "§5\"The Law of Beyonder Characteristics Indestructibility\"");
        add("tooltip.lordofmysteries.characteristic.aggregated_title", "--- Aggregated Characteristics ---");
        add("tooltip.lordofmysteries.potion.title", "--- Beyonder Potion ---");
        add("tooltip.lordofmysteries.potion.pathway", "Pathway: %s");
        add("tooltip.lordofmysteries.potion.sequence", "Sequence: %d");
        add("tooltip.lordofmysteries.potion.empty_warning", "§cEmpty — no pathway data!");

        // ==================== 获取方式描述 ====================
        add("acquisition.lordofmysteries.potion_recipe.fool_9", "Kill a Lava Octopus in volcanic biomes; mine Star Crystal ore in deep caves.");

        // ==================== 非凡者系统消息 ====================
        add("message.lordofmysteries.absorption.start", "§d[Characteristics] §fYou try to forcefully absorb a mass of freshly raw characteristics...");
        add("message.lordofmysteries.sanity.collapse.irreversible", "§4[Sanity Collapse] §cThe remnant sanity failed to stand against the madness. Your loss of control has become irreversible...");
        add("message.lordofmysteries.sanity.collapse.immediate", "§4[Sanity Collapse] §cAlthough the potion was successfully integrated, your mental defense line completely shattered! You lost control due to zero sanity!");
        add("message.lordofmysteries.sanity.insufficient", "§c[Sanity Exhausted] §4Your mind can no longer endure another divine impact — sanity would reach zero!");
        add("message.lordofmysteries.rejection.mortal", "§c[Divine Rejection] §4A mortal flesh attempts to steal a high-order position without a proper ritual for %s!");
        add("message.lordofmysteries.cross_pathway.miracle", "§4[Aberration] §eA miracle happened! You managed to forcefully contain a potion from a foreign pathway...");
        add("message.lordofmysteries.rejection.beyonder", "§c[Divine Imbalance] §4You did not perform the proper ritual for Sequence %d! The divinity tore your flesh apart instantly!");
        add("message.lordofmysteries.upgrade.success", "§6[Divine Resonance] §fYou successfully advanced to Sequence %d!");
        add("message.lordofmysteries.contamination.loss_of_control", "§c[Incomprehensible Contamination] §4You have lost control!");
        add("message.lordofmysteries.madness.whisper", "§5You hear incomprehensible murmurs... Consciousness fading away...");
        add("message.lordofmysteries.madness.failed", "§4Your sanity completely collapsed. You became a victim of loss of control.");
        add("entity.lordofmysteries.madness_zombie", "§4Lost Control: %s");
        add("message.lordofmysteries.ritual.checking", "§d[Ritual Checking] §fYou are attempting to contain the divinity of %s Pathway · Sequence %d. The law is verifying the surrounding environment...");
        add("message.lordofmysteries.ritual.failed.bizarro_sorcerer", "§c[Ritual Failed] §4The number of 'spectators' (living entities) around you is only %d/15! Failed to weave a grand drama capable of deceiving the laws, ritual unestablished!");

        // ==================== 配置界面 ====================
        add("lordofmysteries.configuration.title", "Lord of Mysteries Configs");
        add("lordofmysteries.configuration.section.lordofmysteries.common.toml", "Lord of Mysteries Configs");
        add("lordofmysteries.configuration.section.lordofmysteries.common.toml.title", "Lord of Mysteries Configs");
        add("lordofmysteries.configuration.items", "Item List");
        add("lordofmysteries.configuration.logDirtBlock", "Log Dirt Block");
        add("lordofmysteries.configuration.magicNumberIntroduction", "Magic Number Text");
        add("lordofmysteries.configuration.magicNumber", "Magic Number");
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
