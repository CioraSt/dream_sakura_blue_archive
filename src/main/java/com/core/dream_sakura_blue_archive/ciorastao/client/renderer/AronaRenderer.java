package com.core.dream_sakura_blue_archive.ciorastao.client.renderer;

import com.core.dream_sakura_blue_archive.ciorastao.client.model.AronaModel;
import com.core.dream_sakura_blue_archive.ciorastao.entity.AronaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AronaRenderer extends GeoEntityRenderer<AronaEntity> {
    public AronaRenderer(EntityRendererProvider.Context context) {
        super(context, new AronaModel());
        this.shadowRadius = 0.35F;
    }
}
