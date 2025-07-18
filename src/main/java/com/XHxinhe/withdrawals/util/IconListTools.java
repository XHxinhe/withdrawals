// 文件路径: src/main/java/com/XHxinhe/withdrawals/util/IconListTools.java

package com.XHxinhe.withdrawals.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * 用于在GUI中渲染带背景、边框和稀有度指示的物品图标列表的工具类。
 */
public class IconListTools {

    private static final Identifier GOLD_ITEM_TEXTURE = new Identifier("withdrawals", "textures/screens/gold_item.png");

    public static void renderRarity(DrawContext drawContext, int pX0, int pY0, int toX, int toY, int color) {
        drawContext.fillGradient(pX0, pY0, toX, toY, 0xFF696969, 0xFFD3D3D3);
        drawContext.fill(pX0, pY0, pX0 + 2, toY, color);
    }

    public static void renderItemFrame(
            LivingEntity entity,        // 实体（通常是玩家）
            DrawContext drawContext,    // 绘制上下文
            ItemStack itemStack,        // 要渲染的物品
            int pX,                     // 框的起始X坐标
            int pY,                     // 框的起始Y坐标
            int width,                  // 屏幕宽度
            int height,                 // 屏幕高度
            int grade                   // 物品品质等级
    ) {
        // 根据品质等级获取对应的颜色
        int color = ColorTools.colorItems(grade);

        // 计算框的尺寸
        int frameWidth = width * 8 / 100;    // 框宽度为屏幕宽度的8%
        int frameHeight = height * 11 / 100;  // 框高度为屏幕高度的11%

        // 计算物品的缩放比例（框宽度的60%除以16）
        float scale = frameWidth * 60.0F / 100.0F / 16.0F;

        // 计算框的结束坐标
        int toX = pX + frameWidth;  // 框的右边界
        int toY = pY + frameHeight; // 框的下边界

        // 计算物品在框内的位置（当前是距离边框15%的位置）
        int itemX = pX + (int)(frameWidth * 0.20);
        int itemY = pY + (int)(frameHeight * 0.12);

        // 金色品质(5级)的特殊渲染
        if (grade == 5) {
            // 绘制金色渐变背景
            drawContext.fillGradient(pX, pY, toX, toY,
                    0xFF533c00,  // 深金色
                    0xFFb69008   // 浅金色
            );

            // 绘制左侧品质色边框
            drawContext.fill(pX, pY, pX + 2, toY, color);

            // 绘制金色框架纹理
            drawContext.drawTexture(
                    GOLD_ITEM_TEXTURE,     // 金色框纹理
                    pX + 2,                // 左边界+2像素
                    pY + 2,                // 上边界+2像素
                    0, 0,                  // 纹理起始坐标
                    frameWidth - 4,        // 纹理宽度
                    frameHeight - 4,       // 纹理高度
                    frameWidth - 4,        // 目标宽度
                    frameHeight - 4        // 目标高度
            );
        }
        // 普通品质的渲染
        else {
            // 渲染品质框架
            renderRarity(drawContext, pX, pY, toX, toY, color);

            // 渲染物品
            renderGuiItem(
                    entity,              // 实体
                    entity.getWorld(),   // 世界
                    drawContext,         // 绘制上下文
                    itemStack,           // 物品
                    itemX,              // 物品X坐标
                    itemY,              // 物品Y坐标
                    scale               // 缩放比例
            );
        }
    }

