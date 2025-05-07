package net.buildtheearth.terraminusminus.generator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraminusminus.substitutes.BlockPos;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import net.buildtheearth.terraminusminus.util.ImmutableCompactArray;

@RequiredArgsConstructor
public class EarthBiomeProvider {
    protected final LoadingCache<ChunkPos, CompletableFuture<ImmutableCompactArray<IBiome<?>>>> cache;

    public EarthBiomeProvider(@NonNull EarthGeneratorSettings settings) {
        this.cache = CacheBuilder.newBuilder()
                .weakValues()
                .build(new ChunkDataLoader(settings));
    }

    public EarthBiomeProvider(@NonNull GeneratorDatasets datasets, @NonNull IEarthBiomeFilter<?>[] filters){
        this.cache = CacheBuilder.newBuilder()
                .weakValues()
                .build(new ChunkDataLoader(datasets, filters));
    }

    /**
     * @deprecated this method is blocking, use {@link #getBiomesForChunkAsync(ChunkPos)}
     */
    @Deprecated
    public ImmutableCompactArray<IBiome<?>> getBiomesForChunk(ChunkPos pos) {
        return this.getBiomesForChunkAsync(pos).join();
    }

    /**
     * Gets the biomes in the given chunk.
     *
     * @param pos the position of the chunk
     * @return a {@link CompletableFuture} which will be completed with the biomes in the chunk
     */
    public CompletableFuture<ImmutableCompactArray<IBiome<?>>> getBiomesForChunkAsync(ChunkPos pos) {
        return this.cache.getUnchecked(pos);
    }

    /**
     * @deprecated this method is blocking, use {@link #getBiomesForChunkAsync(ChunkPos)}
     */
    @Deprecated
    public IBiome<?> getBiome(BlockPos pos) {
        return this.getBiomesForChunk(ChunkPos.atBlockPos(pos)).get((pos.x() & 0xF) * 16 + (pos.z() & 0xF));
    }

    /**
     * @deprecated this method is blocking, use {@link #getBiomesForChunkAsync(ChunkPos)}
     */
    @Deprecated
    public IBiome<?>[] getBiomesForGeneration(IBiome<?>[] arr, int x, int z, int width, int height) {
        if (arr == null || arr.length < width * height) {
            arr = new IBiome<?>[width * height];
        }

        //stupidly inefficient solution, but nobody will ever use this so i'm not about to optimize it
        for (int zz = 0; zz < height; zz++) {
            for (int xx = 0; xx < width; xx++) {
                int blockX = (x + xx) << 2;
                int blockZ = (z + zz) << 2;
                arr[zz * 16 + xx] = this.getBiomesForChunk(new ChunkPos(blockX >> 4, blockZ >> 4)).get((blockX & 0xF) * 16 + (blockZ & 0xF));
            }
        }
        return arr;
    }

    /**
     * @deprecated this method is blocking, use {@link #getBiomesForChunkAsync(ChunkPos)}
     */
    @Deprecated
    public IBiome<?>[] getBiomes(@Nullable IBiome<?>[] oldBiomeList, int x, int z, int width, int depth) {
        return this.getBiomes(oldBiomeList, x, z, width, depth, true);
    }

    /**
     * @deprecated this method is blocking, use {@link #getBiomesForChunkAsync(ChunkPos)}
     */
    @Deprecated
    public IBiome<?>[] getBiomes(@Nullable IBiome<?>[] arr, int x, int z, int width, int length, boolean cacheFlag) {
        if (arr == null || arr.length < width * length) {
            arr = new IBiome<?>[width * length];
        }

        if (((x | z) & 0xF) == 0 && width == 16 && length == 16) {
            ImmutableCompactArray<IBiome<?>> array = this.getBiomesForChunk(new ChunkPos(x >> 4, z >> 4));
            for (int zz = 0; zz < 16; zz++) {
                for (int xx = 0; xx < 16; xx++) { //reverse coordinate order
                    arr[zz * 16 + xx] = array.get(xx * 16 + zz);
                }
            }
        } else { //stupidly inefficient solution, but nobody will ever use this so i'm not about to optimize it
            for (int zz = 0; zz < length; zz++) {
                for (int xx = 0; xx < width; xx++) {
                    int blockX = x + xx;
                    int blockZ = z + zz;
                    arr[zz * 16 + xx] = this.getBiomesForChunk(new ChunkPos(blockX >> 4, blockZ >> 4)).get((blockX & 0xF) * 16 + (blockZ & 0xF));
                }
            }
        }

        return arr;
    }

    public boolean areBiomesViable(int x, int z, int radius, List<IBiome<?>> allowed) {
        return true;
    }

    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<IBiome<?>> biomes, Random random) {
        return null;
    }

    /**
     * {@link CacheLoader} implementation for {@link EarthBiomeProvider} which asynchronously aggregates information from multiple datasets and stores it
     * in a {@link ImmutableCompactArray} for use by the generator.
     *
     * @author DaPorkchop_
     */
    public static class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<ImmutableCompactArray<IBiome<?>>>> {
        protected final GeneratorDatasets datasets;
        protected final IEarthBiomeFilter<?>[] filters;

        public ChunkDataLoader(@NonNull EarthGeneratorSettings settings) {
            this.datasets = settings.datasets();
            this.filters = EarthGeneratorPipelines.biomeFilters(settings);
        }

        public ChunkDataLoader(@NonNull GeneratorDatasets datasets, @NonNull IEarthBiomeFilter<?>[] filters) {
            this.datasets = datasets;
            this.filters = filters;
        }

        @Override
        public CompletableFuture<ImmutableCompactArray<IBiome<?>>> load(@NonNull ChunkPos pos) {
            return IEarthAsyncPipelineStep.getFuture(pos, this.datasets, this.filters, ChunkBiomesBuilder::get);
        }
    }
}