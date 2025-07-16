package com.XHxinhe.withdrawals.screen;

import com.XHxinhe.withdrawals.gui.CsgoBoxCraftScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    // 保留原有的 CSGO_BOX_CRAFT_SCREEN_HANDLER
    public static ScreenHandlerType<CsgoBoxCraftScreenHandler> CSGO_BOX_CRAFT_SCREEN_HANDLER;

    // 添加新的 CSGO_SCREEN_HANDLER
    public static ScreenHandlerType<CsboxScreenHandler> CSGO_SCREEN_HANDLER;

    public static void registerAllScreenHandlers() {
        // 注册原有的处理器
        CSGO_BOX_CRAFT_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier("withdrawals", "csgo_box_craft"),
                new ScreenHandlerType<>(CsgoBoxCraftScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        // 注册新的处理器
        CSGO_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier("withdrawals", "csgo_box"),
                new ScreenHandlerType<>(CsboxScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );
    }
}