package com.XHxinhe.withdrawals.gui;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class CsboxScreenHandler extends ScreenHandler {
    // 这个物品栏在服务器上代表了GUI中的槽位。
    // 为了纯展示，我们不需要真正的槽位，但ScreenHandler结构需要它。
    private final Inventory inventory;
    // 这是从服务器传递到客户端的关键数据：正在打开的箱子。
    public final ItemStack boxStack;

    /**
     * 客户端构造函数。当服务器发送打开屏幕的数据包时，客户端会调用这个。
     * 它从数据包中读取箱子信息，然后调用主构造函数。
     */
    public CsboxScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(2), buf.readItemStack());
    }

    /**
     * 主构造函数，服务器和客户端最终都会调用它。
     */
    public CsboxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, ItemStack boxStack) {
        super(ModScreenHandlers.CSGO_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.boxStack = boxStack; // 保存箱子信息，以便客户端的Screen可以访问
        inventory.onOpen(playerInventory.player);

        // 我们不需要在屏幕上显示这些槽位，所以把它们放在屏幕外。
        // 这是一个常用技巧，用于满足ScreenHandler的结构要求，同时不影响自定义UI。
        this.addSlot(new Slot(inventory, 0, -100, -100)); // 箱子槽
        this.addSlot(new Slot(inventory, 1, -100, -100)); // 钥匙槽 (预留)

        // 添加玩家物品栏和快捷栏，同样将它们放在屏幕外。
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        return ItemStack.EMPTY; // 在这个GUI中禁用Shift+点击
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, -100, -100));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, -100, -100));
        }
    }

    /**
     * 创建一个工厂，用于从物品 (`ItemCsgoBox`) 中打开此GUI。
     * 这是打开GUI的推荐方式，因为它能将物品数据附加到打开事件中。
     */
    public static ExtendedScreenHandlerFactory createFactory(ItemStack boxStack) {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                // 在这里，我们将箱子的ItemStack写入数据包，发送给客户端
                buf.writeItemStack(boxStack);
            }

            @Override
            public Text getDisplayName() {
                // GUI的标题
                return boxStack.getName();
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                // 在服务器上创建一个2格的临时物品栏
                SimpleInventory simpleInventory = new SimpleInventory(2);
                simpleInventory.setStack(0, boxStack); // 将箱子放入
                // 这里可以加入寻找并放入钥匙的逻辑
                return new CsboxScreenHandler(syncId, playerInventory, simpleInventory, boxStack);
            }
        };
    }
}