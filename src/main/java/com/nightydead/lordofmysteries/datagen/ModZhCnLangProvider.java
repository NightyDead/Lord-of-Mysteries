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
 * 中文语言文件数据提供者
 * 负责生成完整的 zh_cn.json 语言文件，涵盖所有模组内容的简体中文翻译
 * <p>
 * 包括：物品/方块名称、途径/序列名称、HUD 文本、UI 界面、
 * 炼药锅消息、非凡者系统消息、结构名称、按键绑定等
 */
public class ModZhCnLangProvider extends LanguageProvider {

    /** 途径 ID → 中文显示名称映射 */
    private static final Map<String, String> PATHWAY_ZH = Map.ofEntries(
            Map.entry("fool", "愚者"),
            Map.entry("error", "错误"),
            Map.entry("door", "门"),
            Map.entry("visionary", "空想家"),
            Map.entry("hanged_man", "倒吊人"),
            Map.entry("tyrant", "暴君"),
            Map.entry("sun", "太阳"),
            Map.entry("white_tower", "白塔"),
            Map.entry("hermit", "隐者"),
            Map.entry("paragon", "完美者"),
            Map.entry("darkness", "黑夜"),
            Map.entry("death", "死神"),
            Map.entry("twilight_giant", "黄昏巨人"),
            Map.entry("red_priest", "红祭司"),
            Map.entry("demoness", "魔女"),
            Map.entry("abyss", "深渊"),
            Map.entry("chained", "被缚者"),
            Map.entry("moon", "月亮"),
            Map.entry("mother", "大地"),
            Map.entry("justiciar", "审判者"),
            Map.entry("black_emperor", "黑皇帝"),
            Map.entry("wheel_of_fortune", "命运之轮")
    );

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
     */
    @Override
    protected void addTranslations() {
        // ==================== 聚合非凡特性 ====================
        add(ModItems.AGGREGATED_CHARACTERISTIC.get(), "聚合的非凡特性");

        // ==================== 途径名称 + 序列名称 + 非凡特性 + 魔药（通过数据驱动批量生成）====================
        for (Map.Entry<String, PathwayDef> pwEntry : ModItems.getPathwayData().entrySet()) {
            PathwayDef def = pwEntry.getValue();
            String pathwayId = def.pathwayId();
            String[] zhNames = def.zhNames();

            // 途径显示名称
            String pathwayDisplay = PATHWAY_ZH.getOrDefault(pathwayId, zhNames[0]);
            add("pathway.lordofmysteries." + pathwayId, pathwayDisplay);

            for (int seq = 9; seq >= 0; seq--) {
                int idx = 9 - seq;
                String zh = zhNames[idx];

                // 序列显示名称
                add("sequence.lordofmysteries." + pathwayId + "." + seq, zh);

                // 非凡特性
                Item characteristic = ModItems.getPureCharacteristic(pathwayId, seq);
                if (characteristic != null) {
                    add(characteristic, zh + "非凡特性");
                }
                // 魔药
                Item potion = ModItems.getPurePotion(pathwayId, seq);
                if (potion != null) {
                    add(potion, zh + "魔药");
                }
            }
        }

        // 额外途径（未在 PathwayDef 中注册的）
        add("pathway.lordofmysteries.none", "凡人");
        add("pathway.lordofmysteries.morgany", "欲望母树");

        // ==================== 魔药配方物品 ====================
        add(ModItems.POTION_RECIPE.get(), "魔药配方");

        // ==================== 魔药主材与特殊物品 ====================
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "拉瓦章鱼血液");
        add(ModItems.STAR_CRYSTAL.get(), "星水晶");
        add(ModItems.ADULT_HORNACIS_GRAY_GOAT_UNICORN_CRYSTAL.get(), "成年霍纳奇斯灰山羊独角结晶");
        add(ModItems.COMPLETE_HUMAN_FACE_ROSE.get(), "完整的人脸玫瑰");
        add(ModItems.PURE_WATER.get(), "纯水");
        add(ModItems.RITUAL_DAGGER.get(), "仪式匕首");
        add(ModItems.MYSTIC_DUST.get(), "神秘粉尘");
        add(ModItems.KNOWLEDGE_VESSEL.get(), "知识载体");
        add("item.lordofmysteries.pure_water.pure_water.effect.empty", "纯水");
        add("tooltip.lordofmysteries.pure_water.desc", "炼药的基础溶剂，纯净无杂质。");

