package com.nightydead.lordofmysteries.pathway.impl.fool;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.pathway.abstracts.AbstractPathway;
import net.minecraft.resources.ResourceLocation;

/**
 * 占卜家（Fool）途径实现类
 * 注册名为 "lordofmysteries:fool"
 * <p>
 * 在 registerSequences() 中注册该途径下的所有序列实现，
 * 目前已实现序列 9（占卜家），后续可扩展序列 8~0
 */
public class FoolPathway extends AbstractPathway {

    /**
     * 构造占卜家途径实例
     * 注册名为 "lordofmysteries:fool"
     */
    public FoolPathway() {
        super(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "fool"));
    }

    /**
     * 注册占卜家途径下的所有序列
     * 目前注册序列 9「占卜家」、序列 8「小丑」，后续可按需添加：
     * addSequence(new Seq7Magician());
     * ...
     */
    @Override
    protected void registerSequences() {
        this.addSequence(new Seq9Seer());
        this.addSequence(new Seq8Clown());
    }
}
