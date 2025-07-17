package com.XHxinhe.withdrawals.screen;

import com.XHxinhe.withdrawals.gui.CsgoBoxCraftScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static ScreenHandlerType<CsgoBoxCraftScreenHandler> CSGO_BOX_CRAFT_SCREEN_HANDLER;
    public static ScreenHandlerType<CsboxScreenHandler> CSGO_SCREEN_HANDLER;

    public static void registerAllScreenHandlers() {
        // 箱子合成界面
        CSGO_BOX_CRAFT_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier("withdrawals", "csgo_box_craft"),
                new ScreenHandlerType<>(CsgoBoxCraftScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        // 开箱动画界面
        CSGO_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier("withdrawals", "csgo_box"),
                new ScreenHandlerType<>(CsboxScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );
    }
}