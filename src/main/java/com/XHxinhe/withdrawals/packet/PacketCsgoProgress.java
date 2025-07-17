// 文件路径: src/main/java/com/XHxinhe/withdrawals/packet/PacketCsgoProgress.java
// (请用下面的完整代码替换你的文件内容)

package com.XHxinhe.withdrawals.packet;

import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.screen.CsboxScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PacketCsgoProgress {

    /**
     * 在服务器端接收并处理来自客户端的统一开箱请求。
     */
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {

        // 1. 从数据包中读取客户端发送的数据
        boolean needsKey = buf.readBoolean();
        String keyIdString = "";
        if (needsKey) {
            keyIdString = buf.readString();
        }
        final String finalKeyIdString = keyIdString; // 在lambda中需要final变量

        // 2. 确保所有对玩家的操作都在服务器的主线程上执行
        server.execute(() -> {
            // 检查玩家手上拿的是不是箱子，防止作弊或意外
            if (!(player.getMainHandStack().getItem() instanceof ItemCsgoBox)) {
                return; // 如果手上不是箱子，直接中断操作
            }

            // 3. 消耗箱子本身
            player.getMainHandStack().decrement(1);

            // 4. 如果需要钥匙，执行消耗钥匙的逻辑
            if (needsKey && !finalKeyIdString.isEmpty()) {
                Identifier keyId = new Identifier(finalKeyIdString);
                // 遍历玩家物品栏，找到对应的钥匙并消耗一个
                for (int i = 0; i < player.getInventory().main.size(); i++) {
                    ItemStack stack = player.getInventory().main.get(i);
                    if (Registries.ITEM.getId(stack.getItem()).equals(keyId)) {
                        stack.decrement(1); // 数量减1
                        break; // 消耗一个后就停止查找
                    }
                }
            }

            // 5. 为玩家打开抽奖容器界面
            // 这会触发客户端自动打开与 CsboxScreenHandler 关联的 CsboxProgressScreen
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inventory, p) -> new CsboxScreenHandler(syncId, inventory),
                    Text.translatable("container.withdrawals.csgo_box") // GUI的标题
            ));
        });
    }
}