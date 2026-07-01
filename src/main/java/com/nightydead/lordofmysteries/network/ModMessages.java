package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 统一网络通道管理类
 */
public class ModMessages {

    /**
     * ✨ 核心：对外提供的注册接口，完美对齐物品栏注册风格！
     * 在你的模组主类（LordofMysteries.java）构造函数里直接调用：ModMessages.register(modEventBus);
     */
    public static void register(IEventBus modEventBus) {
        // 利用 Java 的 lambda 表达式，直接监听网络注册事件
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            final PayloadRegistrar registrar = event.registrar(LordofMysteries.MODID);

            // 🌟 顺次调用每个数据包内部自己写好的注册方法！
            SyncSanityPacket.register(registrar);
            SyncSpiritualityPacket.register(registrar);
            SyncDigestionPacket.register(registrar); // 调用刚才加在末尾的方法
            SyncPathwayPacket.register(registrar);
        });
    }
}