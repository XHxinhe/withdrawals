package com.XHxinhe.withdrawals.item;

import com.XHxinhe.withdrawals.config.CsgoBoxManage;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class ModItems {

    // 注册物品
    public static final Item ITEM_CSGO_BOX = registerItem("csgo_box", new ItemCsgoBox());
    public static final Item ITEM_CSGO_KEY0 = registerItem("csgo_key0", new ItemCsgoKey());
    public static final Item ITEM_CSGO_KEY1 = registerItem("csgo_key1", new ItemCsgoKey());
    public static final Item ITEM_CSGO_KEY2 = registerItem("csgo_key2", new ItemCsgoKey());
    public static final Item ITEM_CSGO_KEY3 = registerItem("csgo_key3", new ItemCsgoKey());
    public static final Item ITEM_CSGO_CRAFT = registerItem("csgo_box_craft", new ItemOpenBox());

    // 注册创造模式物品栏
    public static final ItemGroup CSGO_ITEM_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier("withdrawals", "csgo_tab"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.withdrawals.csgo_tab"))
                    .icon(() -> new ItemStack(ITEM_CSGO_KEY0))
                    .entries((displayContext, entries) -> {
                        // 添加基础物品
                        entries.add(ITEM_CSGO_CRAFT);
                        entries.add(ITEM_CSGO_KEY0);
                        entries.add(ITEM_CSGO_KEY1);
                        entries.add(ITEM_CSGO_KEY2);
                        entries.add(ITEM_CSGO_KEY3);

                        // 从配置文件添加箱子，使用深拷贝避免引用问题
                        if (CsgoBoxManage.BOX != null) {
                            for (ItemCsgoBox.BoxInfo originalInfo : CsgoBoxManage.BOX) {
                                if (originalInfo != null) {
                                    // 创建新的BoxInfo对象
                                    ItemCsgoBox.BoxInfo newInfo = new ItemCsgoBox.BoxInfo();
                                    // 复制必要的属性
                                    newInfo.boxName = originalInfo.boxName;
                                    newInfo.boxKey = originalInfo.boxKey;
                                    newInfo.boxRandom = originalInfo.boxRandom != null ?
                                            originalInfo.boxRandom.clone() : new int[0];

                                    // 复制物品列表
                                    if (originalInfo.grade1 != null)
                                        newInfo.grade1 = new ArrayList<>(originalInfo.grade1);
                                    if (originalInfo.grade2 != null)
                                        newInfo.grade2 = new ArrayList<>(originalInfo.grade2);
                                    if (originalInfo.grade3 != null)
                                        newInfo.grade3 = new ArrayList<>(originalInfo.grade3);
                                    if (originalInfo.grade4 != null)
                                        newInfo.grade4 = new ArrayList<>(originalInfo.grade4);
                                    if (originalInfo.grade5 != null)
                                        newInfo.grade5 = new ArrayList<>(originalInfo.grade5);

                                    // 创建新的ItemStack并设置信息
                                    ItemStack boxStack = new ItemStack(ITEM_CSGO_BOX);
                                    ItemCsgoBox.setBoxInfo(boxStack, newInfo);
                                    entries.add(boxStack);
                                }
                            }
                        }
                    }).build());

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("withdrawals", name), item);
    }

    public static void registerModItems() {
        System.out.println("Registering Mod Items for withdrawals");
    }
}