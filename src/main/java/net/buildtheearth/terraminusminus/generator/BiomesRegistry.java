package net.buildtheearth.terraminusminus.generator;

import net.buildtheearth.terraminusminus.substitutes.MetaBiome;
import net.buildtheearth.terraminusminus.substitutes.IBiome;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry class to hold all IBiome's used, get the default registered IBiome,
 * and map all IBiome's, including pseudo based ones, like MetaBiome, to real biome based IBiome which your mod/plugin/project uses.
 * <p>
 * Must be extended and new class set via BiomesRegistry.setDefaultBiomesRegistry, as by default BiomesRegistry.map(IBiome biome) returns biome as it is.
 *
 * @author DavixDevelop
 */
public abstract class BiomesRegistry {
    private static BiomesRegistry INSTANCE;

    public static Map<String, IBiome<?>> REGISTRY = new ConcurrentHashMap<>();

    static {
        //Populate registry by initial biome list based on MetaBiome
        for(MetaBiome biomeEnum : MetaBiome.values()){
            //Register the biome in the registry
            BiomesRegistry.registerBiome(biomeEnum);
        }
    }

    public static BiomesRegistry get(){
        return INSTANCE;
    }

    public static void setDefaultBiomesRegistry(BiomesRegistry biomesRegistry){
        INSTANCE = biomesRegistry;
    }

    public static void registerBiome(IBiome<?> biome){
        if(REGISTRY.containsKey(biome.getId()))
            REGISTRY.replace(biome.getId(), biome);
        else
            REGISTRY.put(biome.getId(), biome);
    }

    /**
     * Get the IBiome from the REGISTRY.
     * Register all biomes in classes that implement IBiome to REGISTRY beforehand
     *
     * @param biomeID The id of the biome
     * @return The biome from the id
     */
    public static IBiome<?> getById(String biomeID){
        return REGISTRY.getOrDefault(biomeID, getDefault());
    }

    /**
     *
     * @return Return the default IBiome instance
     */
    public static IBiome<?> getDefault() {
        return MetaBiome.getDefault();
    }

    /**
     * Map all receiving biomes, including pseudo biomes, like the substitute {@link MetaBiome},
     * to real Minecraft biomes, when used in a mod, plugin or another project, hence when used in such cases, you must implement
     * your own BiomesRegistry, and set it with BiomesRegistry.setDefaultBiomesRegistry
     * <p>
     * Ex:
     * <code>
     * <pre>
     * switch(biome.getId()){
     *      case "ocean":
     *          return RealBiome.fromRegistry(org.bukkit.block.Biome.OCEAN);
     * }</pre>
     * </code>
     *
     * @param biome Any IBiome baked by biome bakers
     * @return The real Minecraft biome based IBiome
     */
    public IBiome<?> map(IBiome<?> biome){
        return biome;
    }


}