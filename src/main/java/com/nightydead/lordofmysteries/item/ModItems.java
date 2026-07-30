package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.item.custom.CharacteristicItem;
import com.nightydead.lordofmysteries.item.custom.MainMaterialItem;
import com.nightydead.lordofmysteries.item.custom.ModPotionItem;
import com.nightydead.lordofmysteries.item.custom.PotionRecipeItem;
import com.nightydead.lordofmysteries.item.custom.PureWaterItem;
import com.nightydead.lordofmysteries.item.custom.RitualDaggerItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 模组物品注册类
 * 统一管理并动态挂载超凡核心组件（数据基因）
 */
public class ModItems {

    /** 物品延迟注册表，使用模组 ID 作为命名空间 */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LordofMysteries.MODID);

    /** 特性物品检索字典 (途径 -> (序列 -> 物品))，用于动态查找对应途径和序列的特性物品 */
    public static final Map<String, Map<Integer, Supplier<Item>>> CHARACTERISTIC_MAP = new HashMap<>();
    /** 魔药物品检索字典 (途径 -> (序列 -> 物品))，用于动态查找对应途径和序列的魔药 */
    public static final Map<String, Map<Integer, Supplier<Item>>> POTION_MAP = new HashMap<>();

    // ==================== 聚合非凡特性 ====================
    public static final DeferredItem<Item> AGGREGATED_CHARACTERISTIC =
            ITEMS.register("characteristic/aggregated_characteristic", () -> new CharacteristicItem());

    // ==================== 魔药配方纸 ====================
    public static final DeferredItem<Item> POTION_RECIPE =
            ITEMS.register("potion_recipe", () -> new PotionRecipeItem());
    // ==================== 占卜家途径主材料 ====================
    /** 拉瓦章鱼血液 - 占卜家途径序列 9 魔药主材 */
    public static final DeferredItem<Item> LAVA_OCTOPUS_BLOOD = registerMainMaterial("lava_octopus_blood", "fool", 9);
    /** 星水晶 - 占卜家途径序列 9 魔药主材 */
    public static final DeferredItem<Item> STAR_CRYSTAL = registerMainMaterial("star_crystal", "fool", 9);

    // ==================== 纯水（炼药基底溶剂） ====================
    public static final DeferredItem<Item> PURE_WATER = registerPureWater("pure_water");

    // ==================== 仪式匕首 ====================
    public static final DeferredItem<Item> RITUAL_DAGGER =
            ITEMS.register("ritual_dagger", RitualDaggerItem::new);

    // ==================== 全途径序列 0 唯一性占位（后续可用独立唯一性物品替代） ====================
    // 序列0特性同时作为该途径的「唯一性」+ 全套特性聚合体使用

    // ==================== 途径物品数据表 ====================
    /** 途径数据定义：途径ID + 序列9~0英文名数组 + 序列9~0中文名数组 */
    public record PathwayDef(String pathwayId, String[] engNames, String[] zhNames) {
        public static final int[] SPIRITUALITY = {50, 100, 200, 350, 500, 1000, 2000, 5000, 10000, 15000};

        void registerAll() {
            for (int seq = 9; seq >= 0; seq--) {
                int idx = 9 - seq;
                String eng = engNames[idx];
                String zh = zhNames[idx];
                // 非凡特性（序列0为真神位阶，不存在对应非凡特性）
                if (seq != 0) {
                    registerCharacteristic(eng + "_characteristic", pathwayId, seq, SPIRITUALITY[idx]);
                }
                // 魔药
                registerPotion(eng + "_potion", pathwayId, seq, SPIRITUALITY[idx]);
            }
        }
    }

    /** 全22途径数据表 */
    private static final Map<String, PathwayDef> PATHWAY_DATA = new HashMap<>();

    static {
        // ==================== 1. 占卜家途径 (Fool) ====================
        PATHWAY_DATA.put("fool", new PathwayDef("fool",
                new String[]{"seer", "clown", "magician", "faceless", "marionettist", "bizarro_sorcerer", "scholar_of_yore", "miracle_invoker", "attendant_of_mysteries", "fool"},
                new String[]{"占卜家", "小丑", "魔术师", "无面人", "秘偶大师", "诡法师", "古代学者", "奇迹师", "诡秘侍者", "愚者"}));
        // ==================== 2. 偷盗者途径 (Error) ====================
        PATHWAY_DATA.put("error", new PathwayDef("error",
                new String[]{"marauder", "swindler", "cryptologist", "promethean", "dream_stealer", "parasite", "mentor_of_deceit", "trojan_horse_of_destiny", "worm_of_time", "error"},
                new String[]{"偷盗者", "诈骗师", "解密学者", "盗火人", "窃梦家", "寄生者", "欺瞒导师", "命运木马", "时之虫", "错误"}));
        // ==================== 3. 学徒途径 (Door) ====================
        PATHWAY_DATA.put("door", new PathwayDef("door",
                new String[]{"apprentice", "trickmaster", "astrologer", "scribe", "traveler", "secrets_sorcerer", "wanderer", "planeswalker", "key_of_stars", "door"},
                new String[]{"学徒", "戏法大师", "占星人", "记录官", "旅行家", "秘法师", "漫游者", "旅法师", "星之匙", "门"}));
        // ==================== 4. 观众途径 (Visionary) ====================
        PATHWAY_DATA.put("visionary", new PathwayDef("visionary",
                new String[]{"spectator", "telepathist", "psychiatrist", "hypnotist", "dreamwalker", "manipulator", "dream_weaver", "discerner", "author", "visionary"},
                new String[]{"观众", "读心者", "心理医生", "催眠师", "梦境行者", "操纵师", "织梦人", "洞察者", "作家", "空想家"}));
        // ==================== 5. 倒吊人途径 (Hanged Man) ====================
        PATHWAY_DATA.put("hanged_man", new PathwayDef("hanged_man",
                new String[]{"secrets_suppliant", "listener", "shadow_ascetic", "rose_bishop", "shepherd", "black_knight", "trinity_templar", "profane_presbyter", "dark_angel", "hanged_man"},
                new String[]{"秘祈人", "倾听者", "隐修士", "蔷薇主教", "牧羊人", "黑骑士", "三首圣堂", "秽语长老", "暗天使", "倒吊人"}));
        // ==================== 6. 水手途径 (Tyrant) ====================
        PATHWAY_DATA.put("tyrant", new PathwayDef("tyrant",
                new String[]{"sailor", "folk_of_rage", "seafarer", "wind_blessed", "ocean_songster", "cataclysmic_intercessor", "sea_king", "calamity", "thunder_god", "tyrant"},
                new String[]{"水手", "暴怒之民", "航海家", "风眷者", "海洋歌者", "灾难主祭", "海王", "天灾", "雷神", "暴君"}));
        // ==================== 7. 歌颂者途径 (Sun) ====================
        PATHWAY_DATA.put("sun", new PathwayDef("sun",
                new String[]{"bard", "light_suppliant", "solar_high_priest", "notary", "priest_of_light", "unshadowed", "justice_mentor", "lightseeker", "white_angel", "sun"},
                new String[]{"歌颂者", "祈光人", "太阳神官", "公证人", "光之祭司", "无暗者", "正义导师", "逐光者", "纯白天使", "太阳"}));
        // ==================== 8. 阅读者途径 (White Tower) ====================
        PATHWAY_DATA.put("white_tower", new PathwayDef("white_tower",
                new String[]{"reader", "student_of_ratiocination", "detective", "polymath", "mysticism_magister", "prophet", "cognizer", "wisdom_angel", "omniscient_eye", "white_tower"},
                new String[]{"阅读者", "推理学员", "守知者", "博学者", "秘术导师", "预言家", "洞悉者", "智天使", "全知之眼", "白塔"}));
        // ==================== 9. 窥秘人途径 (Hermit) ====================
        PATHWAY_DATA.put("hermit", new PathwayDef("hermit",
                new String[]{"mystery_pryer", "melee_scholar", "warlock", "scrolls_professor", "constellations_master", "mysticologist", "clairvoyant", "sage", "knowledge_emperor", "hermit"},
                new String[]{"窥秘人", "格斗学者", "巫师", "卷轴教授", "星象师", "神秘学家", "预言大师", "贤者", "知识皇帝", "隐者"}));
        // ==================== 10. 通识者途径 (Paragon) ====================
        PATHWAY_DATA.put("paragon", new PathwayDef("paragon",
                new String[]{"savant", "archaeologist", "appraiser", "artisan", "astronomer", "alchemist", "arcane_scholar", "knowledge_magister", "illuminator_of_civilization", "paragon"},
                new String[]{"通识者", "考古学家", "鉴定师", "机械专家", "天文学家", "炼金术师", "奥秘学者", "知识导师", "文明启蒙者", "完美者"}));
        // ==================== 11. 不眠者途径 (Darkness) ====================
        PATHWAY_DATA.put("darkness", new PathwayDef("darkness",
                new String[]{"sleepless", "midnight_poet", "nightmare", "soul_assurer", "spirit_warlock", "nightwatcher", "horror_bishop", "servant_of_concealment", "knight_of_misfortune", "darkness"},
                new String[]{"不眠者", "午夜诗人", "梦魇", "安魂师", "灵巫", "守夜人", "恐惧主教", "隐秘之仆", "厄难骑士", "黑暗"}));
        // ==================== 12. 收尸人途径 (Death) ====================
        PATHWAY_DATA.put("death", new PathwayDef("death",
                new String[]{"corpse_collector", "gravedigger", "spirit_medium", "necromancer", "gatekeeper", "undying", "ferryman", "death_consul", "pale_emperor", "death"},
                new String[]{"收尸人", "掘墓人", "通灵者", "死灵导师", "看门人", "不死者", "摆渡人", "死亡执政官", "苍白皇帝", "死神"}));
        // ==================== 13. 战士途径 (Twilight Giant) ====================
        PATHWAY_DATA.put("twilight_giant", new PathwayDef("twilight_giant",
                new String[]{"warrior", "pugilist", "weapon_master", "dawn_paladin", "guardian", "demon_hunter", "silver_knight", "glory", "hand_of_god", "twilight_giant"},
                new String[]{"战士", "格斗家", "武器大师", "黎明骑士", "守护者", "猎魔者", "银骑士", "荣耀者", "神明之手", "黄昏巨人"}));
        // ==================== 14. 猎人途径 (Red Priest) ====================
        PATHWAY_DATA.put("red_priest", new PathwayDef("red_priest",
                new String[]{"hunter", "provoker", "pyromaniac", "conspirer", "reaper", "iron_blooded_knight", "war_bishop", "weather_warlock", "conqueror", "red_priest"},
                new String[]{"猎人", "挑衅者", "纵火家", "阴谋家", "收割者", "铁血骑士", "战争主教", "天气术士", "征服者", "红祭司"}));
        // ==================== 15. 刺客途径 (Demoness) ====================
        PATHWAY_DATA.put("demoness", new PathwayDef("demoness",
                new String[]{"assassin", "instigator", "witch", "pleasure", "affliction", "despair", "unaging", "catastrophe", "apocalypse", "demoness"},
                new String[]{"刺客", "教唆者", "女巫", "欢愉魔女", "痛苦魔女", "绝望魔女", "不老魔女", "灾难魔女", "末日魔女", "原初魔女"}));
        // ==================== 16. 罪犯途径 (Abyss) ====================
        PATHWAY_DATA.put("abyss", new PathwayDef("abyss",
                new String[]{"criminal", "unwinged_angel", "serial_killer", "devil", "desire_apostle", "demon", "blatherer", "bloody_archduke", "filthy_monarch", "abyss"},
                new String[]{"罪犯", "折翼天使", "连环杀手", "恶魔", "欲望使徒", "魔鬼", "呓语者", "鲜血大公", "污秽君王", "深渊"}));
        // ==================== 17. 囚犯途径 (Chained) ====================
        PATHWAY_DATA.put("chained", new PathwayDef("chained",
                new String[]{"prisoner", "lunatic", "werewolf", "zombie", "wraith", "puppet", "disciple_of_silence", "ancient_bane", "abomination", "chained"},
                new String[]{"囚犯", "疯子", "狼人", "活尸", "怨魂", "木偶", "沉默门徒", "古代邪物", "神孽", "被缚者"}));
        // ==================== 18. 药师途径 (Moon) ====================
        PATHWAY_DATA.put("moon", new PathwayDef("moon",
                new String[]{"apothecary", "beast_tamer", "vampire", "potions_professor", "scarlet_scholar", "shaman_king", "high_summoner", "life_giver", "beauty_goddess", "moon"},
                new String[]{"药师", "驯兽师", "吸血鬼", "魔药教授", "深红学者", "巫王", "召唤大师", "创生者", "美神", "月亮"}));
        // ==================== 19. 耕种者途径 (Mother) ====================
        PATHWAY_DATA.put("mother", new PathwayDef("mother",
                new String[]{"planter", "doctor", "harvest_priest", "biologist", "druid", "ancient_alchemist", "pallbearer", "desolate_matriarch", "naturewalker", "mother"},
                new String[]{"耕种者", "医师", "丰收祭司", "生物学家", "德鲁伊", "古代炼金师", "抬棺人", "荒芜主母", "自然行者", "母亲"}));
        // ==================== 20. 仲裁人途径 (Justiciar) ====================
        PATHWAY_DATA.put("justiciar", new PathwayDef("justiciar",
                new String[]{"arbiter", "sheriff", "interrogator", "judge", "disciplinary_paladin", "imperative_mage", "chaos_hunter", "balancer", "hand_of_order", "justiciar"},
                new String[]{"仲裁人", "治安官", "审讯者", "法官", "惩戒骑士", "律令法师", "混乱猎手", "平衡者", "秩序之手", "审判者"}));
        // ==================== 21. 律师途径 (Black Emperor) ====================
        PATHWAY_DATA.put("black_emperor", new PathwayDef("black_emperor",
                new String[]{"lawyer", "barbarian", "briber", "baron_of_corruption", "mentor_of_disorder", "earl_of_the_fallen", "frenzied_mage", "duke_of_entropy", "prince_of_abolition", "black_emperor"},
                new String[]{"律师", "野蛮人", "贿赂者", "腐化男爵", "混乱导师", "堕落伯爵", "狂乱法师", "熵之公爵", "弑序亲王", "黑皇帝"}));
        // ==================== 22. 怪物途径 (Wheel of Fortune) ====================
        PATHWAY_DATA.put("wheel_of_fortune", new PathwayDef("wheel_of_fortune",
                new String[]{"monster", "robot", "lucky_one", "calamity_priest", "winner", "misfortune_mage", "chaoswalker", "soothsayer", "snake_of_mercury", "wheel_of_fortune"},
                new String[]{"怪物", "机器", "幸运者", "灾祸教士", "赢家", "厄运法师", "混乱行者", "先知", "水银之蛇", "命运之轮"}));

        // 批量注册所有途径的非凡特性与魔药
        PATHWAY_DATA.values().forEach(PathwayDef::registerAll);
    }

    // ==================== 魔药辅助材料 ====================
    public static final DeferredItem<Item> NIGHT_PERFUME_JUICE = registerAuxiliaryMaterial("night_perfume_juice");
    public static final DeferredItem<Item> GOLD_MINT_LEAF = registerAuxiliaryMaterial("gold_mint_leaf");
    public static final DeferredItem<Item> POISON_HEMLOCK_JUICE = registerAuxiliaryMaterial("poison_hemlock_juice");
    public static final DeferredItem<Item> DRAGON_BLOOD_POWDER = registerAuxiliaryMaterial("dragon_blood_powder");


    /**
     * 注册纯水产品 - 炼药锅的基础溶剂
     *
     * @param name 物品注册名
     * @return 注册后的 DeferredItem 引用
     */
    private static DeferredItem<Item> registerPureWater(String name) {
        return ITEMS.register("pure_water/" + name, () -> new PureWaterItem(PureWaterItem.createDefaultProperties()));
    }

    /**
     * 注册魔药辅助材料物品
     *
     * @param name 物品注册名
     * @return 注册后的 DeferredItem 引用
     */
    private static DeferredItem<Item> registerAuxiliaryMaterial(String name) {
        return ITEMS.register("auxiliary_material/" + name, () -> new Item(new Item.Properties()));
    }

    /**
     * 🧠 工业级封装快捷注册函数：全自动注入特定途径、序列与灵性组件，并登记进特性映射表
     */
    private static DeferredItem<Item> registerCharacteristic(String name, String pathway, int sequence, int maxSpiritual) {
        DeferredItem<Item> item = ITEMS.register("characteristic/" + name, () -> new CharacteristicItem(
                CharacteristicItem.createDefaultProperties()
                        .component(ModDataComponents.PATHWAY.get(), pathway)
                        .component(ModDataComponents.SEQUENCE.get(), sequence)
                        .component(ModDataComponents.MAX_SPIRITUALITY.get(), maxSpiritual)
        ));
        // 自动录入字典
        CHARACTERISTIC_MAP.computeIfAbsent(pathway.toLowerCase(), k -> new HashMap<>()).put(sequence, item);
        return item;
    }

    /**
     * 🧠 🚀 新增封装：全自动注册魔药主材，并自动登记到主材寻址字典中
     */
    private static DeferredItem<Item> registerMainMaterial(String name, String pathway, int sequence) {
        return ITEMS.register("main_material/" + name, () -> new MainMaterialItem(
                MainMaterialItem.createDefaultProperties()
                        .component(ModDataComponents.PATHWAY.get(), pathway)
                        .component(ModDataComponents.SEQUENCE.get(), sequence)
        ));
    }

    /**
     * 🧠 工业级封装快捷注册函数：全自动注入特定途径、序列与灵性组件，并登记进魔药映射表
     */
    private static DeferredItem<Item> registerPotion(String name, String pathway, int sequence, int maxSpiritual) {
        DeferredItem<Item> item = ITEMS.register("potion/" + name, () -> new ModPotionItem(
                ModPotionItem.createDefaultProperties()
                        .component(ModDataComponents.PATHWAY.get(), pathway)
                        .component(ModDataComponents.SEQUENCE.get(), sequence)
                        .component(ModDataComponents.MAX_SPIRITUALITY.get(), maxSpiritual)
        ));
        POTION_MAP.computeIfAbsent(pathway.toLowerCase(), k -> new HashMap<>()).put(sequence, item);
        return item;
    }

    /**
     * 📊 获取全途径数据表（供datagen等使用）
     */
    public static Map<String, PathwayDef> getPathwayData() {
        return PATHWAY_DATA;
    }

    /**
     * 🔮 通过途径与序列动态获取对应的纯净特性物品
     */
    public static Item getPureCharacteristic(String pathway, int sequence) {
        Map<Integer, Supplier<Item>> seqMap = CHARACTERISTIC_MAP.get(pathway.toLowerCase());
        return seqMap != null && seqMap.containsKey(sequence) ? seqMap.get(sequence).get() : null;
    }

    /**
     * 🧪 通过途径与序列动态获取对应的魔药物品
     */
    public static Item getPurePotion(String pathway, int sequence) {
        Map<Integer, Supplier<Item>> seqMap = POTION_MAP.get(pathway.toLowerCase());
        return seqMap != null && seqMap.containsKey(sequence) ? seqMap.get(sequence).get() : null;
    }

    /**
     * 将物品注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}