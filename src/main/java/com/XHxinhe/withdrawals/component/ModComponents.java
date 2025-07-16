package com.XHxinhe.withdrawals.component;

import com.XHxinhe.withdrawals.Withdrawals;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class ModComponents implements EntityComponentInitializer {

    // 1. 创建一个 ComponentKey，这是访问组件的唯一标识符
    public static final ComponentKey<CsboxComponent> CSBOX_COMPONENT =
            ComponentRegistry.getOrCreate(new Identifier(Withdrawals.MODID, "csbox"), CsboxComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // 2. 将组件附加到玩家实体上
        // 每当一个新的 PlayerEntity 实例被创建时，这个工厂就会被调用
        registry.registerForPlayers(CSBOX_COMPONENT, PlayerCsboxComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}