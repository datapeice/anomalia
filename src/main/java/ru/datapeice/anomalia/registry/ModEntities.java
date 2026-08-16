package ru.datapeice.anomalia.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.datapeice.anomalia.entity.BoltEntity;

public class ModEntities {
    public static final EntityType<BoltEntity> BOLT_ENTITY_TYPE = FabricEntityTypeBuilder.<BoltEntity>create(SpawnGroup.MISC, BoltEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build();

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, new Identifier("anomalia", "bolt"), BOLT_ENTITY_TYPE);
    }
}

