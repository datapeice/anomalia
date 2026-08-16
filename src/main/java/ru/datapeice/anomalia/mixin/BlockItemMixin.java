package ru.datapeice.anomalia.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    private static final BlockState[] ROULETTE_BLOCKS = new BlockState[] {
            Blocks.CAKE.getDefaultState(),
            Blocks.BEE_NEST.getDefaultState(),
            Blocks.DIAMOND_BLOCK.getDefaultState(),
            Blocks.COBWEB.getDefaultState(),
            Blocks.CACTUS.getDefaultState(),
            Blocks.PISTON.getDefaultState(),
            Blocks.MAGMA_BLOCK.getDefaultState(),
            Blocks.SLIME_BLOCK.getDefaultState(),
            Blocks.TNT.getDefaultState(),
            Blocks.ICE.getDefaultState(),
            Blocks.OBSIDIAN.getDefaultState(),
            Blocks.SPONGE.getDefaultState(),
            Blocks.GOLD_BLOCK.getDefaultState(),
            Blocks.BEACON.getDefaultState(),
            Blocks.ANVIL.getDefaultState(),
            Blocks.PUMPKIN.getDefaultState()
    };

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        if (world.isClient) return;

        BlockPos pos = context.getBlockPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.isEmpty()) return;

        Block block = ((BlockItem) (Object) this).getBlock();

        if (anomalies.contains(AnomalyType.INSTANT_GROWTH)) {
            if (block instanceof CropBlock crop) {
                world.setBlockState(pos, crop.withAge(crop.getMaxAge()), Block.NOTIFY_ALL);
                world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                    context.getStack().decrement(1);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            } else if (block instanceof SaplingBlock sapling) {
                BlockState saplingState = sapling.getDefaultState().with(SaplingBlock.STAGE, 1);
                world.setBlockState(pos, saplingState, Block.NOTIFY_ALL);
                if (world instanceof ServerWorld serverWorld) {
                    sapling.generate(serverWorld, pos, saplingState, world.random);
                }
                world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                    context.getStack().decrement(1);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }

        if (anomalies.contains(AnomalyType.BLOCK_REJECTION)) {
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
            world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0.1, 0);
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        if (anomalies.contains(AnomalyType.BEDROCK_TRAP)) {
            world.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.5F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (anomalies.contains(AnomalyType.LAVA_SPONGE)) {
            world.setBlockState(pos, Blocks.LAVA.getDefaultState(), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (anomalies.contains(AnomalyType.ICE_AGE)) {
            world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState(), Block.NOTIFY_ALL);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        BlockPos n = pos.add(dx, dy, dz);
                        if (world.getBlockState(n).isOf(Blocks.WATER)) {
                            world.setBlockState(n, Blocks.ICE.getDefaultState(), Block.NOTIFY_ALL);
                        }
                    }
                }
            }
            world.playSound(null, pos, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.2F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (anomalies.contains(AnomalyType.TELE_PLACEMENT) && context.getPlayer() != null) {
            BlockPos altPos = context.getPlayer().getBlockPos().up(2);
            if (world.getBlockState(altPos).isAir()) {
                world.setBlockState(altPos, ((BlockItem) (Object) this).getBlock().getDefaultState(), Block.NOTIFY_ALL);
                world.playSound(null, altPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!context.getPlayer().getAbilities().creativeMode) {
                    context.getStack().decrement(1);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }

        if (anomalies.contains(AnomalyType.BLOCK_ROULETTE)) {
            BlockState randomState = ROULETTE_BLOCKS[world.random.nextInt(ROULETTE_BLOCKS.length)];
            world.setBlockState(pos, randomState, Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.5F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (anomalies.contains(AnomalyType.ROCKET_BLOCKS)) {
            FireworkRocketEntity rocket = new FireworkRocketEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.FIREWORK_ROCKET));
            world.spawnEntity(rocket);
            world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (anomalies.contains(AnomalyType.LIVING_ESCAPISTS)) {
            SilverfishEntity creature = EntityType.SILVERFISH.create(world);
            if (creature != null) {
                creature.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                world.spawnEntity(creature);
            }
            world.playSound(null, pos, SoundEvents.ENTITY_SILVERFISH_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.2F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (anomalies.contains(AnomalyType.GRAVITY_BLOCKS)) {
            BlockState state = ((BlockItem) (Object) this).getBlock().getDefaultState();
            FallingBlockEntity.spawnFromBlock(world, pos, state);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}

