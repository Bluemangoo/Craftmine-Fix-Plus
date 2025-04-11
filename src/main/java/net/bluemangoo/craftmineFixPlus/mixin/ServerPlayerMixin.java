package net.bluemangoo.craftmineFixPlus.mixin;

import com.mojang.authlib.GameProfile;
import net.bluemangoo.craftmineFixPlus.mixinInterface.ServerLevelMI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "onMineEntered", at = @At("HEAD"))
    void onMineEntered(CallbackInfo ci) {
        this.foodData.setSaturation(5.0F);
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"))
    void teleport(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        ((ServerLevelMI) teleportTransition.newLevel()).craftmine_Fix_Plus$setJoined();
    }

    @Inject(method = "setServerLevel", at = @At("HEAD"))
    void setServerLevel(ServerLevel serverLevel, CallbackInfo ci) {
        ((ServerLevelMI) serverLevel).craftmine_Fix_Plus$setJoined();
    }
}
