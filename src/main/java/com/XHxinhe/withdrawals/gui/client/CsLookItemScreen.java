package com.XHxinhe.withdrawals.gui.client;

import com.XHxinhe.withdrawals.gui.widget.TexturedButtonWithText;
import com.mojang.blaze3d.systems.RenderSystem;
import com.XHxinhe.withdrawals.component.ModComponents;
import com.XHxinhe.withdrawals.sounds.ModSounds;
import com.XHxinhe.withdrawals.util.BlurHandler;
import com.XHxinhe.withdrawals.util.ColorTools;
import com.XHxinhe.withdrawals.util.GuiItemMove;
// 移除了对 RenderFontTool 的导入
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
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
        // [修正] 移除了不必要的 BlurHandler.updateShader(false) 调用
        if (this.client != null) {
            this.client.options.hudHidden = true;
        }

        if (openItem == null) {
            return;
        }

        float scale = (width * 26F / 100F) / 16F;

        context.fill(this.width * 25 / 100, this.height * 92 / 100, this.width * 75 / 100, this.height * 92 / 100 + 1, 0xFFD3D3D3);
        context.fill(this.width * 37 / 100, this.height * 16 / 100, this.width * 63 / 100, this.height * 16 / 100 + 2, ColorTools.colorItems(grade));

        GuiItemMove.renderItemInInventoryFollowsMouse(context, this.width * 50 / 100, this.height * 50 / 100, this.itemRotX, this.itemRotY, openItem, this.entity, scale);
        RenderSystem.disableBlend();
    }

    protected void renderLabels(DrawContext context, int mouseX, int mouseY) {
        if (openItem == null) {
            return;
        }

        Text component = openItem.getName().copy().fillStyle(Style.EMPTY.withBold(true));
        renderText(context, component, this.width * 45F / 100F, this.height * 5F / 100F, 1.8F);

        renderText(context, Text.translatable("gui.withdrawals.csgo_box.grade" + grade), this.width * 45F / 100F, this.height * 11F / 100F, 1F);
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.back_box").copy().fillStyle(Style.EMPTY.withBold(true)), (float) this.width * 72.5F / 100F, (float) this.height * 95 / 100, 0.8F);
    }

    /**
     * [修正] 使用 DrawContext 的矩阵变换来渲染缩放文本，移除了对 RenderFontTool 的依赖。
     */
    private void renderText(DrawContext context, Text text, float px, float py, float scale) {
        context.getMatrices().push();
        context.getMatrices().translate(px, py, 0);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(this.textRenderer, text, 0, 0, 0xFFFFFFFF, false);
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
        boolean isInRange = (pMouseX >= this.width * 37F / 100 && pMouseX <= this.width * 37F / 100 + 200) && (pMouseY >= this.height * 12F / 100 && pMouseY <= this.height * 12F / 100 + 176);
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
        if (keyCode == 256) { // ESC Key
            this.close();
            return true;
        }
        return false;
    }

    // [修正] 移除单独的 closeScreen 方法，统一使用 close()
    // private void closeScreen() { ... }

    @Override
    public void close() {
        // [修正] 调用正确的方法名，并且在关闭时禁用模糊
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