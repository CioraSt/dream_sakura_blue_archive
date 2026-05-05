package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IGlowingItem {
    /**
     * 获取物品的发光颜色
     *
     * @return float数组，包含RGB三个分量，值范围0.0-1.0
     */
    float[] getGlowColor();

    /**
     * 获取发光强度
     *
     * @return 发光强度，默认为1.0
     */
    default float getGlowIntensity() {
        return 1f;
    }

    /**
     * 获取额外的纹理层
     *
     * @return 纹理层列表，每一层按渲染顺序排列
     */
    default List<TextureLayer> getTextureLayers() {
        return List.of(); // 默认无额外层
    }

    /**
     * 纹理层数据类
     */
    class TextureLayer {
        private final ResourceLocation texture;
        private final float red, green, blue, alpha;
        private final float offsetX, offsetY, offsetZ;
        private final boolean isEmissive; // 是否为发光层

        public TextureLayer(ResourceLocation texture, float red, float green, float blue, float alpha,
                            float offsetX, float offsetY, float offsetZ, boolean isEmissive) {
            this.texture = texture;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.isEmissive = isEmissive;
        }

        // 静态辅助方法
        public static TextureLayerBuilder builder(ResourceLocation texture) {
            return new TextureLayerBuilder(texture);
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public float getRed() {
            return red;
        }

        public float getGreen() {
            return green;
        }

        public float getBlue() {
            return blue;
        }

        public float getAlpha() {
            return alpha;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public float getOffsetZ() {
            return offsetZ;
        }

        public boolean isEmissive() {
            return isEmissive;
        }
    }

    /**
     * 纹理层建造器
     */
    class TextureLayerBuilder {
        private final ResourceLocation texture;
        private float red = 1.0f, green = 1.0f, blue = 1.0f, alpha = 1.0f;
        private float offsetX = 0.0f, offsetY = 0.0f, offsetZ = 0.0f;
        private boolean isEmissive = false;

        public TextureLayerBuilder(ResourceLocation texture) {
            this.texture = texture;
        }

        public TextureLayerBuilder color(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            return this;
        }

        public TextureLayerBuilder alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public TextureLayerBuilder offset(float x, float y, float z) {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            return this;
        }

        public TextureLayerBuilder emissive(boolean emissive) {
            this.isEmissive = emissive;
            return this;
        }

        public TextureLayer build() {
            return new TextureLayer(texture, red, green, blue, alpha, offsetX, offsetY, offsetZ, isEmissive);
        }
    }
}