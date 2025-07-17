package com.XHxinhe.withdrawals.packet;

import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PacketCsgoProgress {

    public static void receive(MinecraftServer server, ServerPlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf,
                               PacketSender responseSender) {
        int buttonID = buf.readInt();
        String itemKeyStr = buf.readString();

        server.execute(() -> {
            if (buttonID == 2) {
                // 检查主手物品是否为CSGO盒子
                ItemStack mainHandStack = player.getMainHandStack();
                if (mainHandStack.getItem() instanceof ItemCsgoBox) {
                    // 消耗主手物品
                    mainHandStack.decrement(1);

                    // 查找背包中的目标物品
                    Identifier targetItemId = new Identifier(itemKeyStr);
                    for (ItemStack stack : player.getInventory().main) {
                        if (!stack.isEmpty() &&
                                Registries.ITEM.getId(stack.getItem()).equals(targetItemId)) {
                            stack.decrement(1);
                            break; // 只消耗一个物品
                        }
                    }
                }
            }
        });
    }
}