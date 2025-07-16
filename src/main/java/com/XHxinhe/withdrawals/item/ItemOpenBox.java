package com.XHxinhe.withdrawals.item;

import com.XHxinhe.withdrawals.gui.CsgoBoxCraftScreenHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemOpenBox extends Item {
    public ItemOpenBox() {
        super(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            // 在服务器端打开GUI
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
                    new CsgoBoxCraftScreenHandler(syncId, inventory),
                    Text.translatable("gui.withdrawals.csgo_box_craft.title")
            ));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}