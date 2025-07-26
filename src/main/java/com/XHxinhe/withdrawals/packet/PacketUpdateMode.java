package com.XHxinhe.withdrawals.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender; // [修正] 导入正确的类
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PacketUpdateMode {

    // [修正] 将 LoginQueryResponseSender 替换为 PacketSender
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int mode = buf.readInt();

        server.execute(() -> {
            // Forge的Capability系统需要用Fabric的替代方案，例如 Cardinal Components API。
            // 暂时，我们无法直接实现 iCsboxCap.setMode(mode);
            // 你需要先实现一个等价的组件系统来存储玩家的自定义数据。
            // 这里先打印一条日志作为占位符。
            System.out.println("Player " + player.getName().getString() + " tried to update mode to " + mode + ". (Capability system not implemented yet)");
        });
    }
}