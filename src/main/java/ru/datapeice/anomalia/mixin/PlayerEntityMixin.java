package ru.datapeice.anomalia.mixin;

import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.List;
import java.util.Set;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow @Final private PlayerAbilities abilities;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private Vec3d lastPlayerPos = Vec3d.ZERO;
    private int standStillTicks = 0;
    private int hotHandTicks = 0;
    private int jumpCounter = 0;
    private int speedRampTicks = 0;

    private static final Enchantment[] CRAZY_ENCHANTS = new Enchantment[] {
            Enchantments.SHARPNESS,
            Enchantments.KNOCKBACK,
            Enchantments.FIRE_ASPECT,
            Enchantments.LOOTING,
            Enchantments.EFFICIENCY,
            Enchantments.FORTUNE,
            Enchantments.PROTECTION,
            Enchantments.THORNS
    };

    @Inject(method = "eatFood", at = @At("HEAD"), cancellable = true)
    private void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.STOMACH_FLIP) && !world.isClient) {
            boolean isToxicFood = stack.isOf(Items.ROTTEN_FLESH) 
                    || stack.isOf(Items.SPIDER_EYE) 
                    || stack.isOf(Items.POISONOUS_POTATO)
                    || stack.isOf(Items.PUFFERFISH);

            if (isToxicFood) {
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(20.0F);
                player.removeStatusEffect(StatusEffects.HUNGER);
                player.removeStatusEffect(StatusEffects.POISON);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 1));
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                int currentFood = player.getHungerManager().getFoodLevel();
                player.getHungerManager().setFoodLevel(Math.max(0, currentFood - 8));
                player.getHungerManager().setSaturationLevel(0.0F);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0));
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 0.8F);
            }
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            cir.setReturnValue(stack);
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.INSTA_MINE)) {
            cir.setReturnValue(99999.0F);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (world.isClient) return;
        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.CREATIVE_FLIGHT)) {
            if (!this.abilities.allowFlying) {
                this.abilities.allowFlying = true;
                player.sendAbilitiesUpdate();
            }
        } else if (!player.isCreative() && !player.isSpectator()) {
            if (this.abilities.allowFlying) {
                this.abilities.allowFlying = false;
                this.abilities.flying = false;
                player.sendAbilitiesUpdate();
            }
        }

        if (!world.isClient && !anomalies.contains(AnomalyType.ITEM_ERUPTION) && player.age % 10 == 0) {
            boolean dissolved = false;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.hasNbt() && stack.getNbt().getBoolean("AnomaliaVolcanic")) {
                    player.getInventory().setStack(i, ItemStack.EMPTY);
                    dissolved = true;
                }
            }
            if (dissolved) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.6F, 1.4F);
            }
        }

        if (anomalies.isEmpty()) {
            speedRampTicks = 0;
            return;
        }

        Vec3d vel = player.getVelocity();

        if (anomalies.contains(AnomalyType.SKYWARD_LIFT)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30, 1, false, false, false));
            player.fallDistance = 0.0F;
        }

        if (anomalies.contains(AnomalyType.HEAVY_GRAVITY)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 2, false, false, false));
            if (!player.isOnGround()) {
                player.setVelocity(vel.x * 0.7, vel.y - 0.35, vel.z * 0.7);
                player.velocityModified = true;
            }
        }

        if (anomalies.contains(AnomalyType.ZERO_GRAVITY)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 30, 0, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 2, false, false, false));
            player.fallDistance = 0.0F;
        }

        if (anomalies.contains(AnomalyType.SUPER_SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 4, false, false, false));
        }

        if (anomalies.contains(AnomalyType.SPEED_RAMP) && player.isSprinting()) {
            speedRampTicks = Math.min(speedRampTicks + 1, 80);
            int amp = Math.min(speedRampTicks / 15, 6);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, amp, false, false, false));
        } else if (!anomalies.contains(AnomalyType.SPEED_RAMP)) {
            speedRampTicks = 0;
        }

        if (anomalies.contains(AnomalyType.TURTLE_PACE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 4, false, false, false));
        }

        if (anomalies.contains(AnomalyType.HIGH_JUMP)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 4, false, false, false));
        }

        if (anomalies.contains(AnomalyType.ZERO_FRICTION) && player.isOnGround()) {
            if (!world.isClient) {
                BlockPos under = player.getBlockPos().down();
                BlockState underState = world.getBlockState(under);
                if (!underState.isAir() && !underState.isOf(Blocks.FROSTED_ICE) && !underState.isOf(Blocks.ICE) && !underState.isOf(Blocks.PACKED_ICE) && !underState.isOf(Blocks.BEDROCK) && !underState.isLiquid()) {
                    world.setBlockState(under, Blocks.FROSTED_ICE.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
            double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            if (hSpeed > 0.005) {
                player.setVelocity(vel.x * 1.07, vel.y, vel.z * 1.07);
                player.velocityModified = true;
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.MIDAS_GLASS) && player.isOnGround()) {
            BlockPos under = player.getBlockPos().down();
            BlockState underState = world.getBlockState(under);
            if (!underState.isAir() && !underState.isOf(Blocks.GLASS) && !underState.isOf(Blocks.BEDROCK) && !underState.isLiquid()) {
                world.setBlockState(under, Blocks.GLASS.getDefaultState(), Block.NOTIFY_ALL);
                world.playSound(null, under, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.6F, 1.6F);
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.ICE_AGE) && player.age % 10 == 0) {
            BlockPos pPos = player.getBlockPos();
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockPos target = pPos.add(dx, dy, dz);
                        if (world.getBlockState(target).isOf(Blocks.WATER)) {
                            world.setBlockState(target, Blocks.ICE.getDefaultState(), Block.NOTIFY_ALL);
                        }
                    }
                }
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.INSTANT_GROWTH) && player.age % 10 == 0 && world instanceof ServerWorld serverWorld) {
            BlockPos pPos = player.getBlockPos();
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockPos target = pPos.add(dx, dy, dz);
                        BlockState bState = world.getBlockState(target);
                        if (bState.getBlock() instanceof CropBlock crop && !crop.isMature(bState)) {
                            world.setBlockState(target, crop.withAge(crop.getMaxAge()), Block.NOTIFY_ALL);
                            world.playSound(null, target, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        } else if (bState.getBlock() instanceof SaplingBlock sapling) {
                            for (int k = 0; k < 5; k++) {
                                sapling.generate(serverWorld, target, bState, world.random);
                            }
                            world.playSound(null, target, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
            }
        }

        if (anomalies.contains(AnomalyType.HORIZONTAL_WIND)) {
            player.setVelocity(vel.x + 0.12, vel.y, vel.z);
            player.velocityModified = true;
        }

        if (!world.isClient && anomalies.contains(AnomalyType.EARTHQUAKE) && player.age % 80 == 0) {
            double boost = anomalies.contains(AnomalyType.SUPER_TRAMPOLINE) ? 1.5 : 0.55;
            player.setVelocity(vel.x, boost, vel.z);
            player.velocityModified = true;
            if (anomalies.contains(AnomalyType.SUPER_TRAMPOLINE)) {
                player.fallDistance = 0.0F;
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 0.5F, 0.7F);
        }

        if (!world.isClient && anomalies.contains(AnomalyType.MAGNETIC_CHEST)) {
            Box box = player.getBoundingBox().expand(16.0);
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, box, e -> true);
            for (ItemEntity item : items) {
                Vec3d pull = player.getPos().subtract(item.getPos()).normalize().multiply(0.3);
                item.setVelocity(pull);
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.REPULSION_AURA)) {
            Box box = player.getBoundingBox().expand(6.0);
            List<Entity> nearby = world.getOtherEntities(player, box);
            for (Entity e : nearby) {
                Vec3d push = e.getPos().subtract(player.getPos()).normalize().multiply(0.4);
                e.setVelocity(push.x, 0.2, push.z);
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.KEEP_MOVING)) {
            Vec3d currentPos = player.getPos();
            if (currentPos.squaredDistanceTo(lastPlayerPos) < 0.005) {
                standStillTicks++;
                if (standStillTicks == 30) {
                    player.setOnFireFor(4);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                } else if (standStillTicks > 30) {
                    player.setOnFireFor(4);
                }
            } else {
                standStillTicks = 0;
            }
            lastPlayerPos = currentPos;
        }

        if (!world.isClient && anomalies.contains(AnomalyType.FIRE_FEET) && player.isOnGround()) {
            if (world.getBlockState(player.getBlockPos()).isAir()) {
                world.setBlockState(player.getBlockPos(), Blocks.FIRE.getDefaultState(), Block.NOTIFY_ALL);
            }
        }

        if (anomalies.contains(AnomalyType.VORTEX_TORNADO)) {
            double centerX = (chunkPos.x << 4) + 8.0;
            double centerZ = (chunkPos.z << 4) + 8.0;
            double dx = centerX - player.getX();
            double dz = centerZ - player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.5) {
                double pull = 0.08;
                double tangent = 0.25;
                player.setVelocity(
                        (dx / dist) * pull - (dz / dist) * tangent,
                        Math.min(vel.y * 0.9 + 0.04, 0.45),
                        (dz / dist) * pull + (dx / dist) * tangent
                );
                player.velocityModified = true;
            }
            if (world.isClient && world.random.nextInt(2) == 0) {
                world.addParticle(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 0, 0.1, 0);
                world.addParticle(ParticleTypes.CLOUD, centerX + (world.random.nextDouble() - 0.5) * 4.0, player.getY() + world.random.nextDouble() * 3.0, centerZ + (world.random.nextDouble() - 0.5) * 4.0, 0, 0.2, 0);
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.POTION_ROULETTE) && player.age % 200 == 0) {
            int roll = world.random.nextInt(6);
            if (roll == 0) player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 100, 1));
            else if (roll == 1) player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
            else if (roll == 2) player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 2));
            else if (roll == 3) player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 2));
            else if (roll == 4) player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
            else player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 3));
        }

        if (!world.isClient && anomalies.contains(AnomalyType.CURSED_ENCHANTS) && player.age % 300 == 0) {
            ItemStack held = player.getMainHandStack();
            if (!held.isEmpty()) {
                Enchantment enc = CRAZY_ENCHANTS[world.random.nextInt(CRAZY_ENCHANTS.length)];
                held.addEnchantment(enc, 10);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.2F);
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.RADIO_NOISE) && world.random.nextInt(160) == 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.AMBIENT_CAVE.value(), SoundCategory.AMBIENT, 1.5F, (float) (0.5 + world.random.nextDouble()));
        }

        if (!world.isClient && anomalies.contains(AnomalyType.HOT_HANDS)) {
            ItemStack mainHand = player.getMainHandStack();
            if (!mainHand.isEmpty()) {
                hotHandTicks++;
                if (hotHandTicks > 20) {
                    player.setOnFireFor(3);
                    player.damage(player.getDamageSources().onFire(), 2.0F);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    hotHandTicks = 0;
                }
            } else {
                hotHandTicks = 0;
            }
        }

        if (!world.isClient && anomalies.contains(AnomalyType.GLASS_CANNON)) {
            if (player.getHealth() > 1.0F) {
                player.setHealth(1.0F);
            }
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 20, false, false));
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (world.isClient) return;
        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.HEAVY_GRAVITY)) {
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x * 0.3, 0.05, v.z * 0.3);
            player.velocityModified = true;
            ci.cancel();
            return;
        }

        if (anomalies.contains(AnomalyType.HIGH_JUMP)) {
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, 0.9, v.z);
            player.velocityModified = true;
            player.fallDistance = 0.0F;
            ci.cancel();
            return;
        }

        if (anomalies.contains(AnomalyType.SUPER_TRAMPOLINE)) {
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x * 1.5, 1.4, v.z * 1.5);
            player.velocityModified = true;
            player.fallDistance = 0.0F;
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            ci.cancel();
            return;
        }

        if (!world.isClient && anomalies.contains(AnomalyType.BLINK_STEP)) {
            Vec3d forward = player.getRotationVec(1.0F).multiply(5.0);
            player.teleport(player.getX() + forward.x, player.getY() + 0.5, player.getZ() + forward.z);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8F, 1.5F);
            ci.cancel();
            return;
        }

        if (!world.isClient && anomalies.contains(AnomalyType.ENDER_GLITCH)) {
            jumpCounter++;
            if (jumpCounter >= 3) {
                jumpCounter = 0;
                double targetX = (chunkPos.x << 4) + world.random.nextInt(16);
                double targetZ = (chunkPos.z << 4) + world.random.nextInt(16);
                double targetY = player.getY() + world.random.nextInt(5) - 2;
                player.teleport(targetX, targetY, targetZ);
                world.playSound(null, targetX, targetY, targetZ, 
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}

