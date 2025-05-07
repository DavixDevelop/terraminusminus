package net.buildtheearth.terraminusminus.generator;

import java.util.Arrays;

import lombok.Getter;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import net.buildtheearth.terraminusminus.util.ImmutableCompactArray;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

/**
 * Builds a 16x16 area of chunks.
 *
 * @author DaPorkchop_
 */
@Getter
public class ChunkBiomesBuilder implements IEarthAsyncDataBuilder<ImmutableCompactArray<IBiome<?>>> {
    private static final Cached<ChunkBiomesBuilder> BUILDER_CACHE = Cached.threadLocal(ChunkBiomesBuilder::new, ReferenceStrength.SOFT);

    public static ChunkBiomesBuilder get() {
        return BUILDER_CACHE.get().reset();
    }

    protected final IBiome<?>[] state = new IBiome[16 * 16];

    public IBiome<?> get(int x, int z) {
        return this.state[x * 16 + z];
    }

    public ChunkBiomesBuilder set(int x, int z, IBiome<?> biome) {
        this.state[x * 16 + z] = biome;
        return this;
    }

    /**
     * Resets this builder instance so that it may be used again.
     */
    public ChunkBiomesBuilder reset() {
        Arrays.fill(this.state, null);
        return this;
    }

    /**
     * @return the array of biomes in this chunk
     */
    @Override
    public ImmutableCompactArray<IBiome<?>> build() {
        for (int i = 0; i < 16 * 16; i++) {
            if (this.state[i] == null) {
                throw new IllegalStateException("all biomes must be set!");
            }
        }
        return new ImmutableCompactArray<>(this.state);
    }
}
