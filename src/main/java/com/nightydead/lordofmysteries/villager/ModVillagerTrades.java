package com.nightydead.lordofmysteries.villager;

import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PotionRecipeData;
import com.nightydead.lordofmysteries.data.PotionRecipeRegistry;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.Arrays;
import java.util.List;

/**
 * 牧师村民交易注册类
 * 为原版牧师（Cleric）添加魔药配方纸交易
 * 配方从 PotionRecipeRegistry 动态读取，后续新增配方 JSON 会自动纳入交易池
 */
public class ModVillagerTrades {

    /**
     * 魔药配方交易记录
     * 每次打开交易界面时从已加载配方中按序列筛选并随机选取一张
     *
     * @param sequence 出售配方对应的序列号
     * @param price    出售价格（绿宝石数量）
     * @param maxUses  交易可用次数
     * @param xp       交易提供的经验值
     */
    private record RecipeTradeListing(int sequence, int price, int maxUses, int xp) implements VillagerTrades.ItemListing {

        @Override
        public @Nullable MerchantOffer getOffer(Entity entity, RandomSource random) {
            // 从注册表筛选该序列的所有已加载配方（可能包含多个途径）
            List<String> keys = PotionRecipeRegistry.getAllKeys().stream()
                    .filter(key -> key.endsWith(":" + sequence))
                    .toList();
            if (keys.isEmpty()) {
                // 该序列暂无配方时不显示此交易，后续新增配方后自动出现
                return null;
            }

            // 随机选取一张该序列的配方纸
            String key = keys.get(random.nextInt(keys.size()));
            PotionRecipeData data = PotionRecipeRegistry.get(key.split(":")[0], sequence);
            if (data == null) {
                return null;
            }

            ItemStack recipeStack = new ItemStack(ModItems.POTION_RECIPE.get(), 1);
            recipeStack.set(ModDataComponents.RECIPE_DATA.get(), data);
            return new MerchantOffer(new ItemCost(Items.EMERALD, price), recipeStack, maxUses, xp, 0.05f);
        }
    }

    /**
     * 向原版牧师注册魔药配方交易（按职业等级从低到高）
     * 新手牧师出售序列 9 配方，学徒牧师出售序列 8 配方，熟练牧师出售序列 7 配方
     * 注：1.21.1 中 VillagerTrades.TRADES 按职业 → 等级（1-5）两级分组，值为 ItemListing 数组
     */
    public static void register() {
        addTrade(1, new RecipeTradeListing(9, 10, 12, 8));
        addTrade(2, new RecipeTradeListing(8, 20, 12, 10));
        addTrade(3, new RecipeTradeListing(7, 30, 12, 12));
    }

    /**
     * 将自定义交易追加到牧师指定职业等级的现有交易数组末尾
     *
     * @param level   职业等级（1-5）
     * @param listing 新增的交易条目
     */
    private static void addTrade(int level, VillagerTrades.ItemListing listing) {
        Int2ObjectMap<VillagerTrades.ItemListing[]> levelTrades = VillagerTrades.TRADES.get(VillagerProfession.CLERIC);
        if (levelTrades == null) {
            return;
        }
        VillagerTrades.ItemListing[] oldListings = levelTrades.get(level);
        if (oldListings == null) {
            return;
        }
        VillagerTrades.ItemListing[] newListings = Arrays.copyOf(oldListings, oldListings.length + 1);
        newListings[oldListings.length] = listing;
        levelTrades.put(level, newListings);
    }
}
