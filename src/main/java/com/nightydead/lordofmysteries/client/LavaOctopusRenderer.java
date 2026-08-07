package com.nightydead.lordofmysteries.client;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.entity.LavaOctopusEntity;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 拉瓦章鱼渲染器
 * 使用原版荧光鱿鱼模型层，但替换为模组自有的岩浆色纹理
 */
@OnlyIn(Dist.CLIENT)
public class LavaOctopusRenderer extends SquidRenderer<LavaOctopusEntity> {

    /** 拉瓦章鱼纹理路径（岩浆色调荧光鱿鱼纹理） */
    private static final ResourceLocation LAVA_OCTOPUS_LOCATION =
            ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "textures/entity/squid/lava_octopus.png");

    /**
     * @param context 渲染器上下文，用于获取模型层
     */
    public LavaOctopusRenderer(EntityRendererProvider.Context context) {
        super(context, new SquidModel<>(context.bakeLayer(ModelLayers.GLOW_SQUID)));
    }

    /**
     * 返回拉瓦章鱼的自定义纹理位置
     */
    @Override
    public ResourceLocation getTextureLocation(LavaOctopusEntity entity) {
        return LAVA_OCTOPUS_LOCATION;
    }

    /**
     * 保留荧光鱿鱼的发光亮度计算（受伤后变暗效果）
     * 使拉瓦章鱼在黑暗中自带岩浆光芒
     */
    @Override
    protected int getBlockLightLevel(LavaOctopusEntity entity, BlockPos pos) {
        int i = (int) Mth.clampedLerp(0.0F, 15.0F, 1.0F - (float) entity.getDarkTicksRemaining() / 10.0F);
        return i == 15 ? 15 : Math.max(i, super.getBlockLightLevel(entity, pos));
    }
}
