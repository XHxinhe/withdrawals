package com.XHxinhe.withdrawals;

import com.XHxinhe.withdrawals.gui.client.CsboxProgressScreen;
import com.XHxinhe.withdrawals.screen.CsboxScreenHandler; // 确保导入这个
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
        HandledScreens.register(ModScreenHandlers.CSGO_SCREEN_HANDLER,
                (CsboxScreenHandler handler, PlayerInventory inventory, Text title) -> {
                    // 创建屏幕实例
                    CsboxProgressScreen screen = new CsboxProgressScreen();

                    // 关键：将 handler 设置到 screen 实例中
                    // 我们假设 CsboxProgressScreen 中有 setHandler(CsboxScreenHandler handler) 方法
                    // 如果没有，请参考我之前的回答添加它
                    screen.setHandler(handler);

                    return screen;
                }
        );

        // 注册 BlurHandler 的事件回调
        BlurHandler.register();
    }
}