    public static void renderItemProgress(
            LivingEntity entity,          // 实体(通常是玩家)
            DrawContext drawContext,      // 绘制上下文
            ItemStack itemStack,          // 要渲染的物品
            float pX,                     // 起始X坐标
            float pY,                     // 起始Y坐标
            float width,                  // 屏幕宽度
            float height,                 // 屏幕高度
            int grade                     // 物品品质等级
    ) {
        // 获取品质对应的颜色
        int color = ColorTools.colorItems(grade);

        // 计算框架尺寸
        float frameWidth = width * 18 / 100;   // 框的宽度是屏幕宽度的18%
        float frameHeight = height * 25 / 100;  // 框的高度是屏幕高度的25%

        // 计算物品缩放比例(框宽度的60%除以16)
        float scale = frameWidth * 60.0F / 100.0F / 16.0F;

        // 计算框的结束坐标
        float toX = pX + frameWidth;   // 框的右边界
        float toY = pY + frameHeight;  // 框的下边界

        // 计算物品在框内的位置(距离框15%的位置)
        float itemX = pX + frameWidth * 0.15f;
        float itemY = pY + frameHeight * 0.15f;

        // 金色品质(5级)的特殊渲染
        if (grade == 5) {
            // 绘制金色渐变背景
            drawContext.fillGradient((int)pX, (int)pY, (int)toX, (int)toY,
                    0xFF533c00,    // 深金色
                    0xFFb69008     // 浅金色
            );

            // 绘制金色框架纹理
            drawContext.drawTexture(
                    GOLD_ITEM_TEXTURE,         // 金色框纹理
                    (int)pX + 2,              // 左边界+2像素
                    (int)pY + 2,              // 上边界+2像素
                    0, 0,                      // 纹理起始坐标
                    (int)frameWidth - 4,       // 纹理宽度
                    (int)frameHeight - 4,      // 纹理高度
                    (int)frameWidth - 4,       // 目标宽度
                    (int)frameHeight - 4       // 目标高度
            );

            // 绘制底部品质条
            drawContext.fill((int)pX, (int)toY, (int)toX, (int)toY + 2, color);
        }
        // 普通品质的渲染
        else {
            // 绘制灰色渐变背景
            drawContext.fillGradient((int)pX, (int)pY, (int)toX, (int)toY,
                    0xFF696969,    // 深灰色
                    0xFFA9A9A9     // 浅灰色
            );

            // 绘制下部品质色渐变
            drawContext.fillGradient(
                    (int)pX,                           // 左边界
                    (int)(pY + frameHeight * 2 / 3),   // 从框高度的2/3处开始
                    (int)toX,                          // 右边界
                    (int)toY,                          // 下边界
                    ColorTools.argbColor(0, 128, 128, 128),  // 半透明灰色
                    ColorTools.deepColor(color)              // 加深的品质色
            );

            // 渲染物品
            renderGuiItem(entity, entity.getWorld(), drawContext, itemStack,
                    (int)itemX, (int)itemY, scale);

            // 绘制底部品质条
            drawContext.fill((int)pX, (int)toY, (int)toX, (int)toY + 2, color);
        }
    }

    /**
     * [已修正] 在GUI中渲染一个3D物品模型，并正确处理渲染上下文以避免崩溃。
     * 这个方法现在是渲染物品的唯一入口，简化了逻辑。
     */
    public static void renderGuiItem(LivingEntity entity, World world, DrawContext drawContext, ItemStack itemStack, float pX, float pY, float scale) {
        if (itemStack.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(itemStack, world, entity, 0);

        drawContext.getMatrices().push();
        // 将坐标原点移动到我们想要渲染的位置
        drawContext.getMatrices().translate(pX, pY, 100.0F);
        // 移动到中心点进行缩放
        drawContext.getMatrices().translate(8.0F * scale, 8.0F * scale, 0.0F);
        // Y轴反转以匹配GUI坐标系
        drawContext.getMatrices().scale(1.0F, -1.0F, 1.0F);
        // 应用最终缩放
        drawContext.getMatrices().scale(16.0F * scale, 16.0F * scale, 16.0F * scale);

        // --- [核心修复] ---
        // 1. 获取正确的、用于实体/物品的 VertexConsumerProvider
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // 2. 使用获取到的 'immediate' 实例进行渲染
        itemRenderer.renderItem(
                itemStack,
                ModelTransformationMode.GUI,
                false, // isLeftHanded
                drawContext.getMatrices(),
                immediate, // 使用独立的 VertexConsumerProvider
                LightmapTextureManager.MAX_LIGHT_COORDINATE, // light
                OverlayTexture.DEFAULT_UV, // overlay
                bakedModel
        );

        // 3. 立即提交（draw）这个独立的渲染批次，防止状态污染
        immediate.draw();
        // --- [修复结束] ---

        drawContext.getMatrices().pop();
    }
}