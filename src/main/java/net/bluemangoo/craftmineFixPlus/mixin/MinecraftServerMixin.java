package net.bluemangoo.craftmineFixPlus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bluemangoo.craftmineFixPlus.mixinInterface.ServerLevelMI;
import net.bluemangoo.craftmineFixPlus.mixinInterface.TheGameMI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TheGame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;stopTheGame(Lnet/minecraft/server/TheGame;)V"))
    void stopServer(TheGame theGame, CallbackInfo ci) {
        theGame.getAllLevels().forEach(serverLevel -> ((ServerLevelMI) serverLevel).craftmine_Fix_Plus$isJoined());
    }

    @WrapOperation(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/TheGame;getAllLevels()Ljava/util/Collection;"))
    Collection<ServerLevel> tickChildren(TheGame instance, Operation<Collection<ServerLevel>> original) {
        var list = instance.getAllLevels();
        var nextLevel = ((TheGameMI) instance).craftmine_Fix_Plus$getNextLevel();
        if (nextLevel != null) {
            list.add(nextLevel);
        }
        return list;
    }
}
