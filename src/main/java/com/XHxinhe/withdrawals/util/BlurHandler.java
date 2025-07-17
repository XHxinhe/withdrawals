package com.XHxinhe.withdrawals.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class BlurHandler {

    private static final Identifier FADE_IN_BLUR_SHADER = new Identifier("withdrawals", "shaders/post/fade_in_blur.json");
    public static boolean isShaderOn = false;
    private static long startTime = -1;
    private static float prevProgress = -1;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isShaderOn && client.currentScreen == null) {
                updateShaderState(false);
            }
            if (isShaderOn && client.currentScreen != null) {
                float progress = getProgress();
                if (progress != prevProgress) {
                    prevProgress = progress;
                    updateUniform("Progress", progress);
                }
            }
        });
    }

    public static void updateShaderState(boolean enable) {
        if (isShaderOn == enable) return;
        MinecraftClient client = MinecraftClient.getInstance();
        GameRenderer gameRenderer = client.gameRenderer;

        if (enable) {
            try {
                // 使用反射加载着色器
                Object shaderEffect = Class.forName("net.minecraft.client.gl.ShaderEffect")
                        .getConstructor(
                                client.getTextureManager().getClass(),
                                client.getResourceManager().getClass(),
                                client.getFramebuffer().getClass(),
                                Identifier.class
                        )
                        .newInstance(
                                client.getTextureManager(),
                                client.getResourceManager(),
                                client.getFramebuffer(),
                                FADE_IN_BLUR_SHADER
                        );

                // 使用反射设置着色器
                Class.forName("net.minecraft.client.render.GameRenderer")
                        .getMethod("loadPostProcessor", Identifier.class)
                        .invoke(gameRenderer, FADE_IN_BLUR_SHADER);

                isShaderOn = true;
                startTime = System.currentTimeMillis();
                prevProgress = -1;
            } catch (Exception e) {
                System.err.println("Failed to load blur shader: " + FADE_IN_BLUR_SHADER);
                e.printStackTrace();
                isShaderOn = false;
            }
        } else {
            try {
                // 使用反射禁用着色器
                Class.forName("net.minecraft.client.render.GameRenderer")
                        .getMethod("disablePostProcessor")
                        .invoke(gameRenderer);

                isShaderOn = false;
                startTime = -1;
                prevProgress = -1;
            } catch (Exception e) {
                System.err.println("Failed to disable shader");
                e.printStackTrace();
            }
        }
    }

    private static void updateUniform(String name, float value) {
        RenderSystem.assertOnRenderThread();
        MinecraftClient client = MinecraftClient.getInstance();

        try {
            // 使用反射获取当前着色器
            Object shader = Class.forName("net.minecraft.client.render.GameRenderer")
                    .getMethod("getShader")
                    .invoke(client.gameRenderer);

            if (shader != null) {
                // 使用反射设置uniform值
                Object uniform = Class.forName("net.minecraft.client.gl.ShaderEffect")
                        .getMethod("getUniformOrDefault", String.class)
                        .invoke(shader, name);

                if (uniform != null) {
                    Class.forName("net.minecraft.client.gl.Uniform")
                            .getMethod("set", float.class)
                            .invoke(uniform, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to update shader uniform: " + name);
            e.printStackTrace();
        }
    }

    private static float getProgress() {
        if (startTime == -1) return 0;
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