package ru.datapeice.anomalia;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import ru.datapeice.anomalia.network.AnomalyNetworking;
import ru.datapeice.anomalia.registry.ModEntities;

public class AnomaliaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AnomalyNetworking.registerClient();

        EntityRendererRegistry.register(ModEntities.BOLT_ENTITY_TYPE, FlyingItemEntityRenderer::new);
    }
}

