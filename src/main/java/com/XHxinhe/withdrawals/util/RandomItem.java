package com.XHxinhe.withdrawals.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 用于根据权重随机选择物品的工具类。
 * 包含两个主要功能：
 * 1. 根据权重随机决定一个稀有度等级。
 * 2. 从物品池中根据指定的稀有度等级随机挑选一个物品。
 */
public class RandomItem {

    /**
     * [已修正] 根据给定的权重数组和玩家幸运值，随机一个稀有度等级。
     * 此方法使用标准的累积权重算法，确保每个等级的抽中概率与其权重成正比。
     *
     * @param random 随机数生成器
     * @param array  权重数组，索引必须对应 [0:金, 1:红, 2:粉, 3:紫, 4:蓝] 的权重
     * @param player 玩家实体，用于获取幸运值以微调概率
     * @return 随机出的稀有度等级 (5:金, 4:红, 3:粉, 2:紫, 1:蓝)
     */
    public static int randomItemsGrade(Random random, int[] array, PlayerEntity player) {
        // 玩家的幸运属性可以增加获得高稀有度物品的概率
        // 这里我们将幸运值加到最高稀有度的几个等级上
        int luck = (int) player.getLuck() * 2;

        // 1. 获取并调整各个稀有度的权重
        // 确保权重值不会因为幸运值为负而变成负数
        int gold = Math.max(1, array[0] + luck);
        int red = Math.max(1, array[1] + luck);
        int pink = Math.max(1, array[2] + luck);
        int purple = Math.max(1, array[3]);
        int blue = Math.max(1, array[4]);

        // 2. 计算总权重
        int totalWeight = gold + red + pink + purple + blue;
        if (totalWeight <= 0) {
            return 1; // 如果总权重为0或负数，返回最低等级作为保底
        }

        // 3. 生成一个 0 到 (totalWeight - 1) 之间的随机数
        int r = random.nextInt(totalWeight);

        // 4. 使用累积权重算法判断随机数所在的区间
        // 将权重想象成在一条线上划分的连续片段，随机数落在哪个片段，就代表抽中了哪个等级。
        if (r < gold) {
            return 5; // 落在金色区间
        }
        if (r < gold + red) {
            return 4; // 落在红色区间
        }
        if (r < gold + red + pink) {
            return 3; // 落在粉色区间
        }
        if (r < gold + red + pink + purple) {
            return 2; // 落在紫色区间
        }

        // 如果以上都不是，那么随机数必然落在了最后的蓝色区间
        return 1; // 蓝
    }

    /**
     * [代码正确，无需修改]
     * 从一个Map中根据指定的稀有度等级随机选择一个物品。
     *
     * @param random 随机数生成器
     * @param grade  目标稀有度等级 (1-5)
     * @param map    物品池，Key为物品堆栈，Value为该物品的稀有度等级
     * @return 随机选中的ItemStack。如果该等级没有任何物品，则返回一个空的ItemStack。
     */
    public static ItemStack randomItems(Random random, int grade, Map<ItemStack, Integer> map) {
        // 创建一个列表，用于存放所有符合目标稀有度的物品
        List<ItemStack> itemsOfGrade = new ArrayList<>();

        // 遍历整个物品池
        for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
            // 如果物品的稀有度等级与目标等级相匹配
            if (entry.getValue() == grade) {
                // 将该物品添加到列表中
                itemsOfGrade.add(entry.getKey());
            }
        }

        // 如果列表中没有任何物品（即该稀有度等级下没有配置任何物品）
        if (itemsOfGrade.isEmpty()) {
            // 返回一个空的物品堆栈，防止游戏崩溃
            return ItemStack.EMPTY;
        }

        // 从筛选后的列表中随机选择一个物品
        // random.nextInt(size) 会生成一个 0 到 (size - 1) 的随机索引
        int randomIndex = random.nextInt(itemsOfGrade.size());

        // 获取选中的物品并返回它的一个副本（.copy()），这是一个非常好的习惯，
        // 可以防止后续操作意外修改了原始物品池中的原型。
        return itemsOfGrade.get(randomIndex).copy();
    }
}