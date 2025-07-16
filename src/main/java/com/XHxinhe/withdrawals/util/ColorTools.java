package com.XHxinhe.withdrawals.util;

/**
 * 颜色处理工具类
 */
public class ColorTools {

    /**
     * 根据 ARGB 分量合成一个整数颜色值。
     * @param a 透明度 (0-255)
     * @param r 红色 (0-255)
     * @param g 绿色 (0-255)
     * @param b 蓝色 (0-255)
     * @return 整数表示的 ARGB 颜色
     */
    public static int argbColor(int a, int r, int g, int b) {
        // 修正了原始代码中 g 和 b 的位置错误
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * 将给定的颜色变得更深（变暗）。
     * @param color 原始颜色值
     * @return 变暗后的颜色值
     */
    public static int deepColor(int color) {
        // 获取每个颜色分量
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // 将RGB分量的值乘以一个系数使其变暗
        red = (int) (red * 0.7);
        green = (int) (green * 0.7);
        blue = (int) (blue * 0.7);

        // 重新合成颜色值
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 根据物品稀有度等级返回对应的颜色。
     * @param grade 稀有度等级 (1-5)
     * @return 对应的 ARGB 颜色值
     */
    public static int colorItems(int grade) {
        int color = 0;
        switch (grade) {
            case 1: // 消费级/普通级
                color = 0xff4c70ff; // 蓝色
                break;
            case 2: // 工业级/罕见
                color = 0xff8d5eff; // 紫色
                break;
            case 3: // 军规级/稀有
                color = 0xffe54af2; // 粉色
                break;
            case 4: // 受限级/神话
                color = 0xfff86351; // 红色
                break;
            case 5: // 隐秘级/传说
                color = 0xffffdc1d; // 金色
                break;
        }
        return color;
    }
}