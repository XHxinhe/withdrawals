package com.XHxinhe.withdrawals.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomItem {

    /**
     * [修改版] 根据代码中写死的权重，随机一个稀有度等级。
     * 这个版本会忽略箱子数据文件中的权重，强制使用下面定义的概率。
     *
     * @param random 随机数生成器
     * @param array  (此参数被忽略) 从箱子数据传来的权重数组
     * @param player (此参数被忽略) 玩家实体
     * @return 随机出的稀有度等级 (5:金, 4:红, 3:粉, 2:紫, 1:蓝)
     */
    public static int randomItemsGrade(Random random, int[] array, PlayerEntity player) {

        // --- 在这里直接修改权重 ---
        // 你可以随意修改下面的数字来调整概率，它们的顺序是 [金, 红, 粉, 紫, 蓝]
        // 当前这套权重会让蓝色概率降低到 60%
        int goldWeight = 100;    // 金色权重 (1%)
        int redWeight = 300;     // 红色权重 (3%)
        int pinkWeight = 800;    // 粉色权重 (8%)
        int purpleWeight = 2800; // 紫色权重 (28%)
        int blueWeight = 6000;   // 蓝色权重 (60%)
        // --- 权重修改区结束 ---


        // 基于上面写死的权重计算总权重
        int totalWeight = goldWeight + redWeight + pinkWeight + purpleWeight + blueWeight;
        if (totalWeight <= 0) {
            return 1; // 如果所有权重都是0，返回保底等级
        }

        // 生成一个 0 到 (总权重-1) 之间的随机数
        int r = random.nextInt(totalWeight);

        // 使用标准的累积权重算法判断区间
        if (r < goldWeight) {
            return 5; // 金
        }
        if (r < goldWeight + redWeight) {
            return 4; // 红
        }
        if (r < goldWeight + redWeight + pinkWeight) {
            return 3; // 粉
        }
        if (r < goldWeight + redWeight + pinkWeight + purpleWeight) {
            return 2; // 紫
        }

        return 1; // 蓝
    }

    /**
     * [最终修正版] 从一个Map中根据指定的稀有度等级随机选择一个物品。
     * (此方法是正确的，无需改动)
     */
    public static ItemStack randomItems(Random random, int grade, Map<ItemStack, Integer> map) {
        List<ItemStack> itemsOfGrade = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
            if (entry.getValue() == grade) {
                itemsOfGrade.add(entry.getKey());
            }
        }

        if (itemsOfGrade.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int randomIndex = random.nextInt(itemsOfGrade.size());
        ItemStack chosenItem = itemsOfGrade.get(randomIndex);

        return chosenItem.copy();
    }
}