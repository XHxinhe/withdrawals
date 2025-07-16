package com.XHxinhe.withdrawals.gui.client;

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
import net.minecraft.client.gui.widget.TexturedButtonWidget;
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
        // 在GUI初始化时开启模糊效果
        BlurHandler.updateShaderState(true);

        // 开箱按钮
        this.addDrawableChild(new TexturedButtonWidget(
                this.width * 67 / 100, this.height * 94 / 100,
                this.width * 4 / 100, this.height * 5 / 100,
                0, 0, 64,
                new Identifier("withdrawals", "textures/screens/atlas/open_box.png"),
                82, 128,
                button -> openBox()
        ));

        // 返回按钮
        this.addDrawableChild(new TexturedButtonWidget(
                this.width * 72 / 100, this.height * 94 / 100,
                this.width * 4 / 100, this.height * 5 / 100,
                0, 0, 64,
                new Identifier("withdrawals", "textures/screens/atlas/back_box.png"),
                82, 128,
                button -> closeScreen()
        ));
    }

    private void openBox() {
        if (this.client == null || this.entity == null) return;

        String keyId = ItemCsgoBox.getKey(itemMenu);
        boolean needsKey = keyId != null && !keyId.isEmpty();

        if (itemMenu.getItem() instanceof ItemCsgoBox && (!needsKey || hasKey(keyId))) {
            this.client.setScreen(new CsboxProgressScreen());
            if (needsKey) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(2);
                buf.writeString(keyId);
                ClientPlayNetworking.send(ModPackets.CSGO_PROGRESS_ID, buf);
            }
        }
    }

    private boolean hasKey(String keyId) {
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
        GuiItemMove.renderItemInInventoryFollowsMouse(context, this.width * 37 / 100, this.height * 12 / 100, this.itemRotX, this.itemRotY, itemMenu, this.entity, scale);

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

        // 标题和标签
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.title").fillStyle(boldStyle), middleOf(Text.translatable("gui.withdrawals.csgo_box.title").getString(), 2), this.height * 5.9F / 100F, 2F);
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_box"), this.width * 46F / 100F, this.height * 13F / 100F, 0.8F);
        renderText(context, itemMenu.getName(), this.width * 50F / 100F, this.height * 13F / 100F, 0.8F);

        // 物品名称标签
        int x = 0, y = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            int py = 67, px = i;
            if (i > 9) { py = 85; px = i - 10; }
            int grade = gradeList.get(i);
            x = px; y = py;
            if (grade > 4) break;
            renderText(context, itemsList.get(i).getName(), this.width * 4F / 100 + px * this.width * 9F / 100, this.height * py / 100F, 0.6F);
        }
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_gold"), this.width * 4F / 100 + x * this.width * 9F / 100, this.height * y / 100F, 0.6F);
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_items").fillStyle(boldStyle), this.width * 3F / 100F, this.height * 50.3F / 100F, 0.8F);

        // 钥匙数量显示
        if (!itemKey.isEmpty()) {
            if (boxKeyCount > 0) {
                renderText(context, Text.literal(" × " + boxKeyCount), this.width * 28F / 100F, this.height * 94F / 100F, 0.8F);
            } else {
                renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_open"), this.width * 28F / 100F, this.height * 94F / 100F, 0.8F);
                renderText(context, itemKey.getName(), this.width * 35F / 100F, this.height * 94F / 100F, 0.8F);
                renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_open_1"), this.width * 40F / 100F, this.height * 94F / 100F, 0.8F);
            }
        }

        // 按钮文字
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.open_box").fillStyle(boldStyle), (float) this.width * 67.5F / 100F, (float) this.height * 95 / 100, 0.8F);
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.back_box").fillStyle(boldStyle), (float) this.width * 72.5F / 100F, (float) this.height * 95 / 100, 0.8F);
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
        if (keyId == null || keyId.isEmpty()) return 0;
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
        // 关闭时禁用模糊效果
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

    /**
     * 使用DrawContext的矩阵变换渲染缩放文本
     */
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