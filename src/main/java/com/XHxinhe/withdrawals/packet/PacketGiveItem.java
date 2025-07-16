package com.XHxinhe.withdrawals.packet;

import com.XHxinhe.withdrawals.item.ItemCsgoBox;
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

    // [修正] 服务器端接收处理逻辑
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        long seed = buf.readLong();

        server.execute(() -> {
            ItemStack boxStack = player.getMainHandStack();
            if (!(boxStack.getItem() instanceof ItemCsgoBox)) {
                return;
            }

            if (!tryConsumeKeys(player, boxStack)) {
                return;
            }

            Map<ItemStack, Integer> itemList = ItemCsgoBox.getItemGroup(boxStack);
            int[] boxRandom = ItemCsgoBox.getBoxInfo(boxStack).boxRandom;
            Random rng = new Random(seed);
            List<ItemStack> itemBuffer = new ArrayList<>(50);

            // [修正] 生成50个物品以匹配客户端
            for (int i = 0; i < 50; i++) {
                int grade = RandomItem.randomItemsGrade(rng, boxRandom, player);
                ItemStack itemStack = RandomItem.randomItems(rng, grade, itemList);
                itemBuffer.add(itemStack);
            }

            // [修正] 使用索引45来匹配客户端的选择
            ItemStack giveItem = itemBuffer.get(45).copy();
            itemBuffer.clear();

            if (!giveItem.isEmpty()) {
                if (!player.getInventory().insertStack(giveItem)) {
                    player.dropItem(giveItem, false);
                }
            }

            boxStack.decrement(1);
        });
    }

    private static boolean tryConsumeKeys(PlayerEntity player, ItemStack box) {
        String keyIdStr = ItemCsgoBox.getBoxInfo(box).boxKey;
        if (keyIdStr == null || keyIdStr.isEmpty() || keyIdStr.isBlank()) { // [修正] 添加isBlank()检查
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