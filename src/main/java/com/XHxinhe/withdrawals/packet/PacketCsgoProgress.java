package com.XHxinhe.withdrawals.packet;

import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import net.fabricmc.fabric.api.networking.v1.PacketSender; // [修正] 导入正确的类
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PacketCsgoProgress {

    // [修正] 将 LoginQueryResponseSender 替换为 PacketSender
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int buttonID = buf.readInt();
        String itemKeyStr = buf.readString();

        server.execute(() -> {
            if (buttonID == 2) {
                if (player.getMainHandStack().getItem() instanceof ItemCsgoBox) {
                    player.getMainHandStack().decrement(1);

                    Identifier itemToConsumeId = new Identifier(itemKeyStr);
                    for (ItemStack stack : player.getInventory().main) {
                        if (Registries.ITEM.getId(stack.getItem()).equals(itemToConsumeId)) {
                            stack.decrement(1);
                            break;
                        }
                    }
                }
            }
        });
    }
}