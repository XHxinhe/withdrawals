package com.XHxinhe.withdrawals.event;

import com.XHxinhe.withdrawals.config.CsgoBoxManage;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Random;

public class ModEvents {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(ModEvents::onLivingDeath);
    }

    private static void onLivingDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity.getWorld().isClient()) {
            return;
        }

        Identifier entityTypeId = Registries.ENTITY_TYPE.getId(entity.getType());
        String entityTypeString = entityTypeId.toString();

        if (CsgoBoxManage.BOX == null || CsgoBoxManage.BOX.isEmpty()) {
            return;
        }

        Random random = new Random();
        for (ItemCsgoBox.BoxInfo info : CsgoBoxManage.BOX) {
            if (info == null || info.dropEntities == null || info.dropEntities.isEmpty()) {
                continue;
            }

            if (info.dropRate > 0 &&
                    info.dropRate > random.nextFloat() &&
                    info.dropEntities.contains(entityTypeString)) {

                ItemStack stack = new ItemStack(ModItems.ITEM_CSGO_BOX);
                ItemCsgoBox.setBoxInfo(stack, info);
                entity.dropStack(stack);
            }
        }
    }
}