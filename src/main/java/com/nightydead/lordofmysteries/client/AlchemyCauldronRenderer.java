package com.nightydead.lordofmysteries.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightydead.lordofmysteries.block.AlchemyCauldronBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 炼药锅方块实体渲染器
 * 渲染锅内悬浮的物品：环形分布 + 缓慢旋转 + 上下浮动
 */
public class AlchemyCauldronRenderer implements BlockEntityRenderer<AlchemyCauldronBlockEntity> {

    public AlchemyCauldronRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlchemyCauldronBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        List<ItemStack> items = blockEntity.getItems();
        if (items.isEmpty()) return;

        // 酿造完成（成功/失败）后不再显示悬浮物品，液体已代替视觉效果
        if (blockEntity.isBrewed()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float time = blockEntity.getLevel() != null
                ? (blockEntity.getLevel().getGameTime() + partialTick) : 0;

        poseStack.pushPose();

        // 移动到锅中心偏上位置
        poseStack.translate(0.5, 0.7, 0.5);

        if (items.size() == 1) {
            // 单个物品：居中悬浮
            renderSingleItem(items.get(0), poseStack, itemRenderer, bufferSource,
                    packedLight, packedOverlay, time, 0, 1);
        } else {
            // 多个物品：环形分布
            float angleStep = 360.0F / items.size();
            float radius = 0.2F;
            for (int i = 0; i < items.size(); i++) {
                float angle = (float) Math.toRadians(angleStep * i + time * 2);
                float x = (float) Math.cos(angle) * radius;
                float z = (float) Math.sin(angle) * radius;

                poseStack.pushPose();
                poseStack.translate(x, 0, z);
                renderSingleItem(items.get(i), poseStack, itemRenderer, bufferSource,
                        packedLight, packedOverlay, time, i, items.size());
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    /**
     * 渲染单个悬浮物品
     * 包含缓慢旋转和上下浮动效果
     */
    private void renderSingleItem(ItemStack stack, PoseStack poseStack,
                                   ItemRenderer itemRenderer, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay,
                                   float time, int index, int total) {
        poseStack.pushPose();

        // 上下浮动效果
        float bobOffset = (float) Math.sin(time * 0.05 + index * 0.5) * 0.05F;
        poseStack.translate(0, bobOffset, 0);

        // 缓慢旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.5F + index * 45));

        // 缩小物品渲染尺寸
        poseStack.scale(0.5F, 0.5F, 0.5F);

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, bufferSource,
                null, 0);

        poseStack.popPose();
    }

    /** 渲染距离限制：64 方块 */
    @Override
    public int getViewDistance() {
        return 64;
    }
}
