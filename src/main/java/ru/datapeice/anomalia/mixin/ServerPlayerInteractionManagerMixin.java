package ru.datapeice.anomalia.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow @Final protected ServerPlayerEntity player;

    private static final BlockState[] RARE_ORES = new BlockState[] {
            Blocks.DIAMOND_ORE.getDefaultState(),
            Blocks.GOLD_ORE.getDefaultState(),
            Blocks.EMERALD_ORE.getDefaultState(),
            Blocks.ANCIENT_DEBRIS.getDefaultState(),
            Blocks.LAPIS_ORE.getDefaultState()
    };

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World world = this.player.getWorld();
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.isEmpty()) return;

        BlockState state = world.getBlockState(pos);

        if (anomalies.contains(AnomalyType.INSTA_MINE)) {
            if (state.isOf(Blocks.BEDROCK)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                ItemEntity bedrockDrop = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Blocks.BEDROCK));
                world.spawnEntity(bedrockDrop);
            } else {
                world.breakBlock(pos, true, player);
            }
            cir.setReturnValue(true);
            return;
        }

        if (anomalies.contains(AnomalyType.BLOCK_DUPLICATION)) {
            ItemStack drop = state.getBlock().asItem().getDefaultStack();
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(drop.getItem(), 3));
                world.spawnEntity(itemEntity);
            }
        }

        if (anomalies.contains(AnomalyType.ANVIL_RAIN)) {
            BlockPos anvilPos = pos.up(10);
            if (world.getBlockState(anvilPos).isAir()) {
                FallingBlockEntity.spawnFromBlock(world, anvilPos, Blocks.ANVIL.getDefaultState());
                world.playSound(null, anvilPos, SoundEvents.BLOCK_ANVIL_FALL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }

        if (anomalies.contains(AnomalyType.ORE_TRANSMUTATION) && (state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.DIRT)) && world.random.nextFloat() < 0.4F) {
            BlockState ore = RARE_ORES[world.random.nextInt(RARE_ORES.length)];
            world.setBlockState(pos, ore, Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.2F, 1.5F);
            cir.setReturnValue(false);
            return;
        }

        if (anomalies.contains(AnomalyType.EXPLODING_ORES)) {
            world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0F, World.ExplosionSourceType.BLOCK);
        }

        if (anomalies.contains(AnomalyType.SAND_COLLAPSE)) {
            BlockPos[] neighbors = new BlockPos[] { 
                    pos.north(), pos.south(), pos.east(), pos.west(), pos.up(),
                    pos.north().up(), pos.south().up(), pos.east().up(), pos.west().up()
            };
            for (BlockPos n : neighbors) {
                BlockState nState = world.getBlockState(n);
                if (!nState.isAir() && !nState.isLiquid() && !nState.isOf(Blocks.BEDROCK)) {
                    world.setBlockState(n, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    FallingBlockEntity.spawnFromBlock(world, n, nState);
                }
            }
            world.playSound(null, pos, SoundEvents.BLOCK_SAND_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
        }

        if (anomalies.contains(AnomalyType.PROLIFERATION)) {
            BlockPos[] neighbors = new BlockPos[] { pos.up(), pos.north(), pos.south(), pos.east(), pos.west() };
            for (BlockPos nPos : neighbors) {
                if (world.getBlockState(nPos).isAir()) {
                    world.setBlockState(nPos, Blocks.COBBLESTONE.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
            world.playSound(null, pos, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0F, 0.8F);
        }

        if (anomalies.contains(AnomalyType.CHICKEN_FOUNTAIN)) {
            for (int i = 0; i < 7; i++) {
                ChickenEntity chicken = EntityType.CHICKEN.create(world);
                if (chicken != null) {
                    chicken.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
                    chicken.setVelocity((world.random.nextDouble() - 0.5) * 0.5, 0.4, (world.random.nextDouble() - 0.5) * 0.5);
                    world.spawnEntity(chicken);
                }
            }
            world.playSound(null, pos, SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        if (anomalies.contains(AnomalyType.WATER_GEYSER)) {
            world.setBlockState(pos, Blocks.WATER.getDefaultState(), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        if (anomalies.contains(AnomalyType.SOUND_JUMPSCARE)) {
            if (world.random.nextBoolean()) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 2.0F, 1.0F);
            } else {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.PLAYERS, 2.0F, 0.8F);
            }
        }
    }
}

