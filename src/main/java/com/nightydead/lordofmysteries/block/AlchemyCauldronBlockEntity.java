    package com.nightydead.lordofmysteries.block;

import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.item.custom.CharacteristicItem;
import com.nightydead.lordofmysteries.item.custom.MainMaterialItem;
import com.nightydead.lordofmysteries.recipe.ModRecipes;
import com.nightydead.lordofmysteries.recipe.PotionRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * 炼药锅方块实体
 * 管理炼药锅的内部物品栏、酿造状态机、配方匹配与失败惩罚逻辑
 */
public class AlchemyCauldronBlockEntity extends BlockEntity {

    /** 酿造状态：空锅（无物品） */
    public static final int STATE_EMPTY = 0;
    /** 酿造状态：锅内有物品，尚未开始炼制 */
    public static final int STATE_CONTAINS_ITEMS = 1;
    /** 酿造状态：炼制成功 */
    public static final int STATE_SUCCESS = 2;
    /** 酿造状态：炼制失败 */
    public static final int STATE_FAILED = 3;

    /** 最大物品栏容量 */
    private static final int MAX_ITEMS = 9;
    /** 非凡者空手注入灵性消耗量 */
    private static final int BEYONDER_BREW_COST = 5;
    /** 失败惩罚影响半径（方块） */
    private static final double PENALTY_RADIUS = 2.0;

    /** 内部物品栏（按放入顺序排列） */
    private final List<ItemStack> items = new ArrayList<>();
    /** 当前酿造状态 */
    private int brewState = STATE_EMPTY;
    /** 酿造结果类型："potion" 或 "characteristic" */
    private String resultType = "";
    /** 酿造结果对应的途径 ID */
    private String resultPathway = "";
    /** 酿造结果对应的序列号 */
    private int resultSequence = 0;
    /** 失败酿造的主材信息列表（格式 "pathway:sequence" 或主材显示名称） */
    private List<String> failedMainMaterials = new ArrayList<>();
    /** 失败酿造产物中是否包含非凡特性条目（"途径:序列"） */
    private boolean failedHasCharacteristics = false;
    /** 失败酿造产物中是否包含魔药主材条目（显示名称） */
    private boolean failedHasMainMaterials = false;

