package com.XHxinhe.withdrawals.gui.client;

import com.XHxinhe.withdrawals.gui.widget.TexturedButtonWithText;
import com.XHxinhe.withdrawals.util.IconListTools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.packet.ModPackets;
import com.XHxinhe.withdrawals.util.BlurHandler;
import com.XHxinhe.withdrawals.util.GuiItemMove;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CsboxScreen extends Screen {

    private final World world;
    private final PlayerEntity entity;
    private final ItemStack itemMenu;
    private final Map<ItemStack, Integer> itemGroup;
    private final List<ItemStack> itemsList;
    private final List<Integer> gradeList;
    private ItemStack itemKey = ItemStack.EMPTY;
    private int boxKeyCount = 0;

    public float itemRotX;
    public float itemRotY;
    private int gameTick = 0;

    public CsboxScreen(ItemStack stack) {
        super(Text.literal("cs_screen"));
        this.client = MinecraftClient.getInstance();

        if (this.client != null && this.client.player != null) {
            this.entity = this.client.player;
            this.world = entity.getWorld();
            this.itemMenu = stack;

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
        } else {
            this.entity = null;
            this.world = null;
            this.itemMenu = ItemStack.EMPTY;
            this.itemGroup = Map.of();
            this.itemsList = List.of();
            this.gradeList = List.of();
        }
    }

    @Override
    protected void init() {
        super.init();
        BlurHandler.enable(true);

        // 开箱按钮
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

        // 返回按钮
        this.addDrawableChild(new TexturedButtonWithText(
                this.width * 73 / 100, this.height * 94 / 100,
                this.width * 4 / 100, this.height * 5 / 100,
                0, 0, 64,
                new Identifier("withdrawals", "textures/screens/atlas/back_box.png"),
                82, 128,
                button -> closeScreen(),
                Text.translatable("gui.withdrawals.csgo_box.back_box"),
                this.textRenderer,
                0.8f
        ));
    }

    private void openBox() {
        if (this.client == null || this.entity == null) return;

        String keyId = ItemCsgoBox.getKey(itemMenu);
        boolean needsKey = keyId != null && !keyId.isEmpty();

        if (itemMenu.getItem() instanceof ItemCsgoBox && (!needsKey || hasKey(keyId))) {
            // 【关键修改】: 不再直接创建屏幕，而是发送一个数据包到服务器，请求打开GUI
            // 服务器收到这个包后，会打开一个ScreenHandler，然后客户端会自动打开对应的CsboxProgressScreen
            PacketByteBuf buf = PacketByteBufs.create();
            // 这里可以发送一些需要的信息，如果不需要额外信息，发送一个空包即可
            // 比如，我们可以发送一个整数来表示操作类型
            buf.writeInt(1); // 1 代表请求开箱
            ClientPlayNetworking.send(ModPackets.CSGO_PROGRESS_ID, buf);

            // 如果需要消耗钥匙，消耗钥匙的逻辑应该在服务器端处理，
            // 但我们可以在客户端先发送消耗钥匙的请求。
            // 您原来的代码已经有这个逻辑了，我们把它保留。
            if (needsKey) {
                PacketByteBuf keyBuf = PacketByteBufs.create();
                keyBuf.writeInt(2); // 2 代表消耗钥匙
                keyBuf.writeString(keyId);
                ClientPlayNetworking.send(ModPackets.CSGO_PROGRESS_ID, keyBuf);
            }
        }
    }

    private boolean hasKey(String keyId) {
        if (this.entity == null) return false;
        for (ItemStack stack : this.entity.getInventory().main) {
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
    }

    protected void renderBg(DrawContext context, float partialTicks, int gx, int gy) {
        RenderSystem.enableBlend();
        if (this.client != null) this.client.options.hudHidden = true;

        // 绘制分割线
        context.fill(this.width * 3 / 100, this.height * 53 / 100, this.width * 97 / 100, this.height * 53 / 100 + 1, 0xFFD3D3D3);
        context.fill(this.width * 25 / 100, this.height * 92 / 100, this.width * 75 / 100, this.height * 92 / 100 + 1, 0xFFD3D3D3);

        // 渲染箱子物品（支持旋转）
        float scale = (width * 26F / 100F) / 16F;
        GuiItemMove.renderItemInInventoryFollowsMouse(context, this.width * 50 / 100, this.height * 32 / 100, this.itemRotX, this.itemRotY, itemMenu, this.entity, scale);

        // 渲染物品列表
        int x = 0, y = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            int py = 55, px = i;
            if (i > 9) { py = 73; px = i - 10; }
            ItemStack itemStack1 = itemsList.get(i);
            int grade = gradeList.get(i);
            x = px; y = py;
            if (grade == 5) break;
            IconListTools.renderItemFrame(this.entity, context, itemStack1, this.width * 4 / 100 + px * this.width * 9 / 100, this.height * py / 100, this.width, this.height, grade);
        }
        if (!gradeList.isEmpty() && gradeList.get(gradeList.size() - 1) == 5) {
            IconListTools.renderItemFrame(this.entity, context, ItemStack.EMPTY, this.width * 4 / 100 + x * this.width * 9 / 100, this.height * y / 100, this.width, this.height, 5);
        }

        // 渲染钥匙图标
        if (!itemKey.isEmpty()) {
            IconListTools.renderGuiItem(this.entity, this.world, context, itemKey, this.width * 25F / 100, this.height * 93F / 100, 1);
        }

        RenderSystem.disableBlend();
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
        renderText(context, titleText, middleOf(titleText.getString(), 1.5F), this.height * 5.9F / 100F, 1.5F);

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
    public void tick() {
        super.tick();
        if (this.client == null || this.client.player == null) {
            closeScreen();
            return;
        }

        if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
            this.containerTick();
        } else {
            closeScreen();
        }
    }

    public void containerTick() {
        gameTick++;
        if (gameTick % 20 == 1) {
            this.boxKeyCount = getKeyCount();
        }
        if (gameTick > 100000) gameTick = 0;
    }

    private int getKeyCount() {
        String keyId = ItemCsgoBox.getKey(itemMenu);
        if (keyId == null || keyId.isEmpty() || this.entity == null) return 0;
        int count = 0;
        for (ItemStack stack : entity.getInventory().main) {
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { // ESC Key
            closeScreen();
            return true;
        }
        return false;
    }

    private void closeScreen() {
        if (this.client != null && this.client.player != null) {
            this.client.player.closeHandledScreen();
        }
        this.close();
    }

    @Override
    public void close() {
        BlurHandler.enable(false);
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