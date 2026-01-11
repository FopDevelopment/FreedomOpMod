/*
 * Cleanroom Generator
 * Copyright (C) 2011-2012 nvx
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nz.jovial.fopm.world;

import nz.jovial.fopm.util.FLog;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CleanroomChunkGenerator extends ChunkGenerator
{

    private List<Layer> layers = new ArrayList<>();

    private static class Layer {
        int height;
        Material material;

        Layer(int height, Material material) {
            this.height = height;
            this.material = material;
        }
    }

    public CleanroomChunkGenerator()
    {
        this("64,stone");
    }

    public CleanroomChunkGenerator(String id)
    {
        if (id == null || id.isEmpty()) {
            layers.add(new Layer(64, Material.STONE));
            return;
        }

        try
        {
            if (id.startsWith(".")) {
                id = id.substring(1);
            } else {
                layers.add(new Layer(1, Material.BEDROCK));
            }

            if (id.length() > 0)
            {
                String[] tokens = id.split(",");
                if (tokens.length % 2 != 0) throw new Exception("Invalid format");

                for (int i = 0; i < tokens.length; i += 2)
                {
                    int height = Integer.parseInt(tokens[i]);
                    if (height <= 0) height = 64;

                    String matName = tokens[i+1];
                    if (matName.contains(":")) {
                        matName = matName.split(":")[0];
                    }

                    Material mat = Material.matchMaterial(matName);
                    if (mat == null) {
                        if (matName.equalsIgnoreCase("grass")) mat = Material.GRASS_BLOCK;
                        else {
                            mat = Material.STONE;
                            FLog.warning("Invalid material " + matName + ", defaulting to STONE");
                        }
                    }

                    layers.add(new Layer(height, mat));
                }
            }
        }
        catch (Exception e)
        {
            FLog.severe("Error parsing generator ID: " + e.getMessage());
            layers.clear();
            layers.add(new Layer(1, Material.BEDROCK));
            layers.add(new Layer(64, Material.STONE));
        }
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome)
    {
        ChunkData chunkData = createChunkData(world);
        int currentY = 0;

        for (Layer layer : layers) {
            if (currentY >= world.getMaxHeight()) break;

            int endY = Math.min(currentY + layer.height, world.getMaxHeight());
            if (layer.material != null && layer.material != Material.AIR) {
                chunkData.setRegion(0, currentY, 0, 16, endY, 16, layer.material);
            }
            currentY = endY;
        }

        return chunkData;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        return new ArrayList<>();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random)
    {
        return new Location(world, 0, world.getHighestBlockYAt(0, 0) + 1, 0);
    }
}
