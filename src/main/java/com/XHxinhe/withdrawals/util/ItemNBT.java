package com.XHxinhe.withdrawals.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryOps; // 新增导入
import net.minecraft.registry.RegistryWrapper;

/**
 * 用于处理物品NBT和序列化的工具类
 */
public class ItemNBT {
    /**
     * 将一个包含物品数据的JSON字符串转换为ItemStack对象。
     * @param itemData 物品的JSON序列化字符串
     * @param registries 用于解码的注册表包装器，在1.20.2及以后版本中是必需的
     * @return 解析出的ItemStack对象，如果解析失败则返回ItemStack.EMPTY
     */
    public static ItemStack getStackFromJson(String itemData, RegistryWrapper.WrapperLookup registries) {
        // 1. 创建一个包含注册表信息的 RegistryOps 实例
        final RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registries);

        // 2. 使用新的 ops 实例进行解析
        return ItemStack.CODEC.parse(ops, JsonParser.parseString(itemData))
                .result()
                .orElse(ItemStack.EMPTY);
    }
}