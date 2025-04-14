package net.bluemangoo.craftmineFixPlus.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.bluemangoo.craftmineFixPlus.mixinInterface.ServerLevelMI;
import net.bluemangoo.craftmineFixPlus.mixinInterface.TheGameMI;
import net.bluemangoo.craftmineFixPlus.utils.RwLockLinkedHashMap;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.TheGame;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(TheGame.class)
public abstract class TheGameMixin implements TheGameMI {
    @Shadow
    @Final
    private WorldData worldData;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private LevelStorageSource.LevelStorageAccess storageSource;


    @Shadow
    @Final
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Shadow
    public abstract ServerLevel overworld();

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;
    @Unique
    ChunkProgressListenerFactory chunkProgressListenerFactory;

    public void craftmine_Fix_Plus$setChunkProgressListenerFactory(ChunkProgressListenerFactory chunkProgressListenerFactory) {
        this.chunkProgressListenerFactory = chunkProgressListenerFactory;
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "()Ljava/util/LinkedHashMap;"))
    LinkedHashMap<ResourceKey<Level>, ServerLevel> changeMapImpl(Operation<LinkedHashMap<ResourceKey<Level>, ServerLevel>> original) {
        return new RwLockLinkedHashMap<>();
    }

    @WrapOperation(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/TheGame;getAllLevels()Ljava/util/Collection;"))
    Collection<ServerLevel> saveAllChunks(TheGame instance, Operation<Collection<ServerLevel>> original) {
        return original.call(instance).stream().filter((serverLevel -> ((ServerLevelMI) serverLevel).craftmine_Fix_Plus$isJoined())).toList();
    }

    @Inject(method = "create", at = @At("RETURN"))
    private static void create(MinecraftServer minecraftServer, PackRepository packRepository, WorldStem worldStem, LevelStorageSource.LevelStorageAccess levelStorageAccess, ChunkProgressListenerFactory chunkProgressListenerFactory, Function<TheGame, PlayerList> function, CallbackInfoReturnable<TheGame> cir, @Local TheGame theGame) {
        ((TheGameMI) theGame).craftmine_Fix_Plus$setChunkProgressListenerFactory(chunkProgressListenerFactory);
    }

    public ServerLevel craftmine_Fix_Plus$tryInitLevel(ResourceKey<Level> resourceKey) {
        var levelStem = registries.compositeAccess().lookupOrThrow(Registries.LEVEL_STEM).entrySet().stream().filter(entry -> entry.getKey().location().equals(resourceKey.location())).findFirst().get().getValue();

        ChunkProgressListener chunkProgressListener = chunkProgressListenerFactory.create(worldData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));
        DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, this.worldData.overworldData());
        ServerLevel serverLevel = new ServerLevel(
                (TheGame) (Object) this,
                this.server.taskExecutor(),
                this.storageSource,
                derivedLevelData,
                resourceKey,
                (levelStem).type(),
                chunkProgressListener,
                this.worldData.isDebugWorld(),
                BiomeManager.obfuscateSeed(this.worldData.worldGenOptions().seed()),
                ImmutableList.of(),
                false,
                overworld().getRandomSequences()
        );
        var worldBorder = overworld().getWorldBorder();
        worldBorder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverLevel.getWorldBorder()));
        worldBorder.applySettings(this.worldData.overworldData().getWorldBorder());

        TicketStorage ticketStorage = serverLevel.getDataStorage().get(TicketStorage.TYPE);
        if (ticketStorage != null) {
            ticketStorage.activateAllDeactivatedTickets();
        }
        chunkProgressListener.stop();

        this.levels.put(resourceKey, serverLevel);
        return serverLevel;
    }
}
