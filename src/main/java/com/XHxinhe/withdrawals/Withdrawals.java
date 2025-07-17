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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Withdrawals implements ModInitializer, ClientModInitializer {
    public static final String MODID = "withdrawals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
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

        // 这行代码现在可以正常编译了
        HandledScreens.register(ModScreenHandlers.CSGO_SCREEN_HANDLER, CsboxProgressScreen::new);

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