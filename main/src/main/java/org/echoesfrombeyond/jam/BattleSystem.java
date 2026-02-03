package org.echoesfrombeyond.jam;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BattleSystem extends EntityTickingSystem<EntityStore> {
  public record BattleData(int spawns, float spawnInterval) {}

  private static BattleData dataForDay(int day) {
    return new BattleData(
        5 + (int) (day * 1.5), (float) Math.max(0.85F, 5F - ((float) day * 0.75)));
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

    if (!stage.isBattle && !stage.won) return;
    stage.battleTime += v;

    var battleTime = stage.battleTime;
    var world = store.getExternalData().getWorld();

    var ref = archetypeChunk.getReferenceTo(i);

    world.execute(
        () -> {
          var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());

          if (stage.won) {
            clearEverything(jam, world, stage, ref, store, true);
            return;
          }

          if (jam.towerHealth <= 0) {
            clearEverything(jam, world, stage, ref, store, false);
            return;
          }

          var currentDayData = dataForDay(jam.day);

          if (stage.killed >= currentDayData.spawns) {
            ++jam.day;
            stage.reset();
            if (ref.isValid()) {
              world.sendMessage(Message.raw("You survived the attack!"));
              store.invoke(ref, new HudUpdateSystem.Event());
            }
            return;
          }

          if (battleTime < currentDayData.spawnInterval || stage.spawned >= currentDayData.spawns)
            return;
          stage.battleTime = 0;

          spawnEnemy(ref, store);
          if (++stage.spawned < currentDayData.spawns) {
            System.out.println("Spawned enemy");
          } else {
            System.out.println("All enemies spawned");
          }
        });
  }

  private void clearEverything(
      JamSave jam,
      World world,
      GameStageComponent stage,
      Ref<EntityStore> ref,
      Store<EntityStore> store,
      boolean win) {
    Plugin.clearAllEnemies(world);

    for (var building : jam.buildings) {
      if (building.type == JamSave.BuildingType.CommandTent
          || building.type == JamSave.BuildingType.RadioTower) continue;

      for (int x = building.min.x; x <= building.max.x; x++) {
        for (int y = building.min.y; y <= building.max.y; y++) {
          for (int z = building.min.z; z <= building.max.z; z++) {
            world.setBlock(x, y, z, "Empty");
          }
        }
      }
    }

    jam.assignInitialValues();
    stage.reset();

    if (!ref.isValid()) return;
    store.invoke(ref, new HudUpdateSystem.Event());

    var pr = store.getComponent(ref, PlayerRef.getComponentType());
    if (pr == null) return;

    if (win) OpenWinUI.openWinPopup(ref, pr);
    else OpenLoseUI.openLosePopup(ref, pr);
  }

  private void spawnEnemy(Ref<EntityStore> player, Store<EntityStore> store) {
    var holder = EntityStore.REGISTRY.newHolder();

    var ma = ModelAsset.getAssetMap().getAsset("Spark_Living");
    assert ma != null;

    var model = Model.createScaledModel(ma, 4.0F);
    assert model.getBoundingBox() != null;

    var transform = new TransformComponent();
    transform.getPosition().assign(Plugin.ENEMY_SPAWN_LOC.toVector3d());

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
        new SystemDependency<>(Order.AFTER, EnemyDeathSystem.class),
        new SystemDependency<>(Order.AFTER, TurretFireSystem.class));
  }
}
