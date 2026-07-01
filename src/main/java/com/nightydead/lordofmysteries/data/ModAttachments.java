package com.nightydead.lordofmysteries.data;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LordofMysteries.MODID);

    public static final Supplier<AttachmentType<PlayerData>> PLAYER_DATA = ATTACHMENT_TYPES.register(
            "player_data", () -> AttachmentType.builder(PlayerData::new)
                    .serialize(PlayerData.CODEC)
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}