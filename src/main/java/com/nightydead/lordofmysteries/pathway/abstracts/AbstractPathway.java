package com.nightydead.lordofmysteries.pathway.abstracts;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

/**
 * 非凡途径抽象基类
 * 定义了途径的核心结构：注册名标识 + 序列等级映射表
 * 每个具体途径（如 FoolPathway）需继承此类并实现 registerSequences() 方法，
 * 在其中注册该途径下所有序列（9~0）的具体行为实现
 */
public abstract class AbstractPathway {
    /** 途径的唯一注册标识（如 "lordofmysteries:fool"） */
    private final ResourceLocation registryName;

    /** 序列映射表，以序列号（0-9）为键存储对应的序列行为实现 */
    private final Map<Integer, ISequence> sequences = new HashMap<>();

    /**
     * 构造途径实例，设置注册名并触发序列注册
     *
     * @param registryName 途径的资源位置标识
     */
    public AbstractPathway(ResourceLocation registryName) {
        this.registryName = registryName;
        // 在构造时立即调用子类的序列注册逻辑
        this.registerSequences();
    }

    /**
     * 由子类实现的序列注册方法
     * 子类应在此方法中调用 addSequence() 注册序列 9 到序列 0 的具体实现
     */
    protected abstract void registerSequences();

    /**
     * 向途径中注册一个序列等级
     *
     * @param sequence 序列实现对象，其序列号由 getSequenceNumber() 定义
     */
    protected void addSequence(ISequence sequence) {
        this.sequences.put(sequence.getSequenceNumber(), sequence);
    }

    /**
     * 获取途径的注册名标识
     *
     * @return 途径的 ResourceLocation
     */
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    /**
     * 获取指定序列号的行为实现
     *
     * @param sequenceNum 序列号（0-9，数字越小等级越高）
     * @return 对应的序列实现，未注册则返回 null
     */
    public ISequence getSequence(int sequenceNum) {
        return this.sequences.get(sequenceNum);
    }
}
