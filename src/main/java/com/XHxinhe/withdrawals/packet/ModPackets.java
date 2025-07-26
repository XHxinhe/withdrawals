package com.XHxinhe.withdrawals.packet;

import com.XHxinhe.withdrawals.Withdrawals;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking; // 新增：S2C数据包需要这个
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

/**
 * 负责注册和管理所有网络数据包的类。
 */
public class ModPackets {

    // --- 你的 C2S (客户端 -> 服务端) 数据包ID ---
    public static final Identifier GIVE_ITEM_ID = new Identifier(Withdrawals.MODID, "give_item");
    public static final Identifier CSGO_PROGRESS_ID = new Identifier(Withdrawals.MODID, "csgo_progress");
    public static final Identifier LOOK_ITEM_ID = new Identifier(Withdrawals.MODID, "look_item");
    public static final Identifier UPDATE_MODE_ID = new Identifier(Withdrawals.MODID, "update_mode");

    // --- 可以在这里定义 S2C (服务端 -> 客户端) 数据包ID ---
    // public static final Identifier OPEN_BOX_EFFECT_ID = new Identifier(Withdrawals.MODID, "open_box_effect");


    /**
     * 注册所有从客户端发送到服务端 (C2S) 的数据包。
     * 这个方法在 Mod 的通用初始化 onInitialize() 中调用。
     */
    public static void registerC2SPackets() {
        // 你的代码被完整保留
        ServerPlayNetworking.registerGlobalReceiver(GIVE_ITEM_ID, PacketGiveItem::receive);
        ServerPlayNetworking.registerGlobalReceiver(CSGO_PROGRESS_ID, PacketCsgoProgress::receive);
        ServerPlayNetworking.registerGlobalReceiver(LOOK_ITEM_ID, PacketLookItem::receive);
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_MODE_ID, PacketUpdateMode::receive);
    }

    /**
     * 注册所有从服务端发送到客户端 (S2C) 的数据包。
     * 这个方法必须在 Mod 的客户端初始化 onInitializeClient() 中调用。
     *
     * 这就是之前缺少的、导致编译错误的方法。
     */
    public static void registerS2CPackets() {
        // 目前你的逻辑可能还不需要从服务端主动给客户端发消息，
        // 所以这里暂时是空的。但这个方法的存在解决了编译问题。
        // 未来如果需要，可以在这里添加S2C数据包的接收逻辑。
        // 例如: ClientPlayNetworking.registerGlobalReceiver(OPEN_BOX_EFFECT_ID, ...);
    }
}