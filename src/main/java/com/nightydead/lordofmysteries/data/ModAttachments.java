package com.nightydead.lordofmysteries.data;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 模组数据附加组件（Attachment）注册类
 * 负责在 1.21.1 架构下利用 NeoForge Attachment 核心将超凡属性持久化挂载到实体上
 */
public class ModAttachments {

    /** Attachment 类型延迟注册表，使用模组 ID 作为命名空间 */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LordofMysteries.MODID);

    /**
     * 玩家非凡数据附加组件
     * 自动挂载 PlayerData，依赖其内部的序列化 CODEC 闭环读写存储
     */
    public static final Supplier<AttachmentType<PlayerData>> PLAYER_DATA = ATTACHMENT_TYPES.register(
            "player_data", () -> AttachmentType.builder(PlayerData::new)
                    .serialize(PlayerData.CODEC)
                    .build()
    );

    // ✨ 未来留空：可在下方无缝增加如外神污染源、特定超凡生物（诸如失控者）的独立附件注册

    /**
     * 将 Attachment 注册表绑定到模组事件总线
     */
    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}