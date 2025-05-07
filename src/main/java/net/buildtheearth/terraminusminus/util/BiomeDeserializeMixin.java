package net.buildtheearth.terraminusminus.util;

import static net.daporkchop.lib.common.util.PValidation.checkArg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.generator.BiomesRegistry;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import net.buildtheearth.terraminusminus.substitutes.MetaBiome;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(converter = BiomeDeserializeMixin.Converter.class)
public abstract class BiomeDeserializeMixin {
    protected static class Converter extends StdConverter<String, IBiome<?>> {
        @Override
        public IBiome<?> convert(@NonNull String value) {
            IBiome<?> biome = BiomesRegistry.getById(value);
            checkArg(biome != null, "unknown biome id: %s", value);
            return biome;
        }
    }
}
