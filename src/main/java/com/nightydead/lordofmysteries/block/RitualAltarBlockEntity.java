package com.nightydead.lordofmysteries.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 仪式祭坛方块实体
 * 管理放置在祭坛顶部的物品列表，支持 LIFO（后进先出）存取
 * 物品数据通过 getUpdateTag/getUpdatePacket 同步到客户端供渲染器使用
 */
public class RitualAltarBlockEntity extends BlockEntity {

    /** 祭坛上放置的物品列表（按放入顺序排列） */
    private final List<ItemStack> items = new ArrayList<>();

    public RitualAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUAL_ALTAR.get(), pos, state);
    }

    /** 判断祭坛上是否有物品 */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** 获取物品列表的只读副本（供渲染器使用） */
    public List<ItemStack> getItems() {
        return List.copyOf(items);
    }

    /**
     * 将物品放置到祭坛上（整组放入）
     *
     * @param stack 要放置的物品堆叠
     */
    public void addItem(ItemStack stack) {
        items.add(stack.copy());
        setChanged();
        syncToClient();
    }

    /**
     * 从祭坛上逆序取出最近放入的物品
     *
     * @return 取出的物品堆叠，若为空则返回 ItemStack.EMPTY
     */
    public ItemStack removeLastItem() {
        if (items.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = items.remove(items.size() - 1);
        setChanged();
        syncToClient();
        return removed;
    }

    // ==================== NBT 序列化 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag itemList = new ListTag();
        for (ItemStack stack : items) {
            itemList.add(stack.saveOptional(registries));
        }
        tag.put("Items", itemList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        items.clear();
        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            ItemStack parsed = ItemStack.parseOptional(registries, itemList.getCompound(i));
            if (!parsed.isEmpty()) {
                items.add(parsed);
            }
        }
    }

    // ==================== 客户端同步 ====================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
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

    /** 同步方块实体数据到客户端（触发渲染更新） */
    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
