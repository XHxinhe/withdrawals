package com.XHxinhe.withdrawals;

import com.XHxinhe.withdrawals.gui.client.CsboxProgressScreen;
import com.XHxinhe.withdrawals.gui.CsgoBoxCraftScreen; // 确保导入这个
import com.XHxinhe.withdrawals.screen.ModScreenHandlers;
import com.XHxinhe.withdrawals.util.BlurHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class withdrawalsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 使用最终的、能解决所有类型问题的注册方法
        HandledScreens.register(ModScreenHandlers.CSGO_SCREEN_HANDLER, CsboxProgressScreen::new);
        HandledScreens.register(ModScreenHandlers.CSGO_BOX_CRAFT_SCREEN_HANDLER, CsgoBoxCraftScreen::new);
        // 注册 BlurHandler 的事件回调
        BlurHandler.register();
    }
}