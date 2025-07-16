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
                        // 添加物品到物品栏
                        entries.add(ITEM_CSGO_CRAFT);
                        entries.add(ITEM_CSGO_KEY0);
                        entries.add(ITEM_CSGO_KEY1);
                        entries.add(ITEM_CSGO_KEY2);
                        entries.add(ITEM_CSGO_KEY3);

                        // 从配置文件动态添加所有已定义的箱子
                        if (CsgoBoxManage.BOX != null) {
                            for (ItemCsgoBox.BoxInfo info : CsgoBoxManage.BOX) {
                                ItemStack stack = new ItemStack(ITEM_CSGO_BOX);
                                ItemCsgoBox.setBoxInfo(stack, info);
                                entries.add(stack);
                            }
                        }
                    }).build());


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("withdrawals", name), item);
    }

    public static void registerModItems() {
        // 这个方法只是为了确保类被加载，从而执行静态初始化块中的注册代码。
        // 可以在你的主Mod初始化类中调用它。
        System.out.println("Registering Mod Items for withdrawals");
    }
}