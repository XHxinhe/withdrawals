package com.XHxinhe.withdrawals.config;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.XHxinhe.withdrawals.Withdrawals;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class CsgoBoxManage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("csbox");

    public static final List<ItemCsgoBox.BoxInfo> BOX = Lists.newArrayList();

    public static void loadConfigBox() {
        try {
            if (!Files.isDirectory(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            try (var stream = Files.walk(CONFIG_DIR, 1)) {
                List<ItemCsgoBox.BoxInfo> loadedBoxes = Lists.newArrayList();
                stream.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".json")).forEach(boxConfig -> {
                    try {
                        loadedBoxes.add(GSON.fromJson(Files.newBufferedReader(boxConfig, StandardCharsets.UTF_8), ItemCsgoBox.BoxInfo.class));
                    } catch (Exception ex) {
                        Withdrawals.LOGGER.error("Failed reading box config: {}", boxConfig.getFileName().toString(), ex);
                    }
                });

                BOX.clear();
                BOX.addAll(loadedBoxes);
                Withdrawals.LOGGER.info("Loaded {} box configurations.", BOX.size());
            }
        } catch (IOException e) {
            Withdrawals.LOGGER.error("Could not load or create csbox config directory!", e);
        }
    }

    /**
     * [修正] 在方法签名中添加 "throws IOException"，以将文件写入时可能发生的异常传递给调用者。
     */
    public static void updateBoxJson(String name, List<String> item, List<Integer> grade) throws IOException {
        JsonObject newObject = new JsonObject();
        newObject.addProperty("name", name);
        newObject.addProperty("key", "withdrawals:csgo_key0");
        newObject.addProperty("drop", 0.01);

        JsonArray jsonInt = new JsonArray();
        jsonInt.add(2);
        jsonInt.add(5);
        jsonInt.add(6);
        jsonInt.add(20);
        jsonInt.add(625);
        newObject.add("random", jsonInt);

        JsonArray jsonArray0 = new JsonArray();
        jsonArray0.add("minecraft:zombie");
        newObject.add("entity", jsonArray0);

        JsonArray jsonArray1 = new JsonArray();
        JsonArray jsonArray2 = new JsonArray();
        JsonArray jsonArray3 = new JsonArray();
        JsonArray jsonArray4 = new JsonArray();
        JsonArray jsonArray5 = new JsonArray();

        if (!item.isEmpty()) {
            for (int i = 0; i < item.size(); i++) {
                switch (grade.get(i)) {
                    case 1 -> jsonArray1.add(item.get(i));
                    case 2 -> jsonArray2.add(item.get(i));
                    case 3 -> jsonArray3.add(item.get(i));
                    case 4 -> jsonArray4.add(item.get(i));
                    case 5 -> jsonArray5.add(item.get(i));
                }
            }
        }

        newObject.add("grade1", jsonArray1);
        newObject.add("grade2", jsonArray2);
        newObject.add("grade3", jsonArray3);
        newObject.add("grade4", jsonArray4);
        newObject.add("grade5", jsonArray5);

        // 现在这个调用可能会抛出 IOException，所以 updateBoxJson 也必须声明它
        writeJsonFile(CONFIG_DIR.resolve(name + ".json"), newObject);
    }

    /**
     * [修正] 移除 try-catch 块，让 IOException 直接被抛出。
     * 这样，任何文件写入失败都会通知到上层调用者。
     */
    private static void writeJsonFile(Path filePath, JsonElement jsonElement) throws IOException {
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, GSON.toJson(jsonElement), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
    }
}