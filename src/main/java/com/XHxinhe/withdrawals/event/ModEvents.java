package com.XHxinhe.withdrawals.event;

import com.XHxinhe.withdrawals.Withdrawals;
import com.XHxinhe.withdrawals.config.CsgoBoxManage;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

public class ModEvents {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(ModEvents::onLivingDeath);
    }

    private static void onLivingDeath(LivingEntity entity, DamageSource damageSource) {
        // 基础检查：必须是服务器端，且攻击者是玩家
        if (entity.getWorld().isClient() || !(damageSource.getAttacker() instanceof PlayerEntity)) {
            return;
        }

        Identifier entityTypeId = Registries.ENTITY_TYPE.getId(entity.getType());
        String entityTypeString = entityTypeId.toString();
        Random random = entity.getWorld().getRandom();

        // 1. 筛选出所有可能从此怪物身上掉落的箱子
        List<ItemCsgoBox.BoxInfo> possibleDrops = new ArrayList<>();
        for (ItemCsgoBox.BoxInfo info : CsgoBoxManage.BOX) {
            // 确保 dropEntities 列表不为空且包含当前被击杀的实体ID
            if (info != null && info.dropEntities != null && info.dropEntities.contains(entityTypeString)) {
                possibleDrops.add(info);
            }
        }

        // 如果没有箱子可以从此怪物掉落，直接返回
        if (possibleDrops.isEmpty()) {
            return;
        }

        // 2. 遍历所有可能的掉落，逐一进行掉落判定
        for (ItemCsgoBox.BoxInfo chosenInfo : possibleDrops) {
            // 每个箱子都根据自己的 dropRate 进行一次独立的随机判定
            if (random.nextFloat() < chosenInfo.dropRate) {
                // 如果判定成功，则创建并掉落这个箱子
                ItemStack stack = createDropStack(chosenInfo);
                entity.dropStack(stack);
                // 注意：这里没有 return，意味着一个怪物可能掉落多个不同的箱子（如果配置如此）
                // 如果你希望一个怪物最多只掉一个箱子，可以在这里加上 return;
            }
        }
    }

    /**
     * [已简化] 创建一个包含正确NBT的箱子ItemStack。
     * @param info 从配置中读取的、包含完整信息的BoxInfo对象。
     * @return 一个准备掉落的ItemStack。
     */
    private static ItemStack createDropStack(ItemCsgoBox.BoxInfo info) {
        ItemStack stack = new ItemStack(ModItems.ITEM_CSGO_BOX);
        // 直接将从配置加载的、完整的info对象设置到NBT中。
        // 我们在ItemCsgoBox中已经确保了setBoxInfo会正确写入所有数据。
        ItemCsgoBox.setBoxInfo(stack, info);
        return stack;
    }
}