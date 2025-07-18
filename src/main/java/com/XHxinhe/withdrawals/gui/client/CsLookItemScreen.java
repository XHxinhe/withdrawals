// 文件路径: src/main/java/com/XHxinhe/withdrawals/gui/client/CsLookItemScreen.java

package com.XHxinhe.withdrawals.gui.client;

import com.XHxinhe.withdrawals.component.ModComponents;
import com.XHxinhe.withdrawals.gui.widget.TexturedButtonWithText;
import com.XHxinhe.withdrawals.sounds.ModSounds;
import com.XHxinhe.withdrawals.util.BlurHandler;
import com.XHxinhe.withdrawals.util.ColorTools;
import com.XHxinhe.withdrawals.util.GuiItemMove;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class CsLookItemScreen extends Screen {

    private final World world;
    private final PlayerEntity entity;

    public ItemStack openItem;
    public int grade;
    private boolean openSwitch = true;

    public float itemRotX;
    public float itemRotY;


    public CsLookItemScreen() {
        super(Text.literal("look_item"));
        this.client = MinecraftClient.getInstance();

        if (this.client != null && this.client.player != null) {
            this.world = this.client.player.getWorld();
            this.entity = this.client.player;
        } else {
            this.world = null;
            this.entity = null;
        }
    }

    @Override
    protected void init() {
        super.init();
        BlurHandler.updateShaderState(true);

        // 使用TexturedButtonWithText来添加带文字的返回按钮
        this.addDrawableChild(new TexturedButtonWithText(
                this.width * 72 / 100,      // X位置
                this.height * 94 / 100,     // Y位置
                this.width * 4 / 100,       // 宽度
                this.height * 5 / 100,      // 高度
                0, 0, 64,
                new Identifier("withdrawals", "textures/screens/atlas/back_box.png"),
                82, 128,
                button -> this.close(),
                Text.translatable("gui.withdrawals.csgo_box.back_box"), // 按钮文本
                this.textRenderer,
                0.8f  // 文字缩放
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world != null) {
            // 绘制带模糊效果的背景
            context.fillGradient(0, 0, this.width, this.height, BlurHandler.getBackgroundColor(), BlurHandler.getBackgroundColor());
        } else {
            this.renderBackground(context);
        }

        renderBg(context, delta, mouseX, mouseY);
        renderLabels(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    protected void renderBg(DrawContext context, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        if (this.client != null) {
            this.client.options.hudHidden = true;
        }

        if (openItem == null) {
            return;
        }

        float scale = (width * 26F / 100F) / 16F;

        // 绘制UI装饰线条
        context.fill(this.width * 25 / 100, this.height * 92 / 100, this.width * 75 / 100, this.height * 92 / 100 + 1, 0xFFD3D3D3);
        context.fill(this.width * 37 / 100, this.height * 16 / 100, this.width * 63 / 100, this.height * 16 / 100 + 2, ColorTools.colorItems(grade));

        // 渲染可拖动的3D物品
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int itemX = centerX - 8;  // 减去8(16/2)使物品居中
        int itemY = centerY - 8;
        GuiItemMove.renderItemInInventoryFollowsMouse(context, itemX, itemY, this.itemRotX, this.itemRotY, openItem, this.entity, scale);
        RenderSystem.disableBlend();
    }

    protected void renderLabels(DrawContext context, int mouseX, int mouseY) {
        if (openItem == null) {
            return;
        }

        // 渲染居中的、带缩放的标题和等级文本
        Text titleText = openItem.getName().copy().fillStyle(Style.EMPTY.withBold(true));
        renderCenteredText(context, titleText, this.width / 2.0f, this.height * 4F / 100F, 1.8F);

        Text gradeText = Text.translatable("gui.withdrawals.csgo_box.grade" + grade);
        renderCenteredText(context, gradeText, this.width / 2.0f, this.height * 11F / 100F, 1.0F);
    }

    private void renderCenteredText(DrawContext context, Text text, float centerX, float y, float scale) {
        if (scale == 1.0F) {
            context.drawTextWithShadow(this.textRenderer, text, (int) (centerX - this.textRenderer.getWidth(text) / 2.0f), (int) y, 0xFFFFFFFF);
            return;
        }

        context.getMatrices().push();
        context.getMatrices().translate(centerX, y, 0);
        context.getMatrices().scale(scale, scale, 1.0F);
        float scaledX = -this.textRenderer.getWidth(text) / 2.0f;
        context.drawText(this.textRenderer, text, (int) scaledX, 0, 0xFFFFFFFF, true);
        context.getMatrices().pop();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.client == null || this.client.player == null) {
            this.close();
            return;
        }

        if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
            this.containerTick();
        } else {
            this.close();
        }
    }

    public void containerTick() {
        if (!openSwitch) {
            return;
        }
        if (this.entity == null) return;

        ModComponents.CSBOX_COMPONENT.maybeGet(this.entity).ifPresent(csbox -> {
            entity.playSound(ModSounds.CS_FINISH, 10F, 1F);
            ItemStack itemStack = csbox.getItem();
            if (!itemStack.isEmpty()) {
                openItem = itemStack;
                grade = csbox.getGrade();
            }
            openSwitch = false;
        });
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        boolean isInRange = (pMouseX >= this.width * 30F / 100 && pMouseX <= this.width * 70F / 100) &&
                (pMouseY >= this.height * 20F / 100 && pMouseY <= this.height * 80F / 100);

        if (pButton == 0 && isInRange) {
            this.itemRotX = GuiItemMove.renderRotAngleX(pDragX, this.itemRotX);
            this.itemRotY = GuiItemMove.renderRotAngleY(pDragY, this.itemRotY);
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        // [已修正] 直接检查ESC键的keyCode
        if (keyCode == 256) {
            this.close();
            return true;
        }

        return false;
    }

    @Override
    public void close() {
        BlurHandler.updateShaderState(false);
        if (this.client != null) {
            this.client.options.hudHidden = false;
            if (this.client.player != null) {
                this.client.player.closeHandledScreen();
            }
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}