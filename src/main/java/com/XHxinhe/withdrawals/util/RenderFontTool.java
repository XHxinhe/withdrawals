package com.XHxinhe.withdrawals.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * 提供高级字体渲染功能的工具类，例如缩放渲染。
 */
public class RenderFontTool {

    /**
     * 在GUI中以指定的缩放比例绘制文本。
     * @param drawContext 渲染上下文
     * @param textRenderer 字体渲染器
     * @param text 要绘制的文本
     * @param pX 目标X坐标
     * @param pY 目标Y坐标
     * @param scale 缩放比例
     * @param pColor 颜色
     * @param shadow 是否带阴影
     */
    public static void drawString(DrawContext drawContext, TextRenderer textRenderer, Text text, float pX, float pY, float scale, int pColor, boolean shadow) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        // 矩阵变换：平移到目标位置 -> 缩放
        matrices.translate(pX, pY, 50); // 使用一个小的Z值确保在顶层
        matrices.scale(scale, scale, 1.0F);

        // 使用变换后的矩阵进行绘制
        if (shadow) {
            drawContext.drawTextWithShadow(textRenderer, text, 0, 0, pColor);
        } else {
            drawContext.drawText(textRenderer, text, 0, 0, pColor, false);
        }

        matrices.pop();
    }

    /**
     * 在GUI中以指定的缩放比例绘制格式化文本序列。
     * @param drawContext 渲染上下文
     * @param textRenderer 字体渲染器
     * @param text 要绘制的格式化文本
     * @param pX 目标X坐标
     * @param pY 目标Y坐标
     * @param scale 缩放比例
     * @param pColor 颜色
     * @param shadow 是否带阴影
     */
    public static void drawString(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, float pX, float pY, float scale, int pColor, boolean shadow) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        matrices.translate(pX, pY, 50);
        matrices.scale(scale, scale, 1.0F);

        if (shadow) {
            drawContext.drawTextWithShadow(textRenderer, text, 0, 0, pColor);
        } else {
            // DrawContext没有直接绘制OrderedText且不带阴影的方法，我们用底层方法
            textRenderer.draw(text, 0, 0, pColor, false, matrices.peek().getPositionMatrix(), drawContext.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            drawContext.draw(); // 提交缓冲区
        }

        matrices.pop();
    }
}