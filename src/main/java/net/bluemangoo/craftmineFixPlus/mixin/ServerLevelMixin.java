package net.bluemangoo.craftmineFixPlus.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.bluemangoo.craftmineFixPlus.mixinInterface.ServerLevelMI;
import net.bluemangoo.craftmineFixPlus.mixinInterface.TheGameMI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TheGame;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.MineData;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.MineTravellingBlockEntity;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerLevelMI {
    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @Shadow
    @Final
    List<ServerPlayer> players;

    @Shadow
    protected abstract void handleInProgressMine();

    @Shadow
    @Final
    protected MineData mineData;
    @Shadow
    @Final
    private TheGame theGame;

    @Shadow
    public abstract @NotNull List<ServerPlayer> players();

    @Shadow
    @NotNull
    public abstract TheGame theGame();

    @Unique
    private boolean joined = false;

    @Unique
    private Boolean ignorable = null;

    @Unique
    ResourceKey resourceKey;

    public boolean craftmine_Fix_Plus$isJoined() {
        return joined;
    }

    public void craftmine_Fix_Plus$setJoined() {
        this.joined = true;
    }

    public boolean craftmine_Fix_Plus$isIgnorable() {
        if (ignorable != null) {
            return ignorable;
        }
        ignorable = getChunkSource().chunkMap.getStorageName().startsWith("level");
        return ignorable;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(TheGame theGame, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey resourceKey, Holder holder, ChunkProgressListener chunkProgressListener, boolean bl, long l, List list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        this.resourceKey = resourceKey;
    }

    @Inject(method = "save", at = @At("RETURN"))
    void save(ProgressListener progressListener, boolean bl, boolean bl2, CallbackInfo ci) {
        if (craftmine_Fix_Plus$isIgnorable() && players.isEmpty()) {
            joined = false;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    void tick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        if (craftmine_Fix_Plus$isIgnorable() && !craftmine_Fix_Plus$isJoined()) {
            handleInProgressMine();
            ci.cancel();
        }
    }


    /**
     * @author Bluemangoo
     * @reason fix bug from swapInventoryFromHub execute time
     */
    @Overwrite
    public void leaveForMine(BlockPos blockPos, boolean bl, Optional<UUID> optional) {
        if (!bl) {
            this.mineData.resetMineTravvelingBlock();
        }

        if (this.getBlockEntity(blockPos) instanceof MineTravellingBlockEntity mineTravellingBlockEntity) {
            ResourceKey<Level> resourceKey = mineTravellingBlockEntity.getTargetDimension();
            ServerLevel serverLevel = this.theGame.getLevel(resourceKey);
            var thisLevel = this.resourceKey;

            if (serverLevel == null) {
                serverLevel = ((TheGameMI) theGame()).craftmine_Fix_Plus$tryInitLevel(resourceKey);
            }

            if (serverLevel == null) {
                this.theGame.server().sayGoodbye().thenAcceptAsync(minecraftServer -> {
                    ServerLevel serverLevelx = minecraftServer.theGame().getLevel(resourceKey);
                    if (serverLevelx != null) {
                        for (ServerPlayer serverPlayer : serverLevelx.theGame.getLevel(thisLevel).players()) {
                            if ((optional.isEmpty() || optional.get().equals(serverPlayer.getUUID())) && !serverPlayer.isSpectator()) {
                                serverPlayer.swapInventoryFromHub();
                            }
                        }
                        serverLevelx.teleportAllPlayersToMine(bl, optional);
                    }
                }, this.theGame.server());
            } else {
                for (ServerPlayer serverPlayer : this.players()) {
                    if ((optional.isEmpty() || optional.get().equals(serverPlayer.getUUID())) && !serverPlayer.isSpectator()) {
                        serverPlayer.swapInventoryFromHub();
                    }
                }
                serverLevel.teleportAllPlayersToMine(bl, optional);
            }
        }
    }

    @WrapOperation(method = "respawnPlayerIntoHub", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;setFoodLevel(I)V"))
    void respawnPlayerIntoHub(FoodData instance, int i, Operation<Void> original) {
        original.call(instance, i);
        instance.setSaturation(5.0F);
    }
}
