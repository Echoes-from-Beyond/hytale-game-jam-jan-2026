package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlacePreviewSystem extends EntityTickingSystem<EntityStore> {
  public static final String PREVIEW_BLOCK_ID = "Cloth_Block_Wool_Yellow_Light";

  @Override
  public void tick(
      float v,
      int i,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var place = archetypeChunk.getComponent(i, Plugin.getPlaceType());
    assert place != null;

    if (!place.dirty) return;

    var prefab =
        PrefabStore.get().getAssetPrefabFromAnyPack(place.previewPrefabName + ".prefab.json");
    if (prefab == null) return;

    computeSelectionAreaIfNecessary(prefab);

    var start = prefab.getSelectionMin().clone();
    var end = prefab.getSelectionMax().clone();

    var diff = end.clone().subtract(start);
    var mid = new Vector3i(diff.x / 2, 0, diff.z / 2);

    var newMin = place.cursorHoverPos.clone().subtract(mid).add(0, 1, 0);
    var newMax = newMin.clone().add(diff);

    var oldMin = place.oldMin == null ? null : place.oldMin.clone();
    var oldMax = place.oldMax == null ? null : place.oldMax.clone();

    var world = store.getExternalData().getWorld();
    world.execute(
        () -> {
          if (oldMin != null && oldMax != null) {
            remove(world, oldMin, oldMax);
          }

          for (int x = newMin.x; x <= newMax.x; x++) {
            for (int z = newMin.z; z <= newMax.z; z++) {
              var cur = world.getBlockType(x, newMin.y, z);
              if (cur == null || !cur.getId().equals("Empty")) continue;

              world.setBlock(x, newMin.y, z, PREVIEW_BLOCK_ID);
            }
          }
        });

    place.oldMin = newMin.clone();
    place.oldMax = newMax.clone();

    place.dirty = false;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Plugin.getPlaceType());
  }

  public static void remove(World world, Vector3i min, Vector3i max) {
    for (int x = min.x; x <= max.x; x++) {
      for (int z = min.z; z <= max.z; z++) {
        var cur = world.getBlockType(x, min.y, z);
        if (cur == null || !cur.getId().equals(PREVIEW_BLOCK_ID)) continue;

        world.setBlock(x, min.y, z, "Empty");
      }
    }
  }

  public static void computeSelectionAreaIfNecessary(BlockSelection selection) {
    if (selection.hasSelectionBounds()) return;

    int[] minX = new int[] {Integer.MAX_VALUE};
    int[] maxX = new int[] {Integer.MIN_VALUE};

    int[] minY = new int[] {Integer.MAX_VALUE};
    int[] maxY = new int[] {Integer.MIN_VALUE};

    int[] minZ = new int[] {Integer.MAX_VALUE};
    int[] maxZ = new int[] {Integer.MIN_VALUE};

    selection.forEachBlock(
        (x, y, z, holder) -> {
          var asset = BlockType.getAssetMap().getAsset(holder.blockId());
          if (asset == null) return;

          var id = asset.getId();
          if (id.equals("Empty")) return;

          if (x < minX[0]) minX[0] = x;
          if (y < minY[0]) minY[0] = y;
          if (z < minZ[0]) minZ[0] = z;

          if (x > maxX[0]) maxX[0] = x;
          if (y > maxY[0]) maxY[0] = y;
          if (z > maxZ[0]) maxZ[0] = z;
        });

    selection.setSelectionArea(
        new Vector3i(minX[0], minY[0], minZ[0]), new Vector3i(maxX[0], maxY[0], maxZ[0]));
  }
}
