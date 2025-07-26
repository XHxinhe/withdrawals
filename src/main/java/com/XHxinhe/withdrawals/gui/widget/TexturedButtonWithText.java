package com.XHxinhe.withdrawals.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TexturedButtonWithText extends TexturedButtonWidget {
    private final Text text;
    private final TextRenderer textRenderer;
    private final float scale; // 添加缩放比例

    public TexturedButtonWithText(int x, int y, int width, int height,
                                  int u, int v, int hoveredVOffset,
                                  Identifier texture, int textureWidth, int textureHeight,
                                  PressAction pressAction, Text buttonText, TextRenderer textRenderer,
                                  float scale) { // 添加缩放参数
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction);
        this.text = buttonText;
        this.textRenderer = textRenderer;
        this.scale = scale;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);

        int textWidth = (int)(textRenderer.getWidth(text) * scale);
        float textX = this.getX() + (this.width - textWidth) / 2;
        float textY = this.getY() + (this.height - 8 * scale) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(textX, textY, 0);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(textRenderer, text, 0, 0, 0xFFFFFF, true);
        context.getMatrices().pop();
    }
}