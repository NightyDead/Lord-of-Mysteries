package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络通道统一注册管理类
 * 负责在模组初始化时监听网络注册事件，并将所有自定义数据包统一注册到 NeoForge 网络系统
 * <p>
 * 当前注册的数据包（均为服务端 → 客户端方向）：
 * - {@link SyncSanityPacket}        理智值同步
 * - {@link SyncSpiritualityPacket}  灵性值同步
 * - {@link SyncDigestionPacket}     消化进度同步
 * - {@link SyncPathwayPacket}       途径/序列同步
 */
public class ModMessages {

    /**
     * 注册所有自定义网络数据包
     * 应在模组主类构造函数中调用：ModMessages.register(modEventBus)
     *
     * @param modEventBus 模组生命周期事件总线
     */
    public static void register(IEventBus modEventBus) {
        // 监听网络载荷注册事件，获取 PayloadRegistrar 进行注册
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            final PayloadRegistrar registrar = event.registrar(LordofMysteries.MODID);

            // 依次注册各个数据包的处理器
            SyncSanityPacket.register(registrar);
            SyncSpiritualityPacket.register(registrar);
            SyncDigestionPacket.register(registrar);
            SyncPathwayPacket.register(registrar);
        });
    }
}
