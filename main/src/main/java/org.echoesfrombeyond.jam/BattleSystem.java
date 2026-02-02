package org.echoesfrombeyond.jam;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BattleSystem extends EntityTickingSystem<EntityStore> {
  public record BattleData(int spawns, float spawnInterval) {}

  public static final Int2ObjectMap<BattleData> DAYS;

  public static final int FINAL_DAY = 5;

  static {
    DAYS = new Int2ObjectOpenHashMap<>();
    DAYS.put(1, new BattleData(5, 5));
    DAYS.put(2, new BattleData(10, 5));
    DAYS.put(3, new BattleData(12, 5));
    DAYS.put(4, new BattleData(15, 5));
    DAYS.put(FINAL_DAY, new BattleData(20, 5));
  }

  @Override
  public void tick(
      float v,
      int i,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var stage = archetypeChunk.getComponent(i, Plugin.getStageType());
    assert stage != null;

    if (!stage.isBattle) return;
    stage.battleTime += v;

    var battleTime = stage.battleTime;
    var world = store.getExternalData().getWorld();

    world.execute(
        () -> {
          var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());

          if (jam.towerHealth <= 0 && jam.day <= FINAL_DAY) {
            System.out.println("YOU LOST...");
            return;
          }

          if (!DAYS.containsKey(jam.day)) return;
          var currentDayData = DAYS.get(jam.day);

          if (stage.killed >= currentDayData.spawns) {
            if (++jam.day > FINAL_DAY) {
              System.out.println("YOU WIN");
            } else {
              System.out.println("YOU SURVIVED THE NIGHT...");
            }
            stage.killed = 0;
            stage.spawned = 0;
            stage.isBattle = false;
            stage.battleTime = 0;
            return;
          }

          if (battleTime < currentDayData.spawnInterval || stage.spawned >= currentDayData.spawns)
            return;
          stage.battleTime = 0;

          if (++stage.spawned < currentDayData.spawns) {
            System.out.println("Spawned enemy");
            spawnEnemy(archetypeChunk.getReferenceTo(i), store);
          } else {
            System.out.println("All enemies spawned");
          }
        });
  }

  private void spawnEnemy(Ref<EntityStore> player, Store<EntityStore> store) {
    var holder = EntityStore.REGISTRY.newHolder();

    var ma = ModelAsset.getAssetMap().getAsset("Kweebec_Seedling");
    assert ma != null;

    var model = Model.createScaledModel(ma, 5.0F);
    assert model.getBoundingBox() != null;

    var transform = new TransformComponent();
    transform.getPosition().assign(1145, 150, -2687);

    holder.addComponent(TransformComponent.getComponentType(), transform);
    holder.addComponent(HeadRotation.getComponentType(), new HeadRotation());
    holder.addComponent(
        PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
    holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
    holder.addComponent(
        NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));

    var enemy = new EnemyComponent();
    enemy.player = player;

    holder.addComponent(Plugin.getEnemyType(), enemy);

    var ct = NPCEntity.getComponentType();
    assert ct != null;

    var npc = new NPCEntity();
    var index = NPCPlugin.get().getIndex("Sim_Enemy");

    npc.setRoleIndex(index);
    npc.setRoleName("Sim_Enemy");

    holder.addComponent(ct, npc);

    holder.ensureComponent(UUIDComponent.getComponentType());
    holder.ensureComponent(Interactable.getComponentType());

    var newRef = store.addEntity(holder, AddReason.SPAWN);
    if (newRef == null) return;

    var newNpcComp = store.getComponent(newRef, NPCEntity.getComponentType());
    if (newNpcComp == null) return;

    var target = Plugin.RADIO_LOC;

    var tp = new TransientPath();
    // tp.addWaypoint(transform.getPosition().clone(), transform.getRotation().clone());
    tp.addWaypoint(target.clone().toVector3d(), new Vector3f(0, 0, 0));

    newNpcComp.getPathManager().setTransientPath(tp);
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(
        Player.getComponentType(), PlayerRef.getComponentType(), Plugin.getStageType());
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(
        new SystemDependency<>(Order.AFTER, EnemyDamageTowerSystem.class),
        new SystemDependency<>(Order.AFTER, EnemyDeathSystem.class));
  }
}
