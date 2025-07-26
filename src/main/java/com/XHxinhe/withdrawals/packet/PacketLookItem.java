package com.XHxinhe.withdrawals.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender; // [修正] 导入正确的类
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PacketLookItem {

    // [修正] 将 LoginQueryResponseSender 替换为 PacketSender
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int buttonID = buf.readInt();

        server.execute(() -> {
            if (buttonID == 1) {
                // 执行相关逻辑
            }
        });
    }
}