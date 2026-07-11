package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class Renders {
    public static Function<ResourceLocation, RenderType> HALO_TYPT_NC = Util.memoize(tex -> RenderType.create(dream_sakura_blue_archive.MODID + ":halo_type_nc", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(tex, false, false)).setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).createCompositeState(false)));
    public static Function<ResourceLocation, RenderType> HALO_TYPT = Util.memoize(tex -> RenderType.create(dream_sakura_blue_archive.MODID + ":halo_type", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(tex, false, false)).setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.CULL).createCompositeState(false)));
}