        // ==================== 魔药辅助材料 ====================
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "夜香草汁液");
        add(ModItems.GOLD_MINT_LEAF.get(), "金薄荷叶");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "毒堇汁");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "龙血草粉末");
        add(ModItems.MANDRAKE_JUICE.get(), "曼陀罗汁液");
        add(ModItems.BLACK_EDGED_SUNFLOWER_POWDER.get(), "黑边太阳花粉末");
        add(ModItems.GOLDEN_CLOAK_GRASS_POWDER.get(), "金斗篷草粉末");

        // ==================== 方块 ====================
        add(ModBlocks.EXAMPLE_BLOCK.get(), "示例方块");
        add(ModBlocks.NIGHT_PERFUME_HERB.get(), "夜香草");
        add(ModBlocks.GOLD_MINT_HERB.get(), "金薄荷");
        add(ModBlocks.POISON_HEMLOCK_HERB.get(), "毒堇");
        add(ModBlocks.DRAGON_BLOOD_HERB.get(), "龙血草");
        add(ModBlocks.MANDRAKE_HERB.get(), "曼陀罗");
        add(ModBlocks.BLACK_EDGED_SUNFLOWER.get(), "黑边太阳花");
        add(ModBlocks.GOLDEN_CLOAK_GRASS.get(), "金斗篷草");
        add(ModBlocks.ALCHEMY_CAULDRON.get(), "炼药锅");
        add(ModBlocks.RITUAL_ALTAR.get(), "仪式祭坛");

        // ==================== 创造模式标签页 ====================
        add("itemGroup.characteristic_tab", "非凡特性");
        add("itemGroup.potion_tab", "魔药");
        add("itemGroup.mod_block_tab", "模组方块");
        add("itemGroup.potion_main_material_tab", "魔药主材");
        add("itemGroup.potion_auxiliary_materials_tab", "魔药辅材");
        add("itemGroup.natural_item_tab", "自然物品");
        add("itemGroup.recipe_tab", "魔药配方");

        // ==================== 炼药锅交互消息 ====================
        add("message.lordofmysteries.cauldron.item_added", "§a[炼药锅] §f材料已放入。");
        add("message.lordofmysteries.cauldron.item_removed", "§e[炼药锅] §f材料已取出。");
        add("message.lordofmysteries.cauldron.brew.success", "§6[炼药锅] §a炼制成功！魔药正在成形...");
        add("message.lordofmysteries.cauldron.brew.failed", "§c[炼药锅] §4炼制失败！材料已被污染扭曲...");
        add("message.lordofmysteries.cauldron.brew.failed_no_main", "§7[炼药锅] 炼制失败，但没有主材参与，材料已消散。");
        add("message.lordofmysteries.cauldron.brew.penalty", "§4[炼药锅] §c失败的炼制涌出混乱的能量，抽取了你的生命与理智！");

        // ==================== 仪式祭坛交互消息 ====================
        add("message.lordofmysteries.ritual.need_open_sky", "§c[仪式祭坛] 仪式必须在露天下进行！");
        add("message.lordofmysteries.ritual.need_aggregated", "§c[仪式祭坛] 祭坛上需要放置聚合非凡特性！");
        add("message.lordofmysteries.ritual.no_features", "§c[仪式祭坛] 聚合非凡特性中没有可分离的特性！");
        add("message.lordofmysteries.ritual.need_spirituality", "§c[仪式祭坛] 灵性不足！需要消耗 %s 点灵性。");
        add("message.lordofmysteries.ritual.need_sanity", "§c[仪式祭坛] 理智不足！需要消耗 %s 点理智。");
        add("message.lordofmysteries.ritual.success", "§6[仪式祭坛] §e闪电劈落！特性已分离！");
        add("message.lordofmysteries.ritual.separate_failed", "§c[仪式祭坛] §4分离失败！聚合特性数据异常，无法识别。");
        add("message.lordofmysteries.cauldron.potion_taken", "§a[炼药锅] §f你从锅中取出了魔药。");
        add("message.lordofmysteries.cauldron.characteristic_taken", "§e[炼药锅] §f你取出了聚合的非凡特性。");
        add("message.lordofmysteries.cauldron.use_bottle", "§e[炼药锅] §f请使用玻璃瓶来收集魔药。");
        add("message.lordofmysteries.cauldron.need_beyonder", "§c[炼药锅] §4只有非凡者才能注入灵性触发炼制。（Shift + 空手右键）");
        add("message.lordofmysteries.cauldron.need_spirituality", "§c[炼药锅] §4灵性不足！你至少需要 5 点灵性。");
        add("message.lordofmysteries.cauldron.brewed_block", "§e[炼药锅] §f炼药锅已完成炼制，请先取出结果。");

        // ==================== 仪式匕首消息 ====================
        add("message.lordofmysteries.ritual_dagger.cauldron_brew", "§5[仪式匕首] §f你通过匕首将灵性注入到炼药锅中...");

        // ==================== 配方学习消息 ====================
        add("message.lordofmysteries.recipe.learned", "§a[配方学习] §f你学会了 %s 途径 · 序列 %d %s 的魔药配方！");
        add("message.lordofmysteries.recipe.already_known", "§7[配方学习] 你已经掌握了这份知识。");

        // ==================== HUD 显示文本 ====================
        add("hud.lordofmysteries.identity.mortal", "凡人");
        add("hud.lordofmysteries.identity.format", "%s 途径 (序列 %s)");
        add("hud.lordofmysteries.sanity.text", "理智: %s / 100");
        add("hud.lordofmysteries.spirituality.text", "灵性: %s / %s");
        add("hud.lordofmysteries.digestion.text", "魔药消化度: %s / %s");

        // ==================== 按键绑定 ====================
        add("key.categories.lordofmysteries", "诡秘之主 - 超凡能力");
        add("key.lordofmysteries.toggle_vision", "开启/关闭 灵视");
        add("key.lordofmysteries.skill_wheel", "技能轮盘");
        add("key.lordofmysteries.knowledge_panel", "知识面板");

        // ==================== 技能 ====================
        add("skill.lordofmysteries.spirit_vision", "灵视");
        add("skill.lordofmysteries.divination", "占卜");
        add("skill.lordofmysteries.wheel.mortal_hint", "凡人之躯");

        // ==================== UI 界面 ====================
        add("screen.lordofmysteries.biome_divination", "群系占卜");
        add("screen.lordofmysteries.biome_divination.search", "搜索群系…");
        add("screen.lordofmysteries.biome_divination.hint", "↑↓ 选择  Enter 确认  Esc 取消");
        add("screen.lordofmysteries.structure_divination", "结构占卜");
        add("screen.lordofmysteries.structure_divination.search", "搜索结构…");
        add("screen.lordofmysteries.structure_divination.hint", "↑↓ 选择  Enter 确认  Esc 取消");
        add("screen.lordofmysteries.structure_divination.loading", "正在加载结构列表…");
        add("screen.lordofmysteries.knowledge", "知识面板");
        add("screen.lordofmysteries.knowledge.mortal", "§7你尚未触及超凡的门槛");
        add("screen.lordofmysteries.knowledge.learned_recipes", "--- 已学习的魔药配方 ---");
        add("screen.lordofmysteries.knowledge.current_identity", "--- 当前身份 ---");
        add("screen.lordofmysteries.knowledge.skills", "--- 已掌握的技能 ---");
        add("screen.lordofmysteries.knowledge.main_materials", "主材:");
        add("screen.lordofmysteries.knowledge.aux_materials", "辅材:");
        add("screen.lordofmysteries.knowledge.acquisition", "获取方式:");

        // ==================== Minecraft 结构名称翻译 ====================
        add("structure.minecraft.pillager_outpost", "掠夺者前哨站");
        add("structure.minecraft.mineshaft", "废弃矿井");
        add("structure.minecraft.mineshaft_mesa", "恶地废弃矿井");
        add("structure.minecraft.woodland_mansion", "林地府邸");
        add("structure.minecraft.mansion", "林地府邸");
        add("structure.minecraft.jungle_pyramid", "丛林神庙");
        add("structure.minecraft.desert_pyramid", "沙漠神殿");
        add("structure.minecraft.igloo", "雪屋");
        add("structure.minecraft.shipwreck", "沉船");
        add("structure.minecraft.shipwreck_beached", "沙滩沉船");
        add("structure.minecraft.swamp_hut", "沼泽小屋");
        add("structure.minecraft.stronghold", "要塞");
        add("structure.minecraft.monument", "海底神殿");
        add("structure.minecraft.ocean_ruin_cold", "冷水海底废墟");
        add("structure.minecraft.ocean_ruin_warm", "暖水海底废墟");
        add("structure.minecraft.nether_fortress", "下界要塞");
        add("structure.minecraft.fortress", "下界要塞");
        add("structure.minecraft.end_city", "末地城");
        add("structure.minecraft.buried_treasure", "埋藏的宝藏");
        add("structure.minecraft.village_plains", "平原村庄");
        add("structure.minecraft.village_desert", "沙漠村庄");
        add("structure.minecraft.village_savanna", "热带草原村庄");
        add("structure.minecraft.village_snowy", "雪原村庄");
        add("structure.minecraft.village_taiga", "针叶林村庄");
        add("structure.minecraft.ruined_portal", "废弃传送门");
        add("structure.minecraft.ruined_portal_desert", "沙漠废弃传送门");
        add("structure.minecraft.ruined_portal_jungle", "丛林废弃传送门");
        add("structure.minecraft.ruined_portal_mountain", "山地废弃传送门");
        add("structure.minecraft.ruined_portal_nether", "下界废弃传送门");
        add("structure.minecraft.ruined_portal_ocean", "海洋废弃传送门");
        add("structure.minecraft.ruined_portal_swamp", "沼泽废弃传送门");
        add("structure.minecraft.bastion_remnant", "堡垒遗迹");
        add("structure.minecraft.nether_fossil", "下界化石");
        add("structure.minecraft.ancient_city", "远古城市");
        add("structure.minecraft.trail_ruins", "古迹废墟");
        add("structure.minecraft.trial_chambers", "试炼密室");

        // ==================== Tooltip ====================
        add("tooltip.lordofmysteries.main_material.title", "非凡魔药主材");
        add("tooltip.lordofmysteries.main_material.usage", "这是调配【%s】途径序列 %d 魔药的核心基质。");
        add("tooltip.lordofmysteries.recipe.title", "--- 魔药配方 ---");
        add("tooltip.lordofmysteries.recipe.pathway", "途径: %s");
        add("tooltip.lordofmysteries.recipe.sequence", "序列 %d：%s");
        add("tooltip.lordofmysteries.recipe.main_materials_title", "主材:");
        add("tooltip.lordofmysteries.recipe.aux_materials_title", "辅材:");
        add("tooltip.lordofmysteries.recipe.acquisition_title", "获取方式:");
        add("tooltip.lordofmysteries.characteristic.failed_brew_title", "--- 炼制失败：魔药主材 ---");
        add("tooltip.lordofmysteries.characteristic.failed_brew_material", "主材来源: %s（序列 %d）");
        add("tooltip.lordofmysteries.characteristic.info", "途径: %s · 序列 %d");
        add("tooltip.lordofmysteries.characteristic.law", "§5\"非凡特性不灭定律\"");
        add("tooltip.lordofmysteries.characteristic.aggregated_title", "--- 聚合的非凡特性 ---");
        add("tooltip.lordofmysteries.potion.title", "--- 超凡魔药 ---");
        add("tooltip.lordofmysteries.potion.pathway", "途径: %s");
        add("tooltip.lordofmysteries.potion.sequence", "序列: %d");
        add("tooltip.lordofmysteries.potion.empty_warning", "§c空 — 无途径数据！");

        // ==================== 获取方式描述 ====================
        add("acquisition.lordofmysteries.potion_recipe.fool_9", "拉瓦章鱼血液：击杀荧光鱿鱼概率掉落；星水晶：挖掘紫水晶簇概率掉落；纯水：熔炉或烟熏炉烧制水瓶；夜香草汁液、毒堇汁：酿造台酿造；金薄荷叶、龙血草粉末：合成获取");
        add("acquisition.lordofmysteries.potion_recipe.fool_8", "成年霍纳奇斯灰山羊独角结晶：击杀成年山羊概率掉落；完整的人脸玫瑰：满月夜晚破坏玫瑰丛概率掉落；纯水：熔炉或烟熏炉烧制水瓶；曼陀罗汁液、毒堇汁：酿造台酿造；黑边太阳花粉末、金斗篷草粉末：合成获取");

        // ==================== 非凡者系统消息 ====================
        add("message.lordofmysteries.absorption.start", "§d[特性容纳] §f你尝试强行吸收一团聚合的生鲜非凡特性...");
        add("message.lordofmysteries.sanity.collapse.irreversible", "§4[理智崩溃] §c残存的理智未能阻挡疯狂，你的失控变得不可逆转...");
        add("message.lordofmysteries.sanity.collapse.immediate", "§4[理智崩溃] §c魔药虽然成功容纳，但你的精神防线已彻底瓦解！你因理智归零而失控了！");
        add("message.lordofmysteries.sanity.insufficient", "§c[理智不足] §4你的精神状态已无法承受再一次神性冲击，理智将归零！");
        add("message.lordofmysteries.rejection.mortal", "§c[神性排异] §4凡人之躯妄图窃取高位神职，且未举行对应的 %s 晋升仪式！");
        add("message.lordofmysteries.cross_pathway.miracle", "§4[异界错乱] §e奇迹发生了！你竟然强行容纳了异途径的魔药...");
        add("message.lordofmysteries.rejection.beyonder", "§c[神性失衡] §4你没有举行晋升序列 %d 所需的完整神秘学仪式！神性当场撕裂了你的躯壳！");
        add("message.lordofmysteries.upgrade.success", "§6[神性的共鸣] §f你成功跨入了新序列 %d！");
        add("message.lordofmysteries.contamination.loss_of_control", "§c[不可直视的高维污染] §4你失控了！");
        add("message.lordofmysteries.madness.whisper", "§5你听到了无法理解的呢喃... 意识正在消散...");
        add("message.lordofmysteries.madness.failed", "§4你的理智彻底崩溃，你成为了失控的牺牲品。");
        add("message.lordofmysteries.potion.consumed", "§d[神性共鸣] §f%s途径 · 序列 %d 的魔药在你体内激荡，从此你将踏足非凡！");
        add("message.lordofmysteries.characteristic.dropped", "§e[特性析出] §f你的非凡特性已经析出为一团聚合体...");
        add("message.lordofmysteries.characteristic.stack", "§6[同序列共鸣] §f你成功容纳了额外的序列 %d 特性，能力得到增强！");
        add("entity.lordofmysteries.madness_zombie", "§4失控的 %s");
        add("message.lordofmysteries.ritual.checking", "§d[仪式检查] §f你正在试图容纳 %s途径 · 序列 %d 的神性，法则正在审核周围的环境...");
        add("message.lordofmysteries.ritual.failed.bizarro_sorcerer", "§c[仪式失败] §4你周围的\"观众\"（活体生物）数量仅有 %d/15 个！无法交织出足以欺骗规律的盛大戏剧，仪式未成立！");

        // ==================== 灵视系统消息 ====================
        add("message.lordofmysteries.vision.mortal_blocked", "§c凡俗之躯无法触碰灵界，你未能开启灵视。");
        add("message.lordofmysteries.vision.no_spirituality", "§c灵性枯竭，无法开启灵视。");
        add("message.lordofmysteries.vision.activated", "§5【神秘学启示】§7 灵光在你眼底蔓延，你开启了灵视...");
        add("message.lordofmysteries.vision.deactivated", "§7 灵光隐去，你退出了灵视。");
        add("message.lordofmysteries.vision.spirituality_exhausted", "§c灵性枯竭！灵视被迫关闭，你感到一阵头晕目眩...");

        // ==================== 占卜系统消息 ====================
        add("message.lordofmysteries.divination.not_seer", "§c唯有占卜家途径的非凡者才能施展%s占卜。");
        add("message.lordofmysteries.divination.no_spirituality", "§c灵性不足，无法施展占卜（需要 %s 点灵性）。");
        add("message.lordofmysteries.divination.no_response", "§7灵摆毫无反应… %s 格内未发现目标%s。");
        add("message.lordofmysteries.divination.success", "§6【占卜启示】§7 灵摆指向 %s 格外的%s");
        add("message.lordofmysteries.divination.failed", "§c占卜失败");
        add("message.lordofmysteries.knowledge_divination.no_combination", "§c知识载体占卜需要一手持知识载体、另一手持魔药主材。");
        add("message.lordofmysteries.knowledge_divination.no_recipe", "§c无法从主材中解读出对应的魔药配方。");
        add("message.lordofmysteries.knowledge_divination.success", "§6【知识占卜】§a知识载体化作【%s·%s序列】的魔药配方！消耗了 §b%s§a 灵性、§c%s§a 理智。");

        // ==================== 同序列堆叠详情 ====================
        add("message.lordofmysteries.stack.detail", "§e📚 同序列堆叠×%s | 灵性上限 %s | 消耗 %s | 理智上限 %s");

        // ==================== HUD 动态文本 ====================
        add("hud.lordofmysteries.vision.active_title", "👁 灵视状态已激活 (Spirit Vision)");
        add("hud.lordofmysteries.vision.running", "§b⚡ 灵视运行中");

        // ==================== 配置界面 ====================
        add("lordofmysteries.configuration.title", "诡秘之主配置");
        add("lordofmysteries.configuration.section.lordofmysteries.common.toml", "诡秘之主配置");
        add("lordofmysteries.configuration.section.lordofmysteries.common.toml.title", "诡秘之主配置");
        add("lordofmysteries.configuration.items", "物品列表");
        add("lordofmysteries.configuration.logDirtBlock", "日志记录泥土方块");
        add("lordofmysteries.configuration.magicNumberIntroduction", "特征码文本");
        add("lordofmysteries.configuration.magicNumber", "特征码");
    }
}
