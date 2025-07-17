package com.XHxinhe.withdrawals.gui.client;

import com.XHxinhe.withdrawals.gui.CsboxScreenHandler;
import com.XHxinhe.withdrawals.gui.widget.TexturedButtonWithText;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.packet.ModPackets;
import com.XHxinhe.withdrawals.util.BlurHandler;
import com.XHxinhe.withdrawals.util.GuiItemMove;
import com.XHxinhe.withdrawals.util.IconListTools;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CsboxScreen extends HandledScreen<CsboxScreenHandler> {

    private final ItemStack itemMenu;
    private final Map<ItemStack, Integer> itemGroup;
    private final List<ItemStack> itemsList;
    private final List<Integer> gradeList;
    private ItemStack itemKey = ItemStack.EMPTY;
    private int boxKeyCount = 0;

    public float itemRotX;
    public float itemRotY;
    private int gameTick = 0;

    public CsboxScreen(CsboxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.itemMenu = handler.boxStack;

        if (this.itemMenu.getItem() instanceof ItemCsgoBox) {
            this.itemGroup = ItemCsgoBox.getItemGroup(this.itemMenu);
            this.itemsList = itemsListProgress(this.itemGroup);
            this.gradeList = gradeListProgress(this.itemGroup);

            String keyId = ItemCsgoBox.getKey(this.itemMenu);
            if (keyId != null && !keyId.isEmpty()) {
                Registries.ITEM.getOrEmpty(new Identifier(keyId))
                        .ifPresent(item -> this.itemKey = new ItemStack(item));
            }
        } else {
            this.itemGroup = Map.of();
            this.itemsList = List.of();
            this.gradeList = List.of();
        }
        this.playerInventoryTitleY = -1000;
        this.backgroundHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        BlurHandler.updateShaderState(true);

        this.addDrawableChild(new TexturedButtonWithText(
                this.width * 68 / 100, this.height * 94 / 100,
                this.width * 4 / 100, this.height * 5 / 100,
                0, 0, 64,
                new Identifier("withdrawals", "textures/screens/atlas/open_box.png"),
                82, 128,
                button -> openBox(),
                Text.translatable("gui.withdrawals.csgo_box.open_box"),
                this.textRenderer,
                0.8f
        ));

        this.addDrawableChild(new TexturedButtonWithText(
                this.width * 73 / 100, this.height * 94 / 100,
                this.width * 4 / 100, this.height * 5 / 100,
                0, 0, 64,
                new Identifier("withdrawals", "textures/screens/atlas/back_box.png"),
                82, 128,
                button -> this.close(),
                Text.translatable("gui.withdrawals.csgo_box.back_box"),
                this.textRenderer,
                0.8f
        ));
    }

    // =================================================================================
    // |                             >>> 修改点在这里 <<<                               |
    // =================================================================================
    private void openBox() {
        if (this.client == null || this.client.player == null) return;

        String keyId = ItemCsgoBox.getKey(itemMenu);
        boolean needsKey = keyId != null && !keyId.isEmpty();

        // 检查是否是箱子，并且满足钥匙条件（要么不需要钥匙，要么玩家有钥匙）
        if (itemMenu.getItem() instanceof ItemCsgoBox && (!needsKey || hasKey(keyId))) {
            // 创建数据包
            PacketByteBuf buf = PacketByteBufs.create();

            // 将钥匙的ID（可能为空字符串""）写入数据包
            // 这与服务器端的 readString() 相对应
            buf.writeString(keyId);

            // 发送数据包到服务器
            ClientPlayNetworking.send(ModPackets.CSGO_PROGRESS_ID, buf);

            // 可以在这里添加逻辑，比如禁用按钮防止重复点击，或者切换到抽奖动画屏幕
            // this.client.setScreen(new CsboxProgressScreen( ... ));
        }
    }
    // =================================================================================
    // |                             >>> 修改结束 <<<                                 |
    // =================================================================================

    private boolean hasKey(String keyId) {
        if (this.client == null || this.client.player == null) return false;
        for (ItemStack stack : this.client.player.getInventory().main) {
            Identifier stackId = Registries.ITEM.getId(stack.getItem());
            if (stackId.toString().equals(keyId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world != null) {
            context.fillGradient(0, 0, this.width, this.height, BlurHandler.getBackgroundColor(), BlurHandler.getBackgroundColor());
        } else {
            this.renderBackground(context);
        }

        renderBg(context, delta, mouseX, mouseY);
        renderLabels(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // We handle our own background
    }

    protected void renderBg(DrawContext context, float partialTicks, int gx, int gy) {
        // ** 检查 client 和 client.player 是否为空，防止在退出游戏时崩溃 **
        if (this.client == null || this.client.player == null) {
            return;
        }

        RenderSystem.enableBlend();
        this.client.options.hudHidden = true;

        context.fill(this.width * 3 / 100, this.height * 53 / 100, this.width * 97 / 100, this.height * 53 / 100 + 1, 0xFFD3D3D3);
        context.fill(this.width * 25 / 100, this.height * 92 / 100, this.width * 75 / 100, this.height * 92 / 100 + 1, 0xFFD3D3D3);

        float scale = (width * 26F / 100F) / 16F;
        // ** 修正: 使用 this.client.player 而不是 this.player **
        GuiItemMove.renderItemInInventoryFollowsMouse(context, this.width * 50 / 100, this.height * 32 / 100, this.itemRotX, this.itemRotY, itemMenu, this.client.player, scale);

        int x = 0, y = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            int py = 55, px = i;
            if (i > 9) { py = 73; px = i - 10; }
            ItemStack itemStack1 = itemsList.get(i);
            int grade = gradeList.get(i);
            x = px; y = py;
            if (grade == 5) break;
            // ** 修正: 使用 this.client.player 而不是 this.player **
            IconListTools.renderItemFrame(this.client.player, context, itemStack1, this.width * 4 / 100 + px * this.width * 9 / 100, this.height * py / 100, this.width, this.height, grade);
        }
        if (!gradeList.isEmpty() && gradeList.get(gradeList.size() - 1) == 5) {
            // ** 修正: 使用 this.client.player 而不是 this.player **
            IconListTools.renderItemFrame(this.client.player, context, ItemStack.EMPTY, this.width * 4 / 100 + x * this.width * 9 / 100, this.height * y / 100, this.width, this.height, 5);
        }

        if (!itemKey.isEmpty()) {
            // ** 修正: 使用 this.client.player 和 this.client.player.getWorld() **
            IconListTools.renderGuiItem(this.client.player, this.client.player.getWorld(), context, itemKey, this.width * 25F / 100, this.height * 93F / 100, 1);
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // We use renderLabels for text, so this is empty
    }

    protected void renderLabels(DrawContext context, int mouseX, int mouseY) {
        Style boldStyle = Style.EMPTY.withBold(true);

        renderTitleSection(context, boldStyle);
        renderItemListSection(context, boldStyle);
        renderBottomSection(context, boldStyle);
    }

    private void renderTitleSection(DrawContext context, Style boldStyle) {
        Style colorStyle = Style.EMPTY.withColor(0xFFFFFF00);
        Text titleText = Text.translatable("gui.withdrawals.csgo_box.title").fillStyle(boldStyle).fillStyle(colorStyle);
        renderText(context, titleText, middleOf(titleText.getString(), 1.5f), this.height * 5.9F / 100F, 1.5F);

        Text labelText = Text.translatable("gui.withdrawals.csgo_box.label_box");
        Text boxNameText = itemMenu.getName();
        float labelWidth = this.textRenderer.getWidth(labelText) * 0.7F;
        float boxNameWidth = this.textRenderer.getWidth(boxNameText) * 0.7F;
        float totalWidth = labelWidth + boxNameWidth + 5;
        float startX = (this.width - totalWidth) / 2;
        renderText(context, labelText, startX, this.height * 13F / 100F, 0.7F);
        renderText(context, boxNameText, startX + labelWidth + 5, this.height * 13F / 100F, 0.7F);
    }

    private void renderItemListSection(DrawContext context, Style boldStyle) {
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_items").fillStyle(boldStyle), this.width * 3F / 100F, this.height * 50.3F / 100F, 0.8F);

        int lastRenderedPx = 0;
        int lastRenderedPy = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            int py = 67, px = i;
            if (i > 9) { py = 85; px = i - 10; }
            int grade = gradeList.get(i);
            if (grade > 4) {
                lastRenderedPx = px;
                lastRenderedPy = py;
                break;
            }
            renderText(context, itemsList.get(i).getName(), this.width * 4F / 100 + px * this.width * 9F / 100, this.height * py / 100F, 0.6F);
            lastRenderedPx = px;
            lastRenderedPy = py;
        }

        if (!gradeList.isEmpty() && gradeList.get(gradeList.size() - 1) == 5) {
            renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_gold"), this.width * 4F / 100 + lastRenderedPx * this.width * 9F / 100, this.height * lastRenderedPy / 100F, 0.6F);
        }
    }

    private void renderBottomSection(DrawContext context, Style boldStyle) {
        float iconX = this.width * 25F / 100F;
        float iconY = this.height * 93F / 100F;
        float textX = iconX + 20;
        float textY = iconY + 5;

        if (!itemKey.isEmpty()) {
            if (boxKeyCount > 0) {
                renderText(context, Text.literal(" × " + boxKeyCount), textX - 5, textY, 0.8F);
            } else {
                String tip = "需要使用 1个 " + itemKey.getName().getString() + " 打开 ";
                renderText(context, Text.literal(tip), textX, textY, 0.8F);
            }
        }
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.client == null || this.client.player == null) {
            this.close();
            return;
        }

        if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
            if (gameTick % 20 == 1) {
                this.boxKeyCount = getKeyCount();
            }
            if (gameTick > 100000) gameTick = 0;
            gameTick++;
        } else {
            this.close();
        }
    }

    private int getKeyCount() {
        String keyId = ItemCsgoBox.getKey(itemMenu);
        if (keyId == null || keyId.isEmpty() || this.client == null || this.client.player == null) return 0;
        int count = 0;
        for (ItemStack stack : this.client.player.getInventory().main) {
            Identifier stackId = Registries.ITEM.getId(stack.getItem());
            if (stackId.toString().equals(keyId)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean isInRange = (mouseX >= this.width * 37F / 100 && mouseX <= this.width * 37F / 100 + 200) && (mouseY >= this.height * 12F / 100 && mouseY <= this.height * 12F / 100 + 176);
        if (button == 0 && isInRange) {
            this.itemRotX = GuiItemMove.renderRotAngleX(deltaX, this.itemRotX);
            this.itemRotY = GuiItemMove.renderRotAngleY(deltaY, this.itemRotY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void close() {
        BlurHandler.updateShaderState(false);
        if (this.client != null) {
            this.client.options.hudHidden = false;
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private float middleOf(String text, float scale) {
        return (this.width - this.textRenderer.getWidth(text) * scale) * 0.5F;
    }

    private void renderText(DrawContext context, Text text, float px, float py, float scale) {
        context.getMatrices().push();
        context.getMatrices().translate(px, py, 0);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(this.textRenderer, text, 0, 0, 0xFFD3D3D3, false);
        context.getMatrices().pop();
    }

    public static List<ItemStack> itemsListProgress(Map<ItemStack, Integer> itemList) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            for (Map.Entry<ItemStack, Integer> entry : itemList.entrySet()) {
                if (entry.getValue() == i) {
                    itemStacks.add(entry.getKey());
                }
            }
        }
        return itemStacks;
    }

    public static List<Integer> gradeListProgress(Map<ItemStack, Integer> itemList) {
        List<Integer> grades = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            for (Map.Entry<ItemStack, Integer> entry : itemList.entrySet()) {
                if (entry.getValue() == i) {
                    grades.add(i);
                }
            }
        }
        return grades;
    }
}