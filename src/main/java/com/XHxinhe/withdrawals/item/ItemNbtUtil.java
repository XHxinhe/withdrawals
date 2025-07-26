package com.XHxinhe.withdrawals.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

/**
 * 物品NBT工具类。
 * 负责将物品的NBT字符串表示形式与ItemStack对象之间进行转换。
 */
public class ItemNbtUtil {

    /**
     * 从一个NBT字符串创建一个ItemStack。
     * 这个字符串通常是从配置文件（如json）中读取的。
     * 例如: "{\"id\":\"minecraft:diamond_sword\",\"Count\":1,\"tag\":{\"Damage\":0}}"
     *
     * @param nbtString 代表物品的NBT字符串。
     * @return 解析成功后的ItemStack，如果解析失败则返回一个空物品。
     */
    public static ItemStack fromNbtString(String nbtString) {
        try {
            // 1. 使用Minecraft内置的StringNbtReader来解析字符串，得到NbtCompound对象。
            NbtCompound compound = StringNbtReader.parse(nbtString);
            // 2. 使用ItemStack的静态方法从NbtCompound创建ItemStack实例。
            return ItemStack.fromNbt(compound);
        } catch (CommandSyntaxException e) {
            // 如果字符串格式不正确，解析会抛出异常。
            // 在这种情况下，我们打印一个错误日志并返回一个空物品，防止游戏崩溃。
            System.err.println("Failed to parse ItemStack from NBT string: " + nbtString);
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }
}