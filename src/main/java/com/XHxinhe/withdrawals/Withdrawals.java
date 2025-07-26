package com.XHxinhe.withdrawals;

import com.XHxinhe.withdrawals.config.CsgoBoxManage;
import com.XHxinhe.withdrawals.event.ModEvents;
import com.XHxinhe.withdrawals.gui.client.CsboxProgressScreen;
import com.XHxinhe.withdrawals.item.ModItems;
import com.XHxinhe.withdrawals.packet.ModPackets;
import com.XHxinhe.withdrawals.screen.ModScreenHandlers;
import com.XHxinhe.withdrawals.sounds.ModSounds;
import com.XHxinhe.withdrawals.util.BlurHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.XHxinhe.withdrawals.gui.CsgoBoxCraftScreen;

import java.lang.management.ManagementFactory;

public class Withdrawals implements ModInitializer, ClientModInitializer {
    public static final String MODID = "withdrawals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static {
        try {
            checkDependencies();
        } catch (Exception e) {
            forceExit();
        }
    }

    private static void checkDependencies() {
        try {
            // 检查必需的类
            Class.forName("net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback");
            Class.forName("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking");

            // 检查必需的mod
            if (!FabricLoader.getInstance().isModLoaded("fabric") ||
                    !FabricLoader.getInstance().isModLoaded("aliveandwell")) {
                forceExit();
            }

            // 检查mod版本，只要是5.x版本即可
            FabricLoader.getInstance().getModContainer("aliveandwell").ifPresent(mod -> {
                String version = mod.getMetadata().getVersion().getFriendlyString();
                // 我们放宽条件，只要是 5.x 的版本都可以
                if (!version.startsWith("5.")) {
                    LOGGER.warn("注意: 前置Mod 'aliveandwell' 的版本 (" + version + ") 可能与推荐版本 (5.1.0) 不完全匹配，但这通常不会导致问题。");
                }
            });

        } catch (Exception e) {
            forceExit();
        }
    }

    private static void forceExit() {
        try {
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            String os = System.getProperty("os.name").toLowerCase();

            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("taskkill", "/F", "/PID", pid);
            } else {
                processBuilder = new ProcessBuilder("kill", "-9", pid);
            }

            // 重定向输出，防止显示命令窗口
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            processBuilder.start();

            // 以防万一进程没有被杀死
            Thread.sleep(100);
            Runtime.getRuntime().halt(1);
        } catch (Exception ignored) {
            Runtime.getRuntime().halt(1);
        }
    }

    @Override
    public void onInitialize() {
        // 再次检查
        checkDependencies();

        LOGGER.info("《提款》Mod 正在进行通用初始化...");

        ModItems.registerModItems();
        ModSounds.registerSounds();
        ModScreenHandlers.registerAllScreenHandlers();
        ModPackets.registerC2SPackets();
        CsgoBoxManage.loadConfigBox();
        ModEvents.register();


        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("《提款》Mod: 服务器已启动！");
        });
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("《提款》Mod 正在进行客户端初始化...");

        HandledScreens.register(ModScreenHandlers.CSGO_SCREEN_HANDLER, CsboxProgressScreen::new);
        HandledScreens.register(ModScreenHandlers.CSGO_BOX_CRAFT_SCREEN_HANDLER, CsgoBoxCraftScreen::new);

        ModPackets.registerS2CPackets();
        BlurHandler.register();
    }
}