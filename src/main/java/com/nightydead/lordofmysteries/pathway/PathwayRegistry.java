package com.nightydead.lordofmysteries.pathway;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.pathway.abstracts.AbstractPathway;
import com.nightydead.lordofmysteries.pathway.impl.fool.FoolPathway;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 非凡途径注册中心
 * 管理所有途径（Pathway）的注册与查询，是神秘学系统的核心索引
 * 通过静态初始化块预注册所有内置途径，支持后续通过 register() 扩展新途径
 */
public class PathwayRegistry {
    /** 途径注册表，以 ResourceLocation 为键存储所有已注册的途径实例 */
    private static final Map<ResourceLocation, AbstractPathway> PATHWAYS = new HashMap<>();

    static {
        // 注册占卜家（Fool）途径
        register(new FoolPathway());
        // 后续添加新途径只需在此处追加一行：
        // register(new ErrorPathway());
    }

    /**
     * 注册一个途径实例到注册表
     *
     * @param pathway 要注册的途径对象，其注册名由途径自身定义
     */
    public static void register(AbstractPathway pathway) {
        PATHWAYS.put(pathway.getRegistryName(), pathway);
    }

    /**
     * 根据途径 ID 字符串查找已注册的途径实例
     * 自动将 ID 转为小写以匹配 ResourceLocation 的命名规范
     *
     * @param id 途径标识符（如 "fool"、"error"）
     * @return 对应的途径实例，未找到则返回 null
     */
    public static AbstractPathway get(String id) {
        return PATHWAYS.get(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, id.toLowerCase()));
    }

    /**
     * 根据完整的 ResourceLocation 查找途径实例
     *
     * @param rl 完整的资源位置标识
     * @return 对应的途径实例，未找到则返回 null
     */
    public static AbstractPathway get(ResourceLocation rl) {
        return PATHWAYS.get(rl);
    }

    /**
     * 获取所有已注册的途径集合
     *
     * @return 所有途径实例的集合视图
     */
    public static Collection<AbstractPathway> getAllPathways() {
        return PATHWAYS.values();
    }
}
