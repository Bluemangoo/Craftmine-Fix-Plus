package net.bluemangoo.craftmineFixPlus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bluemangoo.craftmineFixPlus.mixinInterface.ServerLevelMI;
import net.minecraft.server.TheGame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(TheGame.class)
public class TheGameMixin {
    @WrapOperation(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/TheGame;getAllLevels()Ljava/util/Collection;"))
    Collection<ServerLevel> saveAllChunks(TheGame instance, Operation<Collection<ServerLevel>> original) {
        return original.call(instance).stream().filter((serverLevel -> ((ServerLevelMI) serverLevel).craftmine_Fix_Plus$isJoined())).toList();
    }
}
