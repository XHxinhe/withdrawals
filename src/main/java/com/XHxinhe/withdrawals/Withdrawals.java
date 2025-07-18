package com.XHxinhe.withdrawals;

import com.XHxinhe.withdrawals.config.CsgoBoxManage;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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

            // 检查mod版本 (如果需要特定版本)
            FabricLoader.getInstance().getModContainer("aliveandwell").ifPresent(mod -> {
                String version = mod.getMetadata().getVersion().getFriendlyString();
                if (!version.startsWith("4.1.")) {
                    forceExit();
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
        createDefaultConfig();
        CsgoBoxManage.loadConfigBox();

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

    private void createDefaultConfig() {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir();
            Path folderPath = configPath.resolve("csbox");

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path filePath = folderPath.resolve("default.json");

            if (!Files.exists(filePath)) {
                LOGGER.info("未找到 'default.json'，正在创建新的默认配置文件...");
                String content = """
                {
                  "name": "Weapons Supply Box",
                  "key": "withdrawals:csgo_key0",
                  "drop": 0.12,
                  "random": [2, 5, 6, 20, 625],
                  "entity": ["minecraft:zombie", "minecraft:skeleton"],
                  "grade1": ["{\\"id\\":\\"minecraft:stone_sword\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:iron_axe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:iron_shovel\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:iron_pickaxe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:iron_axe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:iron_hoe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:iron_sword\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}"],
                  "grade2": ["{\\"id\\":\\"minecraft:golden_sword\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:golden_axe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:golden_axe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:golden_pickaxe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:golden_shovel\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}"],
                  "grade3": ["{\\"id\\":\\"minecraft:diamond_shovel\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:diamond_pickaxe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:diamond_hoe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}"],
                  "grade4": ["{\\"id\\":\\"minecraft:diamond_axe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:diamond_sword\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}"],
                  "grade5": ["{\\"id\\":\\"minecraft:netherite_sword\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:netherite_axe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:netherite_pickaxe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:netherite_shovel\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}", "{\\"id\\":\\"minecraft:netherite_hoe\\",\\"Count\\":1,\\"tag\\":{\\"Damage\\":0}}"]
                }""";
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8))) {
                    writer.write(content);
                }
            }
        } catch (IOException e) {
            LOGGER.error("创建默认配置文件时发生IO异常！", e);
        }
    }
}