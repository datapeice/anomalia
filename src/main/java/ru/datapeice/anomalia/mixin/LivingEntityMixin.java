package ru.datapeice.anomalia.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract void heal(float amount);
    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();


    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        World world = entity.getWorld();
        if (world.isClient) return;

        if (source.isOf(DamageTypes.THORNS) || source.isOf(DamageTypes.LIGHTNING_BOLT) || source.isOf(DamageTypes.GENERIC)) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.isEmpty()) return;

        if (anomalies.contains(AnomalyType.DAMAGE_INVERSION)) {
            this.heal(amount);
            if (source.getAttacker() instanceof LivingEntity attacker && attacker != entity) {
                attacker.damage(world.getDamageSources().generic(), amount);
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8F, 1.2F);
            cir.setReturnValue(false);
            return;
        }

        if (anomalies.contains(AnomalyType.VAMPIRISM) && source.getAttacker() instanceof LivingEntity attacker && attacker != entity) {
            attacker.heal(amount * 1.5F);
            world.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), 
                    SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.2F);
        }

        if (anomalies.contains(AnomalyType.THORNS_AURA) && source.getAttacker() instanceof LivingEntity attacker && attacker != entity && !source.isOf(DamageTypes.THORNS)) {
            attacker.damage(world.getDamageSources().thorns(entity), amount * 2.0F);
        }

        if (anomalies.contains(AnomalyType.RANDOM_DROP_ON_HIT) && entity instanceof ServerPlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            if (!stack.isEmpty()) {
                player.dropItem(stack.split(1), true, false);
            }
        }

        if (anomalies.contains(AnomalyType.BODY_SWAP) && source.getAttacker() instanceof ServerPlayerEntity player && player != entity) {
            Vec3d playerPos = player.getPos();
            Vec3d entityPos = entity.getPos();
            player.teleport(entityPos.x, entityPos.y, entityPos.z);
            entity.teleport(playerPos.x, playerPos.y, playerPos.z);
            world.playSound(null, playerPos.x, playerPos.y, playerPos.z, 
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        if (anomalies.contains(AnomalyType.LIGHTNING_STRIKER) && source.getAttacker() instanceof PlayerEntity && !source.isOf(DamageTypes.LIGHTNING_BOLT)) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                world.spawnEntity(lightning);
            }
        }

        if (anomalies.contains(AnomalyType.CELL_DIVISION) && !(entity instanceof PlayerEntity) && entity.getHealth() > 1.0F && !source.isOf(DamageTypes.LIGHTNING_BOLT)) {
            if (world.random.nextFloat() < 0.4F) {
                Entity clone = entity.getType().create(world);
                if (clone instanceof LivingEntity livingClone) {
                    livingClone.refreshPositionAndAngles(entity.getX() + 0.5, entity.getY(), entity.getZ() + 0.5, entity.getYaw(), entity.getPitch());
                    livingClone.setHealth(entity.getHealth() / 2.0F);
                    world.spawnEntity(livingClone);
                }
            }
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        World world = entity.getWorld();
        if (world.isClient) return;

        ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.COMBUSTION_MOBS)) {
            world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 2.0F, World.ExplosionSourceType.MOB);
        }

        if (anomalies.contains(AnomalyType.GOLDEN_TOUCH)) {
            ItemEntity gold = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.GOLD_INGOT, world.random.nextInt(3) + 1));
            world.spawnEntity(gold);
        }
    }
}

