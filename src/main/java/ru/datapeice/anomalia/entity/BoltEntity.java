package ru.datapeice.anomalia.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.registry.ModEntities;
import ru.datapeice.anomalia.registry.ModItems;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

public class BoltEntity extends ThrownItemEntity {

    public BoltEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public BoltEntity(World world, LivingEntity owner) {
        super(ModEntities.BOLT_ENTITY_TYPE, owner, world);
    }

    public BoltEntity(World world, double x, double y, double z) {
        super(ModEntities.BOLT_ENTITY_TYPE, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BOLT_ITEM;
    }

    @Override
    public void tick() {
        super.tick();

        World world = this.getWorld();
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (!anomalies.isEmpty()) {
            Vec3d vel = this.getVelocity();

            if (anomalies.contains(AnomalyType.SKYWARD_LIFT)) {
                this.setVelocity(vel.x, vel.y + 0.08, vel.z);
                world.addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0, 0.1, 0);
            }

            if (anomalies.contains(AnomalyType.ZERO_GRAVITY)) {
                this.setVelocity(vel.x * 0.99, vel.y + 0.03, vel.z * 0.99);
                world.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }

            if (anomalies.contains(AnomalyType.HORIZONTAL_WIND)) {
                this.setVelocity(vel.x + 0.1, vel.y, vel.z);
                world.addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0.1, 0, 0);
            }

            if (anomalies.contains(AnomalyType.VORTEX_TORNADO)) {
                double centerX = (chunkPos.x << 4) + 8.0;
                double centerZ = (chunkPos.z << 4) + 8.0;
                double dx = centerX - this.getX();
                double dz = centerZ - this.getZ();
                this.setVelocity(-dz * 0.1, vel.y * 0.95 + 0.02, dx * 0.1);
                world.addParticle(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }

            if (anomalies.contains(AnomalyType.LIGHTNING_STRIKER) || anomalies.contains(AnomalyType.COMBUSTION_MOBS) || anomalies.contains(AnomalyType.EXPLODING_ORES)) {
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 0, 0.05, 0);
            }

            if (anomalies.contains(AnomalyType.ICE_AGE)) {
                world.addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0, -0.05, 0);
            }

            if (anomalies.contains(AnomalyType.LAVA_SPONGE) || anomalies.contains(AnomalyType.FIRE_FEET)) {
                world.addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0.05, 0);
            }

            if (anomalies.contains(AnomalyType.BOSS_APOCALYPSE) || anomalies.contains(AnomalyType.ENDER_GLITCH)) {
                world.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 
                        (Math.random() - 0.5) * 0.5, (Math.random() - 0.5) * 0.5, (Math.random() - 0.5) * 0.5);
            }
        }
    }

    private void reportAnomaliesToOwner(ChunkPos chunkPos) {
        World world = this.getWorld();
        if (!world.isClient && this.getOwner() instanceof ServerPlayerEntity player) {
            Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);
            if (anomalies.isEmpty()) {
                player.sendMessage(Text.translatable("bolt.report.clean"), false);
            } else {
                StringBuilder sb = new StringBuilder();
                int index = 0;
                for (AnomalyType anomaly : anomalies) {
                    if (index > 0) {
                        sb.append("§7, ");
                    }
                    sb.append("§e").append(anomaly.getName());
                    index++;
                }
                player.sendMessage(Text.translatable("bolt.report.found", sb.toString()), false);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        World world = this.getWorld();
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        reportAnomaliesToOwner(chunkPos);

        world.playSound(null, this.getX(), this.getY(), this.getZ(), 
                SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.3F, 2.0F);

        if (anomalies.contains(AnomalyType.SUPER_TRAMPOLINE)) {
            this.setVelocity(this.getVelocity().x * 1.5, 1.2, this.getVelocity().z * 1.5);
            world.playSound(null, this.getX(), this.getY(), this.getZ(), 
                    SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.PLAYERS, 0.8F, 1.2F);
            return;
        }

        if (!world.isClient) {
            this.dropStack(new ItemStack(ModItems.BOLT_ITEM));
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        World world = this.getWorld();
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());

        reportAnomaliesToOwner(chunkPos);

        world.playSound(null, this.getX(), this.getY(), this.getZ(), 
                SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.4F, 1.8F);
        if (!world.isClient) {
            this.dropStack(new ItemStack(ModItems.BOLT_ITEM));
            this.discard();
        }
    }
}
