package ru.datapeice.anomalia.mixin;

import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.world.ChunkAnomalyManager;

import java.util.Set;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {

    private boolean reversed = false;
    private boolean artilleryArmed = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onProjectileTick(CallbackInfo ci) {
        ProjectileEntity projectile = (ProjectileEntity) (Object) this;
        World world = projectile.getWorld();
        if (world.isClient) return;

        ChunkPos chunkPos = new ChunkPos(projectile.getBlockPos());
        Set<AnomalyType> anomalies = ChunkAnomalyManager.getAnomalies(world, chunkPos);

        if (!reversed && anomalies.contains(AnomalyType.BOOMERANG_PROJECTILES) && projectile.age > 3) {
            Vec3d v = projectile.getVelocity();
            projectile.setVelocity(-v.x * 1.3, -v.y, -v.z * 1.3);
            projectile.setOwner(null);
            reversed = true;
            world.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), 
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 0.8F, 1.8F);
        }

        if (!artilleryArmed && anomalies.contains(AnomalyType.HEAVY_ARTILLERY) && projectile.getOwner() instanceof SkeletonEntity) {
            artilleryArmed = true;
            projectile.setVelocity(projectile.getVelocity().multiply(1.5));
        }
    }

    @Inject(method = "onCollision", at = @At("HEAD"))
    private void onCollision(HitResult hitResult, CallbackInfo ci) {
        ProjectileEntity projectile = (ProjectileEntity) (Object) this;
        World world = projectile.getWorld();
        if (world.isClient || !artilleryArmed) return;

        world.createExplosion(projectile, projectile.getX(), projectile.getY(), projectile.getZ(), 2.0F, World.ExplosionSourceType.MOB);
    }
}

