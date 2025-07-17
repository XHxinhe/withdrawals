package com.XHxinhe.withdrawals.gui;

import com.XHxinhe.withdrawals.Withdrawals;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    // 用于“合成台”的ScreenHandler (保持不变)
    public static ScreenHandlerType<CsgoBoxCraftScreenHandler> CSGO_BOX_CRAFT_SCREEN_HANDLER;

    // 用于“开箱预览”的ScreenHandler，这是一个扩展类型
    public static ScreenHandlerType<CsboxScreenHandler> CSGO_SCREEN_HANDLER;

    public static void registerAllScreenHandlers() {
        Withdrawals.LOGGER.info("Registering Screen Handlers for " + Withdrawals.MODID);

        // 注册合成台 ScreenHandler
        CSGO_BOX_CRAFT_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier(Withdrawals.MODID, "csgo_box_craft"),
                new ScreenHandlerType<>(CsgoBoxCraftScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        // 注册开箱预览 ScreenHandler，使用 ExtendedScreenHandlerType
        // 它需要一个接收 PacketByteBuf 的构造函数引用 (CsboxScreenHandler::new)
        CSGO_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier(Withdrawals.MODID, "csgo_box"),
                new ExtendedScreenHandlerType<>(CsboxScreenHandler::new)
        );
    }
}