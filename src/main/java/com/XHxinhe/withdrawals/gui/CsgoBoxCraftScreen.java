package com.XHxinhe.withdrawals.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.XHxinhe.withdrawals.config.CsgoBoxManage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsgoBoxCraftScreen extends HandledScreen<CsgoBoxCraftScreenHandler> {

    private static final Identifier TEXTURE = new Identifier("withdrawals", "textures/screens/csgo_table.png");
    private TextFieldWidget boxNameField;
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public CsgoBoxCraftScreen(CsgoBoxCraftScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // 调整背景尺寸以匹配贴图和槽位布局
        this.backgroundWidth = 176; // 176 -> 212
        this.backgroundHeight = 166; // 166 -> 240
    }

    @Override
    protected void init() {
        super.init();
        // 将标题居中
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        // 创建箱子名称输入框
        this.boxNameField = new TextFieldWidget(this.textRenderer, this.x + 63, this.y + 42, 60, 12, Text.translatable("gui.withdrawals.csgo_box_craft.box_name"));
        this.boxNameField.setSuggestion(Text.translatable("gui.withdrawals.csgo_box_craft.box_name").getString());
        this.boxNameField.setMaxLength(32);
        this.addDrawableChild(this.boxNameField);

        // 创建“生成”按钮
        ButtonWidget createButton = ButtonWidget.builder(Text.translatable("gui.withdrawals.csgo_box_craft.button_down"), button -> {
            if (boxNameField != null && !boxNameField.getText().isEmpty()) {
                try {
                    CsgoBoxManage.updateBoxJson(
                            boxNameField.getText(),
                            itemListExport(),
                            gradeListExport()
                    );
                    // 可以在这里给玩家发送一条成功消息
                    if (this.client != null && this.client.player != null) {
                        this.client.player.sendMessage(Text.translatable("gui.withdrawals.csgo_box_craft.success", boxNameField.getText()), false);
                    }
                } catch (IOException ex) {
                    // 可以在这里记录错误或通知玩家
                    if (this.client != null && this.client.player != null) {
                        this.client.player.sendMessage(Text.translatable("gui.withdrawals.csgo_box_craft.fail"), true);
                    }
                    throw new RuntimeException(ex);
                }
            }
        }).dimensions(this.x + 146, this.y + 31, 40, 20).build();
        this.addDrawableChild(createButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.boxNameField.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        context.drawTexture(TEXTURE, this.x - 4, this.y - 38, 0, 0, 512, 512, 512, 512);
        RenderSystem.disableBlend();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不再需要渲染标题，因为 HandledScreen 会自动处理
        // context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
    }

    // 导出物品列表为JSON字符串
    private List<String> itemListExport() {
        List<String> itemNameList = new ArrayList<>();
        for (int i = 0; i < 35; i++) {
            Slot slot = this.handler.getSlot(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                // 使用 Codec 将 ItemStack 序列化为 NBT，然后转为 JSON 字符串
                NbtCompound nbt = new NbtCompound();
                stack.writeNbt(nbt);
                itemNameList.add(nbt.toString());
            }
        }
        return itemNameList;
    }

    // 导出每个物品对应的等级
    private List<Integer> gradeListExport() {
        List<Integer> itemGradeList = new ArrayList<>();
        for (int i = 0; i < 35; i++) {
            Slot slot = this.handler.getSlot(i);
            if (!slot.getStack().isEmpty()) {
                int grade;
                if (i < 7) grade = 1;       // 0-6
                else if (i < 12) grade = 2; // 7-11
                else if (i < 15) grade = 3; // 12-14
                else if (i < 17) grade = 4; // 15-16
                else grade = 5;             // 17-34
                itemGradeList.add(grade);
            }
        }
        return itemGradeList;
    }
}