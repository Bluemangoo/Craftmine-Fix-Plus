package net.bluemangoo.craftmineFixPlus.mixin;

import net.minecraft.world.level.UnlockCondition;
import net.minecraft.world.level.mines.RandomizationMode;
import net.minecraft.world.level.mines.WorldEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldEffect.class)
public class WorldEffectMixin {
    @Shadow
    @Final
    private List<UnlockCondition> unlockedBy;

    @Mutable
    @Shadow
    @Final
    private RandomizationMode randomizationMode;

    @Inject(method = "requiredUnlockCount", at = @At("HEAD"))
    void requiresUnlockCount(CallbackInfoReturnable<Integer> cir) {
        if (unlockedBy.isEmpty()) {
            randomizationMode = RandomizationMode.WHEN_UNLOCKABLE;
        }
    }

}
