package net.bluemangoo.craftmineFixPlus.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TheGame;
import net.minecraft.server.level.DimensionGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.mines.WorldGenBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(DimensionGenerator.class)
public class DimensionGeneratorMixin {
    @WrapOperation(method = "generateDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/DimensionGenerator;save(Ljava/lang/Object;Lcom/mojang/serialization/Codec;Lnet/minecraft/resources/RegistryOps;Ljava/nio/file/Path;)Ljava/util/concurrent/CompletableFuture;", ordinal = 3))
    private static CompletableFuture<?> saveLevelStem(Object object, Codec<?> codec, RegistryOps<JsonElement> registryOps, Path path, Operation<CompletableFuture<?>> original, @Local Holder<DimensionType> holder, @Local(argsOnly = true) TheGame theGame, @Local ResourceLocation resourceLocation) {
        var fake = (DimensionGenerator.FakeLevelStem) object;
        var levelStem = new LevelStem(holder, Optional.empty(), fake.effects(), fake.mine(), fake.spawn());
        ResourceKey<LevelStem> resourceKey = ResourceKey.create(Registries.LEVEL_STEM, resourceLocation);
        var reg = (WritableRegistry<LevelStem>) theGame.registries().compositeAccess().lookupOrThrow(Registries.LEVEL_STEM);
        reg.register(resourceKey, levelStem, RegistrationInfo.BUILT_IN);

        return original.call(object, codec, registryOps, path);
    }


    @WrapOperation(method = "generateDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/DimensionGenerator;save(Ljava/lang/Object;Lcom/mojang/serialization/Codec;Lnet/minecraft/resources/RegistryOps;Ljava/nio/file/Path;)Ljava/util/concurrent/CompletableFuture;", ordinal = 2))
    private static CompletableFuture<?> saveBiome(Object object, Codec<?> codec, RegistryOps<JsonElement> registryOps, Path path, Operation<CompletableFuture<?>> original, @Local Holder<DimensionType> holder, @Local(argsOnly = true) TheGame theGame, @Local ResourceLocation resourceLocation, @Local WorldGenBuilder.ModifiedBiome modifiedBiome) {
        var biome = (Biome) object;
        ResourceKey<Biome> resourceKey = ResourceKey.create(Registries.BIOME, new ResourceLocation(resourceLocation.getNamespace(), modifiedBiome.modified().location().getPath()));
        var reg = (WritableRegistry<Biome>) theGame.registries().compositeAccess().lookupOrThrow(Registries.BIOME);
        var biomeHolder = reg.register(resourceKey, biome, RegistrationInfo.BUILT_IN);
        var originBiomeHolder = reg.get(modifiedBiome.original()).get();
        biomeHolder.bindTags(originBiomeHolder.tags().toList());
        return original.call(object, codec, registryOps, path);
    }

    @WrapOperation(method = "generateDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceKey;create(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;", ordinal = 0))
    private static ResourceKey<?> saveLevelType(ResourceKey<? extends Registry<?>> resourceKey, ResourceLocation resourceLocation, Operation<ResourceKey<?>> original, @Local(argsOnly = true) TheGame theGame, @Local DimensionType dimensionType) {
        var key = (ResourceKey<DimensionType>) original.call(resourceKey, resourceLocation);
        var reg = (WritableRegistry<DimensionType>) theGame.registries().compositeAccess().lookupOrThrow(Registries.DIMENSION_TYPE);
        reg.register(key, dimensionType, RegistrationInfo.BUILT_IN);
        return key;
    }
}
