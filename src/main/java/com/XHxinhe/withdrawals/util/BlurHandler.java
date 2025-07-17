package com.XHxinhe.withdrawals.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;

public class BlurHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("withdrawals");
    private static final Identifier FADE_IN_BLUR_SHADER = new Identifier("withdrawals", "shaders/post/fade_in_blur.json");
    public static boolean isShaderOn = false;
    private static long startTime = -1;
    private static float prevProgress = -1;

    // 用于缓存反射找到的方法，避免每次都查找，提高性能
    private static Method loadPostProcessorMethod;
    private static Method hasPostProcessorMethod;
    private static Method getUniformMethod;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isShaderOn && client.currentScreen == null) {
                enable(false);
            }
            if (isShaderOn) {
                float progress = getProgress();
                if (progress != prevProgress) {
                    prevProgress = progress;
                    updateUniform("Progress", progress);
                }
            }
        });
    }

    public static void enable(boolean enabled) {
        if (isShaderOn == enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        GameRenderer gameRenderer = client.gameRenderer;
        if (gameRenderer == null) return;

        try {
            if (enabled) {
                // 使用反射调用 loadPostProcessor
                if (loadPostProcessorMethod == null) {
                    loadPostProcessorMethod = GameRenderer.class.getDeclaredMethod("loadPostProcessor", Identifier.class);
                    loadPostProcessorMethod.setAccessible(true); // 强行设置为可访问
                }
                loadPostProcessorMethod.invoke(gameRenderer, FADE_IN_BLUR_SHADER);

                isShaderOn = true;
                startTime = System.currentTimeMillis();
                prevProgress = -1;
            } else {
                // 关闭着色器是公开方法，可以直接调用
                gameRenderer.disablePostProcessor();
                isShaderOn = false;
                startTime = -1;
                prevProgress = -1;
            }
        } catch (Exception e) {
            isShaderOn = false;
            LOGGER.warn("Failed to toggle blur shader using reflection: {}", FADE_IN_BLUR_SHADER, e);
            // 发生异常时，尝试安全地关闭
            try {
                if (hasPostProcessorMethod == null) {
                    hasPostProcessorMethod = GameRenderer.class.getDeclaredMethod("hasPostProcessor");
                    hasPostProcessorMethod.setAccessible(true);
                }
                boolean isActive = (boolean) hasPostProcessorMethod.invoke(gameRenderer);
                if (isActive) {
                    gameRenderer.disablePostProcessor();
                }
            } catch (Exception ex) {
                LOGGER.error("Failed to safely disable post processor.", ex);
            }
        }
    }

    private static void updateUniform(String name, float value) {
        RenderSystem.assertOnRenderThread();
        PostEffectProcessor postProcessor = MinecraftClient.getInstance().gameRenderer.getPostProcessor();

        if (postProcessor != null) {
            try {
                // 使用反射调用 getUniform
                if (getUniformMethod == null) {
                    getUniformMethod = PostEffectProcessor.class.getDeclaredMethod("getUniform", String.class);
                    getUniformMethod.setAccessible(true);
                }
                // 调用 getUniform 方法，获取 GlUniform 对象
                Object uniformObject = getUniformMethod.invoke(postProcessor, name);

                if (uniformObject instanceof GlUniform) {
                    ((GlUniform) uniformObject).set(value);
                }
            } catch (Exception e) {
                // 第一次更新失败后不再尝试，避免刷屏
                if (getUniformMethod != null) {
                    LOGGER.warn("Failed to update shader uniform '{}' using reflection.", name, e);
                    getUniformMethod = null; // 标记为失败，防止日志刷屏
                }
            }
        }
    }

    private static float getProgress() {
        if (startTime == -1) return 0.0f;
        return MathHelper.clamp((System.currentTimeMillis() - startTime) / 1000.0f, 0.0f, 1.0f);
    }

    public static int getBackgroundColor() {
        int a = 128, r = 90, g = 90, b = 90;
        float prog = getProgress();
        a = (int) (a * prog);
        r = (int) (r * prog);
        g = (int) (g * prog);
        b = (int) (b * prog);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}