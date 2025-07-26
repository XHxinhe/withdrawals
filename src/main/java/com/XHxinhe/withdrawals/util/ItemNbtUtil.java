package com.XHxinhe.withdrawals.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * 用于处理 ItemStack 和其 NBT 字符串之间转换的工具类。
 */
public class ItemNbtUtil {

    /**
     * 从一个NBT字符串创建ItemStack。
     * 字符串格式应为 "{id:\"minecraft:item_id\", Count:1b, tag:{...}}"。
     * 例如: "{id:\"minecraft:diamond_sword\",Count:1b,tag:{Damage:10,Enchantments:[{id:\"minecraft:sharpness\",lvl:5s}]}}"
     *
     * @param nbtString 包含物品ID和NBT数据的字符串。
     * @return 创建的 ItemStack，如果格式错误或物品不存在则返回 ItemStack.EMPTY。
     */
    @NotNull
    public static ItemStack fromNbtString(String nbtString) {
        if (nbtString == null || nbtString.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try {
            // 使用Mojang提供的NBT解析器，确保字符串是合法的NBT Compound格式
            NbtCompound nbt = StringNbtReader.parse(nbtString);

            // 使用官方的 ItemStack.fromNbt 方法创建物品，这是最安全和兼容性最好的方式
            ItemStack stack = ItemStack.fromNbt(nbt);

            return stack;

        } catch (CommandSyntaxException e) {
            // 如果字符串格式不正确，打印错误并返回空物品
            System.err.println("Failed to parse NBT string for item creation: " + nbtString);
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }
}