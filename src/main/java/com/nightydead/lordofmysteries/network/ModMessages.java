package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络通道统一注册管理类
 * 负责在模组初始化时监听网络注册事件，并将所有自定义数据包统一注册到 NeoForge 网络系统[cite: 10]
 */
public class ModMessages {

    /**
     * 注册所有自定义网络数据包
     * 应在模组主类构造函数中调用：ModMessages.register(modEventBus)[cite: 10]
     *
     * @param modEventBus 模组生命周期事件总线[cite: 10]
     */
    public static void register(IEventBus modEventBus) {
        // 监听网络载荷注册事件，获取 PayloadRegistrar 进行注册[cite: 10]
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            final PayloadRegistrar registrar = event.registrar(LordofMysteries.MODID); //[cite: 10]

            // 依次注册各个数据包的处理器[cite: 10]
            SyncSanityPacket.register(registrar); //[cite: 10]
            SyncSpiritualityPacket.register(registrar); //[cite: 10]
            SyncDigestionPacket.register(registrar); //[cite: 10]
            SyncPathwayPacket.register(registrar); //[cite: 10]

            // 👁️ 注册灵视专属包（服务器 -> 客户端）
            SyncVisionPacket.register(registrar);
            // ⚡ 注册灵视切换包（客户端 -> 服务器）
            C2SToggleVisionPacket.register(registrar);
            // 🔮 注册占卜请求包（客户端 → 服务器）
            C2SDivinationPacket.register(registrar);
            // 📖 注册知识载体占卜请求包（客户端 → 服务器）
            C2SKnowledgeDivinationPacket.register(registrar);
            // 🌿 注册群系占卜请求包（客户端 → 服务器）
            C2SBiomeDivinationPacket.register(registrar);
            // 🏛️ 注册结构占卜请求包（客户端 → 服务器）
            C2SStructureDivinationPacket.register(registrar);
            // 🏛️ 注册结构列表请求/响应包
            C2SRequestStructuresPacket.register(registrar);
            S2CStructureListPacket.register(registrar);
            // 📜 注册已学配方同步包（服务端 → 客户端）
            SyncLearnedRecipesPacket.register(registrar);
        });
    }
}