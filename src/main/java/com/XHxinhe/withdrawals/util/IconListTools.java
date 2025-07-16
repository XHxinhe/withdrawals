package com.XHxinhe.withdrawals.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
// import net.minecraft.client.render.DiffuseLighting; // 已废弃，不再需要
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Matrix4f;

/**
 * 用于在GUI中渲染带背景、边框和稀有度指示的物品图标列表的工具类。
 */
public class IconListTools {

    private static final Identifier GOLD_ITEM_TEXTURE = new Identifier("withdrawals", "textures/screens/gold_item.png");

    // ... renderRarity, renderItemFrame, renderItemProgress 方法保持不变 ...
    public static void renderRarity(DrawContext drawContext, int pX0, int pY0, int toX, int toY, int color) {
        drawContext.fillGradient(pX0, pY0, toX, toY, 0xFF696969, 0xFFD3D3D3);
        drawContext.fill(pX0, pY0, pX0 + 2, toY, color);
    }

    public static void renderItemFrame(LivingEntity entity, DrawContext drawContext, ItemStack itemStack, int pX, int pY, int width, int height, int grade) {
        int color = ColorTools.colorItems(grade);
        int frameWidth = width * 8 / 100;
        int frameHeight = height * 11 / 100;
        float scale = frameWidth * 60.0F / 100.0F / 16.0F;
        int toX = pX + frameWidth;
        int toY = pY + frameHeight;
        int itemX = pX + (int)(frameWidth * 0.20);
        int itemY = pY + (int)(frameHeight * 0.10);

        if (grade == 5) {
            drawContext.fillGradient(pX, pY, toX, toY, 0xFF533c00, 0xFFb69008);
            drawContext.fill(pX, pY, pX + 2, toY, color);
            drawContext.drawTexture(GOLD_ITEM_TEXTURE, pX + 2, pY + 2, 0, 0, frameWidth - 4, frameHeight - 4, frameWidth - 4, frameHeight - 4);
        } else {
            renderRarity(drawContext, pX, pY, toX, toY, color);
            renderGuiItem(entity, entity.getWorld(), drawContext, itemStack, itemX, itemY, scale);
        }
    }

    public static void renderItemProgress(LivingEntity entity, DrawContext drawContext, ItemStack itemStack, float pX, float pY, float width, float height, int grade) {
        int color = ColorTools.colorItems(grade);
        float frameWidth = width * 18 / 100;
        float frameHeight = height * 25 / 100;
        float scale = frameWidth * 60.0F / 100.0F / 16.0F;
        float toX = pX + frameWidth;
        float toY = pY + frameHeight;
        float itemX = pX + frameWidth * 0.20f;
        float itemY = pY + frameHeight * 0.10f;

        if (grade == 5) {
            drawContext.fillGradient((int)pX, (int)pY, (int)toX, (int)toY, 0xFF533c00, 0xFFb69008);
            drawContext.drawTexture(GOLD_ITEM_TEXTURE, (int)pX + 2, (int)pY + 2, 0, 0, (int)frameWidth - 4, (int)frameHeight - 4, (int)frameWidth - 4, (int)frameHeight - 4);
            drawContext.fill((int)pX, (int)toY, (int)toX, (int)toY + 2, color);
        } else {
            drawContext.fillGradient((int)pX, (int)pY, (int)toX, (int)toY, 0xFF696969, 0xFFA9A9A9);
            drawContext.fillGradient((int)pX, (int)(pY + frameHeight * 2 / 3), (int)toX, (int)toY, ColorTools.argbColor(0, 128, 128, 128), ColorTools.deepColor(color));
            renderGuiItem(entity, entity.getWorld(), drawContext, itemStack, (int)itemX, (int)itemY, scale);
            drawContext.fill((int)pX, (int)toY, (int)toX, (int)toY + 2, color);
        }
    }

    /**
     * 在GUI中渲染一个3D物品模型。
     */
    public static void renderGuiItem(LivingEntity entity, World world, DrawContext drawContext, ItemStack itemStack, float pX, float pY, float scale) {
        BakedModel bakedModel = MinecraftClient.getInstance().getItemRenderer().getModel(itemStack, world, entity, 0);
        renderGuiItem(drawContext.getMatrices(), itemStack, pX, pY, bakedModel, scale);
    }

    protected static void renderGuiItem(MatrixStack matrixStack, ItemStack itemStack, float pX, float pY, BakedModel bakedModel, float scale) {
        matrixStack.push();
        matrixStack.translate(pX, pY, 100.0F);
        matrixStack.translate(8.0F * scale, 8.0F * scale, 0.0F);
        matrixStack.multiplyPositionMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
        matrixStack.scale(16.0F * scale, 16.0F * scale, 16.0F * scale);

        // DiffuseLighting 已被废弃，通常不再需要手动调用。
        // DiffuseLighting.enableGuiDepthLighting();

        // [FIX] getEntityVertexConsumerProvider() 已被重命名为 getEffectVertexConsumers()
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();

        // 渲染物品
        MinecraftClient.getInstance().getItemRenderer().renderItem(itemStack, ModelTransformationMode.GUI, false, matrixStack, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, bakedModel);

        // 提交缓冲区
        immediate.draw();
        // DiffuseLighting.disableGuiDepthLighting();

        matrixStack.pop();
    }
}