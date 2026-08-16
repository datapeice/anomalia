package ru.datapeice.anomalia.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.List;
import java.util.Set;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();

    protected ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private boolean anomalia$erupted = false;

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void onPlayerPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        World world = this.getWorld();
        if (world.isClient || anomalia$erupted || itemEntity.cannotPickup()) return;

        ChunkPos chunkPos = new ChunkPos(itemEntity.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (anomalies.contains(AnomalyType.ITEM_ERUPTION)) {
            anomalia$erupted = true;
            ItemStack stack = this.getStack();
            if (!stack.isEmpty()) {
                Box nearbyBox = player.getBoundingBox().expand(16.0);
                List<ItemEntity> nearbyItems = world.getEntitiesByClass(ItemEntity.class, nearbyBox, e -> true);
                if (nearbyItems.size() >= 32) {
                    return;
                }

                world.playSound(null, this.getX(), this.getY(), this.getZ(), 
                        SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 0.7F, 1.3F);

                int spawnCount = Math.min(5, 32 - nearbyItems.size());
                for (int i = 0; i < spawnCount; i++) {
                    ItemStack volcanicStack = stack.copyWithCount(1);
                    volcanicStack.getOrCreateNbt().putBoolean("AnomaliaVolcanic", true);

                    ItemEntity copy = new ItemEntity(world, this.getX(), this.getY() + 0.3, this.getZ(), volcanicStack);
                    double vx = (world.random.nextDouble() - 0.5) * 0.6;
                    double vy = 0.35 + world.random.nextDouble() * 0.3;
                    double vz = (world.random.nextDouble() - 0.5) * 0.6;
                    copy.setVelocity(vx, vy, vz);
                    copy.setPickupDelay(10);
                    world.spawnEntity(copy);
                }
            }
        }
    }
}

