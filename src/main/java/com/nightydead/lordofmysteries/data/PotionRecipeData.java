package com.nightydead.lordofmysteries.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * 魔药配方数据记录
 * 存储在物品 DataComponent 中，包含途径、序列及可选的配方详情
 *
 * @param pathway            途径 ID（如 "fool"）
 * @param sequence           序列号（0-9）
 * @param mainMaterials      主材列表（可为空）
 * @param auxiliaryMaterials 辅材列表（可为空）
 * @param acquisition        获取方式描述（可为空字符串）
 */
public record PotionRecipeData(
        String pathway,
        int sequence,
        List<String> mainMaterials,
        List<String> auxiliaryMaterials,
        String acquisition
) {
    public static final Codec<PotionRecipeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("pathway").forGetter(PotionRecipeData::pathway),
                    Codec.INT.fieldOf("sequence").forGetter(PotionRecipeData::sequence),
                    Codec.STRING.listOf().optionalFieldOf("main_materials", List.of()).forGetter(PotionRecipeData::mainMaterials),
                    Codec.STRING.listOf().optionalFieldOf("auxiliary_materials", List.of()).forGetter(PotionRecipeData::auxiliaryMaterials),
                    Codec.STRING.optionalFieldOf("acquisition", "").forGetter(PotionRecipeData::acquisition)
            ).apply(instance, PotionRecipeData::new)
    );

    public static final StreamCodec<ByteBuf, PotionRecipeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PotionRecipeData::pathway,
            ByteBufCodecs.VAR_INT, PotionRecipeData::sequence,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(64)), PotionRecipeData::mainMaterials,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(64)), PotionRecipeData::auxiliaryMaterials,
            ByteBufCodecs.STRING_UTF8, PotionRecipeData::acquisition,
            PotionRecipeData::new
    );

    /** 创建仅包含途径+序列的精简配方数据（创造模式标签页用） */
    public static PotionRecipeData simple(String pathway, int sequence) {
        return new PotionRecipeData(pathway, sequence, List.of(), List.of(), "");
    }
}
