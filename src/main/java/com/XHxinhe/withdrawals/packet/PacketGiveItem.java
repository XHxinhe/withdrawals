package com.XHxinhe.withdrawals.packet;

import com.XHxinhe.withdrawals.item.ItemCsgoBox;
// 确保你的 RandomItem 工具类在这里被正确导入
import com.XHxinhe.withdrawals.util.RandomItem;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class PacketGiveItem {
    private final long seed;

    // 构造函数
    public PacketGiveItem(long seed) {
        this.seed = seed;
    }

    // 从buf读取数据的构造函数
    public PacketGiveItem(PacketByteBuf buf) {
        this.seed = buf.readLong();
    }

    // 将数据写入buf
    public PacketByteBuf toBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(this.seed);
        return buf;
    }

    // 获取种子值
    public long getSeed() {
        return this.seed;
    }

    // 服务器端接收处理逻辑
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        long seed = buf.readLong();

        server.execute(() -> {
            ItemStack boxStack = player.getMainHandStack();
            if (!(boxStack.getItem() instanceof ItemCsgoBox)) {
                return;
            }

            // 提前检查并消耗钥匙，如果失败则中止
            if (!tryConsumeKeys(player, boxStack)) {
                // 可以在这里给玩家发送一条消息，提示钥匙不足
                return;
            }

            Map<ItemStack, Integer> itemList = ItemCsgoBox.getItemGroup(boxStack);
            if (itemList == null || itemList.isEmpty()) {
                return;
            }

            // --- 这是与客户端同步的核心修改 ---
            // 1. 将原始的 itemList (HashMap) 的条目放入一个 List 中，以便排序。
            List<Map.Entry<ItemStack, Integer>> sortedEntries = new ArrayList<>(itemList.entrySet());

            // 2. 对 List 进行排序。我们使用物品的注册表ID作为排序依据，
            //    这是一个稳定且唯一的标识符 (例如 "minecraft:diamond_sword")。
            //    这确保了无论在客户端还是服务器，物品的顺序都是固定的。
            sortedEntries.sort(Comparator.comparing(entry ->
                    Registries.ITEM.getId(entry.getKey().getItem()).toString()
            ));

            // 3. 创建一个新的 LinkedHashMap 来保持排序后的顺序。
            Map<ItemStack, Integer> sortedItemList = new LinkedHashMap<>();
            for (Map.Entry<ItemStack, Integer> entry : sortedEntries) {
                sortedItemList.put(entry.getKey(), entry.getValue());
            }
            // --- 核心修改结束 ---

            int[] boxRandom = ItemCsgoBox.getBoxInfo(boxStack).boxRandom;
            Random rng = new Random(seed);
            List<ItemStack> itemBuffer = new ArrayList<>(50);

            // 生成50个物品以匹配客户端
            for (int i = 0; i < 50; i++) {
                int grade = RandomItem.randomItemsGrade(rng, boxRandom, player);
                // !!! 使用排好序的 sortedItemList 来选择物品 !!!
                ItemStack itemStack = RandomItem.randomItems(rng, grade, sortedItemList);
                itemBuffer.add(itemStack);
            }

            // 使用索引45来匹配客户端的选择
            ItemStack giveItem = itemBuffer.get(45).copy();
            itemBuffer.clear();

            if (!giveItem.isEmpty()) {
                if (!player.getInventory().insertStack(giveItem)) {
                    player.dropItem(giveItem, false);
                }
            }

            // 消耗箱子
            boxStack.decrement(1);
        });
    }

    private static boolean tryConsumeKeys(PlayerEntity player, ItemStack box) {
        String keyIdStr = ItemCsgoBox.getBoxInfo(box).boxKey;
        if (keyIdStr == null || keyIdStr.isBlank()) {
            return true;
        }

        Identifier keyId = new Identifier(keyIdStr);

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(keyId)) {
                stack.decrement(1);
                return true;
            }
        }

        return false;
    }
}