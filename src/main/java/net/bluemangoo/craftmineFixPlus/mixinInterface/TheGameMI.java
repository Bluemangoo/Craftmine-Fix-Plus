package net.bluemangoo.craftmineFixPlus.mixinInterface;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.level.Level;

import java.util.concurrent.CompletableFuture;

public interface TheGameMI {
    void craftmine_Fix_Plus$setChunkProgressListenerFactory(ChunkProgressListenerFactory chunkProgressListenerFactory);

    void craftmine_Fix_Plus$tryInitLevel(ResourceKey<Level> resourceKey);

    CompletableFuture<MinecraftServer> craftmine_Fix_Plus$tryReconfigureClients();
}