    public AlchemyCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_CAULDRON.get(), pos, state);
    }

    // ==================== 状态查询 ====================

    /** 获取当前酿造状态 */
    public int getBrewState() { return brewState; }
    /** 获取内部物品栏的只读副本（用于渲染） */
    public List<ItemStack> getItems() { return List.copyOf(items); }
    /** 获取酿造结果途径 */
    public String getResultPathway() { return resultPathway; }
    /** 获取酿造结果序列 */
    public int getResultSequence() { return resultSequence; }

    /** 判断锅是否为空 */
    public boolean isEmpty() { return brewState == STATE_EMPTY; }
    /** 判断是否已完成酿造（成功或失败） */
    public boolean isBrewed() { return brewState == STATE_SUCCESS || brewState == STATE_FAILED; }

    // ==================== 物品交互 ====================

    /**
     * 向锅内放入物品
     * 仅在锅未开始酿造时允许放入，且物品栏未满
     *
     * @param stack 放入的物品堆叠
     * @return 是否成功放入
     */
    public boolean addItem(ItemStack stack) {
        if (isBrewed() || items.size() >= MAX_ITEMS) return false;
        items.add(stack.copy());
        updateBrewState();
        setChanged();
        syncToClient();
        return true;
    }

    /**
     * 从锅内逆序弹出最后一个放入的物品
     * 仅在锅未开始酿造时允许取出
     *
     * @return 取出的物品，若无法取出则返回 ItemStack.EMPTY
     */
    public ItemStack removeLastItem() {
        if (isBrewed() || items.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = items.remove(items.size() - 1);
        updateBrewState();
        setChanged();
        syncToClient();
        return removed;
    }

    // ==================== 酿造触发 ====================

    /**
     * 触发酿造逻辑
     * 由 AlchemyCauldronBlock 在检测到灵性注入时调用
     *
     * @param player 注入灵性的玩家
     */
    public void triggerBrew(Player player) {
        if (level == null || level.isClientSide()) return;
        if (items.isEmpty() || isBrewed()) return;

        // 匹配配方
        PotionRecipe matched = findMatchingRecipe();

        if (matched != null) {
            // 酿造成功
            brewState = STATE_SUCCESS;
            resultType = "potion";
            resultPathway = matched.getPathway();
            resultSequence = matched.getSequence();
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.lordofmysteries.cauldron.brew.success"), true);
            }
        } else {
            // 酿造失败：按放入顺序收集主材名与非凡特性条目（非凡特性不灭：吞入的材料全部还原析出）
            failedMainMaterials = new ArrayList<>();
            failedHasCharacteristics = false;
            failedHasMainMaterials = false;
            for (ItemStack stack : items) {
                if (stack.getItem() instanceof MainMaterialItem) {
                    failedMainMaterials.add(stack.getHoverName().getString());
                    failedHasMainMaterials = true;
                } else if (isCharacteristicItem(stack)) {
                    // 单体特性 → 记录 "途径:序列"；聚合特性 → 展开全部条目
                    String key = getCharacteristicKey(stack);
                    if (key != null) {
                        failedMainMaterials.add(key);
                    } else {
                        List<String> aggregated = stack.get(ModDataComponents.AGGREGATED_FEATURES.get());
                        if (aggregated != null) {
                            failedMainMaterials.addAll(aggregated);
                        }
                    }
                    failedHasCharacteristics = true;
                }
            }

            // 如果没有主材也没有非凡特性，不产生任何产物，直接重置
            if (failedMainMaterials.isEmpty()) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable(
                            "message.lordofmysteries.cauldron.brew.failed_no_main"), true);
                }
                reset();
                return;
            }

            // 有可还原材料时，设置为失败状态并生成聚合非凡特性
            brewState = STATE_FAILED;
            resultType = "characteristic";
            // 对范围内玩家施加失败惩罚
            applyFailurePenalty();
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.lordofmysteries.cauldron.brew.failed"), true);
            }
        }

        setChanged();
        syncToClient();
    }

    // ==================== 结果取出 ====================

    /**
     * 取出酿造结果
     * 成功时返回对应魔药，失败时返回标记为"魔药主材"的聚合非凡特性
     *
     * @return 酿造结果物品堆叠
     */
    public ItemStack takeResult() {
        if (!isBrewed()) return ItemStack.EMPTY;

        ItemStack result;
        if (brewState == STATE_SUCCESS) {
            // 从 POTION_MAP 查找对应途径/序列的魔药
            var seqMap = ModItems.POTION_MAP.get(resultPathway.toLowerCase());
            if (seqMap != null && seqMap.containsKey(resultSequence)) {
                result = new ItemStack(seqMap.get(resultSequence).get());
            } else {
                // 找不到对应魔药配方，降级为失败结果
                result = createFailedCharacteristic();
            }
        } else {
            result = createFailedCharacteristic();
        }

        // 重置状态
        reset();
        return result;
    }

    /**
     * 创建失败酿造的聚合非凡特性
     * - 纯非凡特性失败 → 普通聚合特性（条目为 "途径:序列"，可直接被仪式祭坛分离/吞服）
     * - 含魔药主材失败 → 带 FAILED_BREW_MARKER 标记（条目可能混合主材名与特性条目）
     */
    private ItemStack createFailedCharacteristic() {
        ItemStack stack = new ItemStack(ModItems.AGGREGATED_CHARACTERISTIC.get());
        if (!failedMainMaterials.isEmpty()) {
            stack.set(ModDataComponents.AGGREGATED_FEATURES.get(), new ArrayList<>(failedMainMaterials));
        }
        if (failedHasMainMaterials) {
            stack.set(ModDataComponents.FAILED_BREW_MARKER.get(), true);
        }
        return stack;
    }

    // ==================== 配方匹配 ====================

    /**
     * 在当前世界中查找匹配的魔药配方
     * 先验证材料顺序（辅材全部在主材之前），再匹配配方
     *
     * @return 匹配的配方，无匹配返回 null
     */
    private PotionRecipe findMatchingRecipe() {
        if (level == null) return null;

        // 验证材料顺序：辅材必须全部在主材之前
        if (!validateMaterialOrder()) return null;

        // 获取所有已注册的魔药配方
        var recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.POTION_BREWING_TYPE.get());

        for (RecipeHolder<PotionRecipe> holder : recipes) {
            if (matchesRecipe(holder.value())) {
                return holder.value();
            }
        }
        return null;
    }

    /**
     * 验证物品栏中的材料顺序
     * 规则：所有辅材（AuxiliaryMaterial / PureWater / 原版物品）必须在所有主材（MainMaterialItem / 非凡特性）之前
     */
    private boolean validateMaterialOrder() {
        boolean seenMainMaterial = false;
        for (ItemStack stack : items) {
            boolean isMain = stack.getItem() instanceof MainMaterialItem || isCharacteristicItem(stack);
            if (isMain) {
                seenMainMaterial = true;
            } else if (seenMainMaterial) {
                // 辅材出现在主材之后 → 顺序违规
                return false;
            }
        }
        return true;
    }

    /**
     * 检查当前物品栏是否匹配指定配方
     * 辅材无序匹配，主材无序匹配（仅需主材在辅材之后放入，由 validateMaterialOrder 保证）
     * 非凡特性可替代全部魔药主材：恰好 1 个与配方同途径同序列的单体特性 → 主材部分匹配成功
     */
    private boolean matchesRecipe(PotionRecipe recipe) {
        List<Ingredient> auxIngredients = recipe.getAuxiliaryIngredients();
        List<Ingredient> mainIngredients = recipe.getMainIngredientsOrder();

        // 分离物品栏中的辅材和主材（非凡特性视为主材类）
        List<ItemStack> auxItems = new ArrayList<>();
        List<ItemStack> mainItems = new ArrayList<>();
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof MainMaterialItem || isCharacteristicItem(stack)) {
                mainItems.add(stack);
            } else {
                auxItems.add(stack);
            }
        }

        // 辅材数量必须完全匹配
        if (auxItems.size() != auxIngredients.size()) {
            return false;
        }

        // 特性替代主材：恰好 1 个单体特性且途径/序列与配方一致 → 替代全部主材
        if (mainItems.size() == 1) {
            String key = getCharacteristicKey(mainItems.get(0));
            if (key != null) {
                return key.equals(recipe.getPathway().toLowerCase() + ":" + recipe.getSequence());
            }
        }

        // 传统主材匹配：数量必须完全匹配
        if (mainItems.size() != mainIngredients.size()) {
            return false;
        }

        // 辅材无序匹配：每个配方辅材都必须有对应物品
        List<ItemStack> auxPool = new ArrayList<>(auxItems);
        for (Ingredient ing : auxIngredients) {
            boolean found = false;
            for (int i = 0; i < auxPool.size(); i++) {
                if (ing.test(auxPool.get(i))) {
                    auxPool.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        // 主材无序匹配：每个配方主材都必须有对应物品
        List<ItemStack> mainPool = new ArrayList<>(mainItems);
        for (Ingredient ing : mainIngredients) {
            boolean found = false;
            for (int i = 0; i < mainPool.size(); i++) {
                if (ing.test(mainPool.get(i))) {
                    mainPool.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    // ==================== 非凡特性辅助 ====================

    /** 判断物品是否为非凡特性（单体或聚合特性物品） */
    private static boolean isCharacteristicItem(ItemStack stack) {
        return stack.getItem() instanceof CharacteristicItem;
    }

    /**
     * 获取单体非凡特性的"途径:序列"键
     * 聚合特性（无 PATHWAY/SEQUENCE 组件）与非特性物品返回 null
     */
    private static String getCharacteristicKey(ItemStack stack) {
        if (!(stack.getItem() instanceof CharacteristicItem)) return null;
        String pathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer sequence = stack.get(ModDataComponents.SEQUENCE.get());
        if (pathway == null || sequence == null) return null;
        return pathway.toLowerCase() + ":" + sequence;
    }

    // ==================== 失败惩罚 ====================

    /**
     * 对炼药锅周围 PENALTY_RADIUS 半径内的所有玩家施加失败惩罚
     * 扣除当前生命值的一半（最小1点伤害）+ 最大理智值的一半
     */
    private void applyFailurePenalty() {
        if (level == null) return;

        AABB area = new AABB(worldPosition).inflate(PENALTY_RADIUS);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area);

        for (Player player : nearbyPlayers) {
            // 扣除当前生命值的一半（最小1点伤害）
            float currentHealth = player.getHealth();
            float damage = Math.max(1.0F, currentHealth / 2.0F);
            player.hurt(player.damageSources().magic(), damage);

            // 扣除最大理智值的一半
            PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
            int sanityLoss = Math.max(1, data.getMaxSanity() / 2);
            data.addSanity(-sanityLoss);

            player.displayClientMessage(Component.translatable(
                    "message.lordofmysteries.cauldron.brew.penalty"), true);
        }
    }

    // ==================== 状态管理 ====================

    /** 根据当前物品栏内容更新酿造状态 */
    private void updateBrewState() {
        if (items.isEmpty()) {
            brewState = STATE_EMPTY;
        } else if (brewState != STATE_SUCCESS && brewState != STATE_FAILED) {
            brewState = STATE_CONTAINS_ITEMS;
        }
    }

    /** 重置炼药锅到初始空状态 */
    private void reset() {
        items.clear();
        brewState = STATE_EMPTY;
        resultType = "";
        resultPathway = "";
        resultSequence = 0;
        failedMainMaterials.clear();
        failedHasCharacteristics = false;
        failedHasMainMaterials = false;
        setChanged();
        syncToClient();
    }

    /** 将内部物品全部掉落到世界中（破坏方块时调用） */
    public void dropAllItems() {
        if (level == null) return;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemStack dropped = stack.copy();
                ItemEntity itemEntity =
                        new ItemEntity(
                                level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                                worldPosition.getZ() + 0.5, dropped);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    /** 获取破坏方块时应掉落的物品列表 */
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();

        if (isBrewed()) {
            // 已酿造 → 掉落酿造结果
            ItemStack result = takeResult();
            if (!result.isEmpty()) {
                drops.add(result);
            }
        } else {
            // 未酿造 → 掉落所有放入的材料
            drops.addAll(items.stream().map(ItemStack::copy).toList());
        }

        return drops;
    }

    // ==================== 数据序列化 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // 保存物品栏
        ListTag itemList = new ListTag();
        for (ItemStack stack : items) {
            itemList.add(stack.saveOptional(registries));
        }
        tag.put("Items", itemList);

        // 保存酿造状态
        tag.putInt("BrewState", brewState);
        tag.putString("ResultType", resultType);
        tag.putString("ResultPathway", resultPathway);
        tag.putInt("ResultSequence", resultSequence);

        // 保存失败主材信息
        if (!failedMainMaterials.isEmpty()) {
            ListTag mainMatList = new ListTag();
            for (String s : failedMainMaterials) {
                mainMatList.add(StringTag.valueOf(s));
            }
            tag.put("FailedMainMaterials", mainMatList);
        }
        tag.putBoolean("FailedHasCharacteristics", failedHasCharacteristics);
        tag.putBoolean("FailedHasMainMaterials", failedHasMainMaterials);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // 加载物品栏
        items.clear();
        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            ItemStack parsed = ItemStack.parseOptional(registries, itemList.getCompound(i));
            if (!parsed.isEmpty()) {
                items.add(parsed);
            }
        }

        // 加载酿造状态
        brewState = tag.getInt("BrewState");
        resultType = tag.getString("ResultType");
        resultPathway = tag.getString("ResultPathway");
        resultSequence = tag.getInt("ResultSequence");

        // 加载失败主材信息
        failedMainMaterials.clear();
        if (tag.contains("FailedMainMaterials")) {
            ListTag mainMatList = tag.getList("FailedMainMaterials", Tag.TAG_STRING);
            for (int i = 0; i < mainMatList.size(); i++) {
                failedMainMaterials.add(mainMatList.getString(i));
            }
        }
        failedHasCharacteristics = tag.getBoolean("FailedHasCharacteristics");
        failedHasMainMaterials = tag.getBoolean("FailedHasMainMaterials");
    }

    // ==================== 客户端同步 ====================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("BrewState", brewState);
        tag.putString("ResultPathway", resultPathway);
        tag.putInt("ResultSequence", resultSequence);

        // 保存物品数据供渲染器使用
        ListTag itemList = new ListTag();
        for (ItemStack stack : items) {
            itemList.add(stack.saveOptional(registries));
        }
        tag.put("Items", itemList);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** 同步方块实体数据到客户端（触发方块更新） */
    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** 获取酿造状态名称（用于方块属性） */
    public static String getBrewStateName(int state) {
        return switch (state) {
            case STATE_EMPTY -> "empty";
            case STATE_CONTAINS_ITEMS -> "contains_items";
            case STATE_SUCCESS -> "success";
            case STATE_FAILED -> "failed";
            default -> "empty";
        };
    }
}
