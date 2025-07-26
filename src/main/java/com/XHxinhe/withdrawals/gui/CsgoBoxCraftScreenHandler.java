package com.XHxinhe.withdrawals.gui;

import com.XHxinhe.withdrawals.screen.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class CsgoBoxCraftScreenHandler extends ScreenHandler {
    // 自定义槽位的物品栏，共35个槽位
    private final Inventory inventory;
    private static final int CUSTOM_SLOT_COUNT = 35;

    // 构造函数，当在客户端打开时调用
    public CsgoBoxCraftScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(CUSTOM_SLOT_COUNT));
    }

    // 主要构造函数
    public CsgoBoxCraftScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.CSGO_BOX_CRAFT_SCREEN_HANDLER, syncId);
        checkSize(inventory, CUSTOM_SLOT_COUNT);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        int slotIndex = 0;
        int leftPos = 20;
        int topPos = -20;

        // 添加自定义槽位
        // Grade 1 (蓝色) - 7个
        for (int i = 0; i < 7; i++) {
            this.addSlot(new Slot(inventory, slotIndex++, leftPos + i * 18, topPos));
        }
        // Grade 2 (紫色) - 5个
        for (int i = 0; i < 5; i++) {
            this.addSlot(new Slot(inventory, slotIndex++, leftPos + i * 18, topPos + 18));
        }
        // Grade 3 (粉色) - 3个
        for (int i = 0; i < 3; i++) {
            this.addSlot(new Slot(inventory, slotIndex++, leftPos + i * 18, topPos + 18 * 2));
        }
        // Grade 4 (红色) - 2个
        for (int i = 0; i < 2; i++) {
            this.addSlot(new Slot(inventory, slotIndex++, leftPos + i * 18, topPos + 18 * 3));
        }
        // Grade 5 (金色) - 18个 (2行)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, slotIndex++, leftPos + i * 18, topPos + 18 * 5));
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, slotIndex++, leftPos + i * 18, topPos + 18 * 6));
        }

        // 添加玩家物品栏
        int playerInvTop = 121;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, leftPos + j * 18, playerInvTop + i * 18));
            }
        }
        // 添加玩家快捷栏
        int playerHotbarTop = 179;
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, leftPos + i * 18, playerHotbarTop));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            // 如果点击的是自定义槽位
            if (slotIndex < CUSTOM_SLOT_COUNT) {
                if (!this.insertItem(originalStack, CUSTOM_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 如果点击的是玩家物品栏
            else if (!this.insertItem(originalStack, 0, CUSTOM_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        // 将自定义槽位中的物品返还给玩家
        this.dropInventory(player, this.inventory);
    }
}