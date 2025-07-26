package com.XHxinhe.withdrawals.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 用于根据权重和规则随机选择物品的工具类。
 * 注意：这是一个根据上下文推断出的实现，您可能需要根据您的具体逻辑进行调整。
 */
public class RandomItem {

    /**
     * 根据权重随机决定一个物品等级。
     * @param rng 随机数生成器
     * @param boxRandom 包含各等级权重的数组或对象 (此处假设为 int[5])
     * @param player 玩家实体 (可能用于未来的扩展，如幸运值影响)
     * @return 随机选中的等级 (例如 0-4)
     */
    public static int randomItemsGrade(Random rng, int[] boxRandom, PlayerEntity player) {
        // 这是一个示例实现。您需要用您自己的权重算法替换它。
        // 假设 boxRandom 是一个包含5个等级权重的数组 [grade0, grade1, grade2, grade3, grade4]
        int totalWeight = 0;
        for (int weight : boxRandom) {
            totalWeight += weight;
        }

        if (totalWeight <= 0) {
            return 0; // 防止除零错误，返回最低等级
        }

        int randomNum = rng.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < boxRandom.length; i++) {
            cumulativeWeight += boxRandom[i];
            if (randomNum < cumulativeWeight) {
                return i; // 返回等级索引
            }
        }

        return boxRandom.length - 1; // 理论上不会到达这里，作为保险
    }

    /**
     * 从给定的物品池中，根据等级随机选择一个物品。
     * @param rng 随机数生成器
     * @param grade 目标等级
     * @param itemList 物品池，Key是物品，Value是该物品的等级
     * @return 随机选中的物品的副本
     */
    public static ItemStack randomItems(Random rng, int grade, Map<ItemStack, Integer> itemList) {
        List<ItemStack> candidates = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : itemList.entrySet()) {
            if (entry.getValue() == grade) {
                candidates.add(entry.getKey());
            }
        }

        if (candidates.isEmpty()) {
            // 如果该等级没有任何物品，可以返回一个空物品栈或执行备用逻辑
            return ItemStack.EMPTY;
        }

        // 从候选列表中随机选择一个
        return candidates.get(rng.nextInt(candidates.size())).copy();
    }
}