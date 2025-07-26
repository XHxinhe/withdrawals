package com.XHxinhe.withdrawals.item;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.XHxinhe.withdrawals.gui.client.CsboxScreen;
import com.XHxinhe.withdrawals.util.ItemNbtUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemCsgoBox extends Item {

    public static final String BOX_INFO_TAG = "BoxItemInfo";

    public ItemCsgoBox() {
        super(new FabricItemSettings().maxCount(16).rarity(Rarity.EPIC));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            openCsgoScreen(stack);
        }
        return TypedActionResult.consume(stack);
    }

    @Environment(EnvType.CLIENT)
    private void openCsgoScreen(ItemStack stack) {
        MinecraftClient.getInstance().setScreen(new CsboxScreen(stack));
    }

    @Override
    public Text getName(ItemStack stack) {
        BoxInfo info = getBoxInfo(stack);
        if (info != null && info.boxName != null && !info.boxName.isEmpty()) {
            return Text.literal(info.boxName);
        }
        return Text.translatable("item.withdrawals.cs_box");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        BoxInfo info = getBoxInfo(stack);
        if (info != null) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.translatable("tooltip.withdrawals.item.cs_box.contains").formatted(Formatting.DARK_GRAY));
            addGradeTooltip(tooltip, info.grade1, Formatting.BLUE);
            addGradeTooltip(tooltip, info.grade2, Formatting.DARK_PURPLE);
            addGradeTooltip(tooltip, info.grade3, Formatting.LIGHT_PURPLE);
            addGradeTooltip(tooltip, info.grade4, Formatting.RED);
            if (info.grade5 != null && !info.grade5.isEmpty()) {
                tooltip.add(Text.translatable("gui.withdrawals.csgo_box.label_gold").formatted(Formatting.GOLD));
            }
        }
    }

    private void addGradeTooltip(List<Text> tooltip, List<String> items, Formatting color) {
        if (items == null || items.isEmpty()) return;
        for (String itemNbtStr : items) {
            ItemStack itemStack = ItemNbtUtil.fromNbtString(itemNbtStr);
            if (!itemStack.isEmpty()) {
                MutableText component = itemStack.getName().copy();
                tooltip.add(component.formatted(color));
            }
        }
    }

    public static int[] getRandom(ItemStack stack) {
        BoxInfo info = getBoxInfo(stack);
        if (info != null && info.boxRandom != null && info.boxRandom.length == 5) {
            return info.boxRandom;
        }
        return new int[]{1, 9, 20, 30, 40};
    }

    public static Map<ItemStack, Integer> getItemGroup(ItemStack stack) {
        Map<ItemStack, Integer> itemsMap = new LinkedHashMap<>();
        BoxInfo info = getBoxInfo(stack);
        if (info == null) return itemsMap;
        if(info.grade1 != null) info.grade1.forEach(s -> itemsMap.put(ItemNbtUtil.fromNbtString(s), 1));
        if(info.grade2 != null) info.grade2.forEach(s -> itemsMap.put(ItemNbtUtil.fromNbtString(s), 2));
        if(info.grade3 != null) info.grade3.forEach(s -> itemsMap.put(ItemNbtUtil.fromNbtString(s), 3));
        if(info.grade4 != null) info.grade4.forEach(s -> itemsMap.put(ItemNbtUtil.fromNbtString(s), 4));
        if(info.grade5 != null) info.grade5.forEach(s -> itemsMap.put(ItemNbtUtil.fromNbtString(s), 5));
        return itemsMap;
    }

    public static String getKey(ItemStack stack) {
        BoxInfo info = getBoxInfo(stack);
        if (info != null && info.boxKey != null) {
            return info.boxKey;
        }
        return "";
    }

    public static BoxInfo getBoxInfo(ItemStack stack) {
        if (stack.getItem() instanceof ItemCsgoBox) {
            NbtCompound tag = stack.getNbt();
            if (tag != null && tag.contains(BOX_INFO_TAG, NbtElement.COMPOUND_TYPE)) {
                return BoxInfo.fromNbt(tag.getCompound(BOX_INFO_TAG));
            }
        }
        return null;
    }

    public static void setBoxInfo(ItemStack stack, BoxInfo info) {
        if (stack.getItem() instanceof ItemCsgoBox && info != null) {
            NbtCompound infoTag = new NbtCompound();
            info.writeNbt(infoTag);
            stack.getOrCreateNbt().put(BOX_INFO_TAG, infoTag);
        }
    }

    public static class BoxInfo {
        @SerializedName("name") public String boxName = "";
        @SerializedName("key") public String boxKey = "";
        @SerializedName("random") public int[] boxRandom = new int[0];
        @SerializedName("grade1") public List<String> grade1 = Lists.newArrayList();
        @SerializedName("grade2") public List<String> grade2 = Lists.newArrayList();
        @SerializedName("grade3") public List<String> grade3 = Lists.newArrayList();
        @SerializedName("grade4") public List<String> grade4 = Lists.newArrayList();
        @SerializedName("grade5") public List<String> grade5 = Lists.newArrayList();

        @SerializedName("drop_rate") public float dropRate = 0.0f;
        @SerializedName("drop_entities") public List<String> dropEntities = Lists.newArrayList();

        public BoxInfo() {}

        public static BoxInfo fromNbt(NbtCompound tag) {
            BoxInfo info = new BoxInfo();
            info.boxName = tag.getString("name");
            info.boxKey = tag.getString("key");

            if (tag.contains("random", NbtElement.INT_ARRAY_TYPE)) {
                info.boxRandom = tag.getIntArray("random");
            }
            info.grade1 = nbtListToStringList(tag.getList("grade1", NbtElement.STRING_TYPE));
            info.grade2 = nbtListToStringList(tag.getList("grade2", NbtElement.STRING_TYPE));
            info.grade3 = nbtListToStringList(tag.getList("grade3", NbtElement.STRING_TYPE));
            info.grade4 = nbtListToStringList(tag.getList("grade4", NbtElement.STRING_TYPE));
            info.grade5 = nbtListToStringList(tag.getList("grade5", NbtElement.STRING_TYPE));

            info.dropRate = tag.getFloat("drop_rate");
            info.dropEntities = nbtListToStringList(tag.getList("drop_entities", NbtElement.STRING_TYPE));

            return info;
        }

        public void writeNbt(NbtCompound tag) {
            if (this.boxName != null && !this.boxName.isEmpty()) {
                tag.putString("name", this.boxName);
            }

            tag.putString("key", this.boxKey);
            if (this.boxRandom != null && this.boxRandom.length > 0) {
                tag.putIntArray("random", this.boxRandom);
            }
            tag.put("grade1", stringListToNbtList(this.grade1));
            tag.put("grade2", stringListToNbtList(this.grade2));
            tag.put("grade3", stringListToNbtList(this.grade3));
            tag.put("grade4", stringListToNbtList(this.grade4));
            tag.put("grade5", stringListToNbtList(this.grade5));

            tag.putFloat("drop_rate", this.dropRate);
            tag.put("drop_entities", stringListToNbtList(this.dropEntities));
        }

        private static List<String> nbtListToStringList(NbtList nbtList) {
            List<String> list = Lists.newArrayList();
            if (nbtList != null) {
                nbtList.forEach(nbt -> list.add(nbt.asString()));
            }
            return list;
        }

        private static NbtList stringListToNbtList(List<String> stringList) {
            NbtList nbtList = new NbtList();
            if (stringList != null) {
                stringList.forEach(s -> nbtList.add(NbtString.of(s)));
            }
            return nbtList;
        }
    }
}