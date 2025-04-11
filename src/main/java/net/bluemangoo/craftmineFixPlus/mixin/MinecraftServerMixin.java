package net.bluemangoo.craftmineFixPlus.mixin;

import net.bluemangoo.craftmineFixPlus.mixinInterface.ServerLevelMI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TheGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;stopTheGame(Lnet/minecraft/server/TheGame;)V"))
    void stopServer(TheGame theGame, CallbackInfo ci) {
        theGame.getAllLevels().forEach(serverLevel -> ((ServerLevelMI) serverLevel).craftmine_Fix_Plus$isJoined());
    }
}
