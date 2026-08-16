package ru.datapeice.anomalia.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow @Final protected GoalSelector targetSelector;
    @Shadow @Final protected GoalSelector goalSelector;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private boolean anomalia$initializedMadness = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onMobTick(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        World world = mob.getWorld();
        if (world.isClient) return;
        ChunkPos chunkPos = new ChunkPos(mob.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.isEmpty()) return;

        if (anomalies.contains(AnomalyType.DISCO_CHAOS)) {
            mob.setYaw(mob.getYaw() + 25.0F);
            mob.bodyYaw = mob.getYaw();
            mob.headYaw = mob.getYaw();
            if (mob.isOnGround() && world.random.nextInt(12) == 0) {
                mob.setVelocity(mob.getVelocity().x, 0.38, mob.getVelocity().z);
            }
            if (mob instanceof SheepEntity sheep && world.getTime() % 5 == 0) {
                DyeColor[] colors = DyeColor.values();
                sheep.setColor(colors[world.random.nextInt(colors.length)]);
            }
            if (world.random.nextInt(10) == 0) {
                world.addParticle(ParticleTypes.NOTE, mob.getX(), mob.getY() + 0.9, mob.getZ(), world.random.nextDouble(), 0, 0);
            }
        }

        if (anomalies.contains(AnomalyType.FLYING_PIGS) && mob instanceof PigEntity) {
            mob.setVelocity(mob.getVelocity().x, 0.08, mob.getVelocity().z);
            mob.fallDistance = 0.0F;
        }

        if (!world.isClient && anomalies.contains(AnomalyType.MICRO_FAST)) {
            if (mob.age % 20 == 0) {
                if (mob instanceof PassiveEntity passive) {
                    passive.setBaby(true);
                } else if (mob instanceof ZombieEntity zombie) {
                    zombie.setBaby(true);
                }
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 8, false, false));
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, 3, false, false));
            }
        }

        if (!world.isClient && !anomalia$initializedMadness && anomalies.contains(AnomalyType.KILLER_ANIMALS)) {
            if (mob instanceof AnimalEntity && mob instanceof PathAwareEntity pathAware) {
                this.targetSelector.add(1, new ActiveTargetGoal<>(mob, PlayerEntity.class, true));
                this.goalSelector.add(1, new MeleeAttackGoal(pathAware, 1.25D, true) {
                    @Override
                    protected void attack(LivingEntity target, double squaredDistance) {
                        double maxDist = this.getSquaredMaxAttackDistance(target);
                        if (squaredDistance <= maxDist && this.isCooledDown()) {
                            this.resetCooldown();
                            this.mob.swingHand(Hand.MAIN_HAND);
                            target.damage(target.getDamageSources().mobAttack(this.mob), 3.0F);
                        }
                    }
                });
            }
        }

        if (!world.isClient && !anomalia$initializedMadness && anomalies.contains(AnomalyType.INVISO_MOBS)) {
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 999999, 0, false, false));
        }

        if (!world.isClient && !anomalia$initializedMadness && anomalies.contains(AnomalyType.ARMORED_MOBS)) {
            mob.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            mob.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            mob.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            mob.equipStack(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        }

        anomalia$initializedMadness = true;
    }
}

