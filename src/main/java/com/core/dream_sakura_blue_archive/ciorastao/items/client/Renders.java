package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class Renders {
    /** Additive glow whose source contribution is attenuated by PNG alpha. */
    private static final RenderStateShard.TransparencyStateShard ALPHA_ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "halo_alpha_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
            );

    /** Alpha-tested depth pre-pass. It writes no color. */
    public static final Function<ResourceLocation, RenderType> HALO_DEPTH_MASK = Util.memoize(tex -> RenderType.create(
            dream_sakura_blue_archive.MODID + ":halo_depth_mask",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            false,
            RenderType.CompositeState.builder()
                    // Cutout shader discards transparent texels. Without this, the invisible
                    // part of a flat halo quad also writes depth and makes overlaps flicker.
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
    ));

    /** Original additive halo appearance, with depth writes delegated to the pre-pass. */
    public static final Function<ResourceLocation, RenderType> HALO_GLOW_COLOR = Util.memoize(tex -> RenderType.create(
            dream_sakura_blue_archive.MODID + ":halo_glow_color",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
    ));

    /** Catalog HALOs preserve multi-color RGB while their soft glow follows PNG alpha. */
    public static final Function<ResourceLocation, RenderType> CATALOG_HALO_GLOW_COLOR = Util.memoize(tex -> RenderType.create(
            dream_sakura_blue_archive.MODID + ":catalog_halo_glow_color",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(ALPHA_ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
    ));

    /** Existing combined color/depth path retained for modeled halos. */
    public static final Function<ResourceLocation, RenderType> MODELED_HALO_GLOW = Util.memoize(tex -> RenderType.create(
            dream_sakura_blue_archive.MODID + ":modeled_halo_glow",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    ));

    private Renders() {
    }
}
