package com.XHxinhe.withdrawals;

import com.XHxinhe.withdrawals.gui.ModScreenHandlers;
import com.XHxinhe.withdrawals.gui.client.CsboxScreen;
import com.XHxinhe.withdrawals.gui.CsgoBoxCraftScreen; // 确保你已创建这个合成台屏幕类
import com.XHxinhe.withdrawals.packet.ModPackets;
import com.XHxinhe.withdrawals.util.BlurHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class withdrawalsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Withdrawals.LOGGER.info("《提款》Mod 正在进行客户端初始化...");

        // 1. 注册“开箱预览”屏幕
        // 将 CSGO_SCREEN_HANDLER (处理 CsboxScreenHandler) 与 CsboxScreen (GUI类) 关联起来
        HandledScreens.register(ModScreenHandlers.CSGO_SCREEN_HANDLER, CsboxScreen::new);

        // 2. 注册“合成台”屏幕
        // 将 CSGO_BOX_CRAFT_SCREEN_HANDLER 与 CsgoBoxCraftScreen 关联起来
        // 确保你有一个名为 CsgoBoxCraftScreen 的类，它继承自 HandledScreen
        HandledScreens.register(ModScreenHandlers.CSGO_BOX_CRAFT_SCREEN_HANDLER, CsgoBoxCraftScreen::new);

        // 3. 注册客户端接收的网络包
        ModPackets.registerS2CPackets();

        // 4. 注册模糊效果处理器
        BlurHandler.register();
    }
}