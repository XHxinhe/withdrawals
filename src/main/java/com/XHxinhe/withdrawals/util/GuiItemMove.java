package com.XHxinhe.withdrawals.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * 在GUI中渲染可移动/旋转物品的工具类
 */
public class GuiItemMove {

    /**
     * 在GUI中渲染一个根据指定角度旋转的物品。
     * @param drawContext     渲染上下文
     * @param pX              屏幕X坐标
     * @param pY              屏幕Y坐标
     * @param angleXComponent Y轴旋转分量 (绕Y轴)
     * @param angleYComponent X轴旋转分量 (绕X轴)
     * @param item            要渲染的物品堆栈
     * @param player          玩家实体（用于获取世界和模型上下文）
     * @param scale           缩放大小
     */
    public static void renderItemInInventoryFollowsAngle(DrawContext drawContext, int pX, int pY,
                                                         float angleXComponent, float angleYComponent, ItemStack item, LivingEntity player, float scale) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (item.isEmpty() || player == null) {
            return;
        }
        BakedModel bakedModel = client.getItemRenderer().getModel(item, player.getWorld(), player, 0);

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();

        // 初始定位和缩放
        matrixStack.translate(pX, pY, 100.0F + 50.0F); // 增加Z值防止被其他GUI元素遮挡
        matrixStack.translate(8.0F, 8.0F, 0.0F); // 移动到中心点
        matrixStack.scale(1.0F, -1.0F, 1.0F); // 反转Y轴以匹配GUI坐标
        matrixStack.scale(16.0F * scale, 16.0F * scale, 16.0F * scale); // 应用缩放

        // 应用旋转
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(angleXComponent));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(angleYComponent));

        int light = 15728880;
        // 设置光照
        DiffuseLighting.enableGuiDepthLighting();

        // 获取渲染器和缓冲区
        VertexConsumerProvider.Immediate bufferSource = client.getBufferBuilders().getEffectVertexConsumers();

        // 渲染物品
        client.getItemRenderer().renderItem(item, ModelTransformationMode.GUI, false,
                matrixStack, bufferSource, light,
                OverlayTexture.DEFAULT_UV, bakedModel);

        // 结束渲染
        bufferSource.draw();
        DiffuseLighting.disableGuiDepthLighting();

        matrixStack.pop();
    }

    /**
     * 在GUI中渲染跟随鼠标的物品
     */
    public static void renderItemInInventoryFollowsMouse(DrawContext drawContext, int pX, int pY,
                                                         float angleX, float angleY, ItemStack item, LivingEntity player, float scale) {
        renderItemInInventoryFollowsAngle(drawContext, pX, pY, angleX, angleY, item, player, scale);
    }

    /**
     * 计算X轴旋转角度
     * @param deltaX 鼠标X轴移动距离
     * @param currentRotX 当前X轴旋转角度
     * @return 新的X轴旋转角度
     */
    public static float renderRotAngleX(double deltaX, float currentRotX) {
        return (float) ((currentRotX + deltaX * 0.01) % (Math.PI * 2));
    }

    /**
     * 计算Y轴旋转角度
     * @param deltaY 鼠标Y轴移动距离
     * @param currentRotY 当前Y轴旋转角度
     * @return 新的Y轴旋转角度
     */
    public static float renderRotAngleY(double deltaY, float currentRotY) {
        float newRotY = (float) (currentRotY + deltaY * 0.01);
        // 限制Y轴旋转范围在 -PI/2 到 PI/2 之间
        return Math.max(-1.5f, Math.min(1.5f, newRotY));
    }

    /**
     * 重置旋转角度
     * @param rotX X轴旋转角度
     * @param rotY Y轴旋转角度
     * @return 重置后的角度数组 [rotX, rotY]
     */
    public static float[] resetRotation(float rotX, float rotY) {
        return new float[]{0.0f, 0.0f};
    }

    /**
     * 设置渲染状态
     */
    private static void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    /**
     * 恢复渲染状态
     */
    private static void restoreRenderState() {
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}