package com.XHxinhe.withdrawals.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 用于根据权重随机选择物品的工具类
 */
public class RandomItem {

    /**
     * 根据给定的概率数组和玩家幸运值，随机一个稀有度等级。
     * @param random 随机数生成器
     * @param array 概率数组，索引对应 [金, 红, 粉, 紫, 蓝] 的权重
     * @param player 玩家实体，用于获取幸运值
     * @return 随机出的稀有度等级 (1-5)
     */
    public static int randomItemsGrade(Random random, int[] array, PlayerEntity player) {
        // 玩家的幸运属性可以影响概率
        int luck = (int) player.getLuck() * 2;

        // 各稀有度权重
        int gold = array[0]; // 金
        int red = array[1];  // 红
        int pink = array[2]; // 粉
        int purple = array[3]; // 紫
        int blue = array[4]; // 蓝

        int sumGrade = gold + red + pink + purple + blue;
        if (sumGrade <= 0) return 1; // 防止除零或负数错误

        int r = random.nextInt(sumGrade);
        int randomAdd = 0;

        // 权重轮盘算法
        randomAdd += gold + luck;
        if (r < randomAdd) {
            return 5; // 金
        }
        randomAdd += red + luck;
        if (r < randomAdd) {
            return 4; // 红
        }
        randomAdd += pink + luck;
        if (r < randomAdd) {
            return 3; // 粉
        }
        randomAdd += purple + luck;
        if (r < randomAdd) {
            return 2; // 紫
        }
        return 1; // 蓝
    }

    /**
     * 从一个Map中根据指定的稀有度等级随机选择一个物品。
     * @param random 随机数生成器
     * @param grade  目标稀有度等级
     * @param map    包含所有物品及其稀有度等级的Map
     * @return 随机选中的ItemStack，如果该等级没有物品则可能出错
     */
    public static ItemStack randomItems(Random random, int grade, Map<ItemStack, Integer> map) {
        List<ItemStack> items = new ArrayList<>();

        // 筛选出所有符合目标稀有度的物品
        for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
            if (entry.getValue() == grade) {
                items.add(entry.getKey());
            }
        }

        // 如果没有该等级的物品，返回空物品堆栈
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 从筛选后的列表中随机选择一个
        int i = random.nextInt(items.size());
        return items.get(i).copy(); // 返回副本以防修改原始数据
    }
}