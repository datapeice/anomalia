package ru.datapeice.anomalia.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.config.AnomaliaConfig;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    private static final Set<Long> SPAWNED_BOSS_CHUNKS = new HashSet<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;

        if (world.getTime() % 100 == 0) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
                long key = chunkPos.toLong();

                if (!SPAWNED_BOSS_CHUNKS.contains(key) && ChunkAnomalyManager.hasAnomaly(world, chunkPos, AnomalyType.BOSS_APOCALYPSE)) {
                    if (world.random.nextDouble() < AnomaliaConfig.get().bossSpawnChance) {
                        SPAWNED_BOSS_CHUNKS.add(key);
                        BlockPos spawnPos = player.getBlockPos().up(10);

                        int bossChoice = world.random.nextInt(3);
                        if (bossChoice == 0 && AnomaliaConfig.get().allowBossDragonInOverworld) {
                            EnderDragonEntity dragon = EntityType.ENDER_DRAGON.create(world);
                            if (dragon != null) {
                                dragon.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY() + 15, spawnPos.getZ(), 0, 0);
                                world.spawnEntity(dragon);
                                world.playSound(null, spawnPos, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 1.0F);
                            }
                        } else if (bossChoice == 1 && AnomaliaConfig.get().allowBossWitherInOverworld) {
                            WitherEntity wither = EntityType.WITHER.create(world);
                            if (wither != null) {
                                wither.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
                                world.spawnEntity(wither);
                                world.playSound(null, spawnPos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 3.0F, 1.0F);
                            }
                        } else if (AnomaliaConfig.get().allowBossWardenInOverworld) {
                            WardenEntity warden = EntityType.WARDEN.create(world);
                            if (warden != null) {
                                warden.refreshPositionAndAngles(spawnPos.getX(), player.getY(), spawnPos.getZ(), 0, 0);
                                world.spawnEntity(warden);
                                world.playSound(null, spawnPos, SoundEvents.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 3.0F, 1.0F);
                            }
                        }
                    }
                }

                if (ChunkAnomalyManager.hasAnomaly(world, chunkPos, AnomalyType.PHANTOM_SWARM)) {
                    PhantomEntity phantom = EntityType.PHANTOM.create(world);
                    if (phantom != null) {
                        phantom.refreshPositionAndAngles(player.getX() + world.random.nextInt(6) - 3, player.getY() + 12, player.getZ() + world.random.nextInt(6) - 3, 0, 0);
                        world.spawnEntity(phantom);
                        world.playSound(null, player.getX(), player.getY() + 12, player.getZ(), SoundEvents.ENTITY_PHANTOM_SWOOP, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    }
                }

                if (ChunkAnomalyManager.hasAnomaly(world, chunkPos, AnomalyType.ANGRY_BEES_SWARM)) {
                    BeeEntity bee = EntityType.BEE.create(world);
                    if (bee != null) {
                        bee.refreshPositionAndAngles(player.getX() + world.random.nextInt(6) - 3, player.getY() + 2, player.getZ() + world.random.nextInt(6) - 3, 0, 0);
                        bee.setAngerTime(9999);
                        bee.setTarget(player);
                        world.spawnEntity(bee);
                    }
                }

                if (ChunkAnomalyManager.hasAnomaly(world, chunkPos, AnomalyType.SLIME_APOCALYPSE)) {
                    SlimeEntity slime = EntityType.SLIME.create(world);
                    if (slime != null) {
                        slime.refreshPositionAndAngles(player.getX() + world.random.nextInt(8) - 4, player.getY(), player.getZ() + world.random.nextInt(8) - 4, 0, 0);
                        slime.setSize(world.random.nextInt(3) + 1, true);
                        world.spawnEntity(slime);
                    }
                }
            }
        }
    }
}

