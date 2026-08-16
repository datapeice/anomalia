package ru.datapeice.anomalia.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity {
    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void onExplode(CallbackInfo ci) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;
        World world = creeper.getWorld();
        if (world.isClient) return;

        ChunkPos chunkPos = new ChunkPos(creeper.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.CREEPER_CHAIN) && world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 2; i++) {
                CreeperEntity chainCreeper = EntityType.CREEPER.create(world);
                if (chainCreeper != null) {
                    double angle = world.random.nextDouble() * 2 * Math.PI;
                    double dist = 5.0 + world.random.nextDouble() * 3.5;
                    double spawnX = creeper.getX() + Math.cos(angle) * dist;
                    double spawnZ = creeper.getZ() + Math.sin(angle) * dist;
                    chainCreeper.refreshPositionAndAngles(spawnX, creeper.getY(), spawnZ, (float) Math.toDegrees(angle), 0);
                    world.spawnEntity(chainCreeper);
                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION, spawnX, creeper.getY() + 1, spawnZ, 3, 0.2, 0.2, 0.2, 0.1);
                    world.playSound(null, spawnX, creeper.getY(), spawnZ, SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1.0F, 1.2F);
                }
            }
        }
    }
}

