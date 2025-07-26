package com.XHxinhe.withdrawals.sounds;

import com.XHxinhe.withdrawals.Withdrawals;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    // 定义 SoundEvent
    public static final SoundEvent CS_DITA = registerSoundEvent("cs_dita");
    public static final SoundEvent CS_OPEN = registerSoundEvent("cs_open");
    public static final SoundEvent CS_FINISH = registerSoundEvent("cs_finish");

    // 辅助方法用于注册
    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(Withdrawals.MODID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    // 这个方法用于确保类被加载，从而执行注册
    public static void registerSounds() {
        Withdrawals.LOGGER.info("Registering Mod Sounds for " + Withdrawals.MODID);
    }
}