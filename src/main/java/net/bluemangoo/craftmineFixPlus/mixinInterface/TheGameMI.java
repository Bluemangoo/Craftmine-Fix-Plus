package net.bluemangoo.craftmineFixPlus.mixinInterface;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.level.Level;

public interface TheGameMI {
    void craftmine_Fix_Plus$setChunkProgressListenerFactory(ChunkProgressListenerFactory chunkProgressListenerFactory);

    ServerLevel craftmine_Fix_Plus$tryInitLevel(ResourceKey<Level> resourceKey);
}
