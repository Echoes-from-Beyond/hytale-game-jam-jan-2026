package org.echoesfrombeyond.jam;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.player.MouseInteraction;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.SwitchActiveSlotEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public class Plugin extends JavaPlugin {
  public static final Vector3d VEC = new Vector3d(1127, 143, -2685);

  public static final Vector3i TENT_LOC = new Vector3i(1127, 143, -2685);

  public static final Vector3i RADIO_LOC = new Vector3i(1135, 143, -2685);

  public static final Vector3i ENEMY_SPAWN_LOC = new Vector3i(1178, 145, -2658);

  private static @Nullable ResourceType<ChunkStore, JamSave> JAM_TYPE;
  private static @Nullable ComponentType<EntityStore, PlacePreviewComponent> PLACE_TYPE;
  private static @Nullable ComponentType<EntityStore, GameStageComponent> STAGE_TYPE;
  private static @Nullable ComponentType<EntityStore, EnemyComponent> ENEMY_TYPE;
  private static @Nullable AssetPack ASSET_PACK;

  public static ResourceType<ChunkStore, JamSave> getJamType() {
    assert JAM_TYPE != null;
    return JAM_TYPE;
  }

  public static ComponentType<EntityStore, PlacePreviewComponent> getPlaceType() {
    assert PLACE_TYPE != null;
    return PLACE_TYPE;
  }

  public static ComponentType<EntityStore, GameStageComponent> getStageType() {
    assert STAGE_TYPE != null;
    return STAGE_TYPE;
  }

  public static ComponentType<EntityStore, EnemyComponent> getEnemyType() {
    assert ENEMY_TYPE != null;
    return ENEMY_TYPE;
  }

  public static AssetPack getAssetPack() {
    return AssetModule.get().getAssetPack("org.echoesfrombeyond:Scrapvengers");
  }

  public static void clearAllEnemies(World world) {
    var npct = NPCEntity.getComponentType();
    assert npct != null;

    world
        .getEntityStore()
        .getStore()
        .forEachChunk(
            Query.and(NPCEntity.getComponentType(), Query.not(DeathComponent.getComponentType())),
            (chunk, buffer) -> {
              for (int i = 0; i < chunk.size(); i++) {
                var npc = chunk.getComponent(i, npct);
                if (npc == null) continue;

                var role = npc.getRole();
                if (role == null) continue;

                if (Objects.equals(role.getRoleName(), "Sim_Enemy")) {
                  DamageSystems.executeDamage(
                      i,
                      chunk,
                      buffer,
                      new Damage(Damage.NULL_SOURCE, new DamageCause("OutOfWorld"), 1000f));
                }
              }
            });
  }

  public Plugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    JAM_TYPE = getChunkStoreRegistry().registerResource(JamSave.class, "Jam", JamSave.CODEC);
    PLACE_TYPE =
        getEntityStoreRegistry()
            .registerComponent(PlacePreviewComponent.class, PlacePreviewComponent::new);
    STAGE_TYPE =
        getEntityStoreRegistry()
            .registerComponent(GameStageComponent.class, GameStageComponent::new);
    ENEMY_TYPE =
        getEntityStoreRegistry().registerComponent(EnemyComponent.class, EnemyComponent::new);

    getEntityStoreRegistry().registerSystem(new EnemyDamageTowerSystem());
    getEntityStoreRegistry().registerSystem(new EnemyDeathSystem());
    getEntityStoreRegistry().registerSystem(new TurretFireSystem());
    getEntityStoreRegistry().registerSystem(new BattleSystem());

    getEntityStoreRegistry().registerSystem(new MouseClickSystem());
    getEntityStoreRegistry().registerSystem(new HudUpdateSystem());
    getEntityStoreRegistry().registerSystem(new PlacePreviewSystem());
    getEntityStoreRegistry().registerSystem(new RemovePreviewSystem());

    getEntityStoreRegistry()
        .registerSystem(new CancelSystem<>(DropItemEvent.PlayerRequest.class) {});
    getEntityStoreRegistry().registerSystem(new CancelSystem<>(SwitchActiveSlotEvent.class) {});
    getCodecRegistry(Interaction.CODEC)
        .register("Open_Select_Building", OpenSelectBuildingUI.class, OpenSelectBuildingUI.CODEC);
    getCodecRegistry(Interaction.CODEC)
        .register("Open_Advance_Day", OpenAdvanceDayUI.class, OpenAdvanceDayUI.CODEC);

    PacketAdapters.registerInbound(
        (PlayerPacketWatcher)
            (playerRef, packet) -> {
              if (packet instanceof MouseInteraction interaction) {
                var in = interaction.worldInteraction;
                if (in == null) return;

                var bp = in.blockPosition;
                if (bp == null) return;

                var worldUuid = playerRef.getWorldUuid();
                if (worldUuid == null) return;

                var world = Universe.get().getWorld(worldUuid);
                if (world == null) return;

                var mb = interaction.mouseButton;
                if (mb == null) {
                  // Mouse hover position update
                  world.execute(
                      () -> {
                        var refRef = playerRef.getReference();
                        if (refRef == null) return;

                        var place = refRef.getStore().getComponent(refRef, getPlaceType());
                        if (place == null) return;

                        var vbp = new Vector3i(bp.x, bp.y, bp.z);

                        BlockType type;
                        while ((type = world.getBlockType(vbp)) != null
                            && (type.getId().equals(PlacePreviewSystem.PREVIEW_BLOCK_ID)
                                || type.getId().equals("Empty"))) {
                          vbp.subtract(0, 1, 0);
                        }

                        if (!vbp.equals(place.cursorHoverPos)) {
                          place.cursorHoverPos.assign(vbp);
                          place.dirty = true;
                        }
                      });
                  return;
                }

                if (mb.state != MouseButtonState.Pressed) return;

                world.execute(
                    () -> {
                      var refRef = playerRef.getReference();
                      if (refRef == null) return;

                      var store = world.getEntityStore();
                      var storeStore = store.getStore();
                      storeStore.invoke(
                          refRef,
                          new MouseClickEvent(new Vector3i(bp.x, bp.y, bp.z), mb.mouseButtonType));
                    });
              }
            });

    getEventRegistry()
        .registerGlobal(
            PlayerReadyEvent.class,
            ready -> {
              var player = ready.getPlayer();
              var world = player.getWorld();
              if (world == null) return;

              world.execute(
                  () -> {
                    var ref = player.getReference();
                    if (ref == null) return;

                    var store = ref.getStore();
                    var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef == null) return;

                    if (!world.getName().equals("World-" + playerRef.getUuid()))
                      leaveSimWorld(playerRef);
                    else {
                      clearAllEnemies(world);
                      joinSimWorld(playerRef);
                    }
                  });
            });

    getEventRegistry()
        .registerGlobal(
            PlayerConnectEvent.class,
            connect -> {
              var universe = Universe.get();
              var uuid = connect.getPlayerRef().getUuid();
              var name = "World-" + uuid;

              var existing = universe.getWorld(name);
              if (existing != null) {
                connect.setWorld(existing);
                return;
              }

              World world;
              try {
                world =
                    universe
                        .makeWorld(name, Path.of("universe", "worlds", name), genWorldConfig())
                        .get();
              } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
              }

              connect.setWorld(world);
            });
  }

  private static void leaveSimWorld(PlayerRef ref) {
    var r = ref.getReference();
    if (r != null) {
      var player = r.getStore().getComponent(r, Player.getComponentType());
      if (player != null)
        player
            .getHudManager()
            .setCustomHud(
                ref,
                new CustomUIHud(ref) {
                  @Override
                  protected void build(UICommandBuilder var1) {}
                });
    }

    ref.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
  }

  private static void joinSimWorld(PlayerRef playerRef) {
    setPos(playerRef);

    var ref = playerRef.getReference();
    if (ref != null) {
      var player = ref.getStore().getComponent(ref, Player.getComponentType());
      if (player != null) {
        var inventory = player.getInventory();
        inventory.getHotbar().setItemStackForSlot((short) 0, new ItemStack("Open_Select_Building"));
        inventory.getHotbar().setItemStackForSlot((short) 1, new ItemStack("Open_Advance_Day"));
        player.getHudManager().setCustomHud(playerRef, new SimHud(playerRef));
      }

      ref.getStore().invoke(ref, new HudUpdateSystem.Event());
      ref.getStore().addComponent(ref, getStageType(), new GameStageComponent());
      OpenJoinUI.openJoinPopup(ref, playerRef);
    }

    setDefaultCameraPosition(playerRef);
  }

  public static void setBuildingFocusCameraPosition(PlayerRef playerRef, Vector3i buildingPos) {
    var playerPos = playerRef.getTransform().getPosition().toVector3i();

    var relativeToPlayer = buildingPos.clone().subtract(playerPos).add(-7, 5, -7);

    ServerCameraSettings settings = new ServerCameraSettings();
    settings.positionLerpSpeed = 0.05f;
    settings.rotationLerpSpeed = 0.05f;
    settings.distance = 0.0f;
    settings.displayCursor = true;
    settings.isFirstPerson = false;
    settings.movementMultiplier = new com.hypixel.hytale.protocol.Vector3f(0, 0, 0);
    settings.eyeOffset = true;
    settings.positionOffset =
        new Position(relativeToPlayer.x, relativeToPlayer.y, relativeToPlayer.z);
    settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
    settings.rotationType = RotationType.Custom;
    settings.rotation = new Direction(-2.7f, -0.75f, 0.0f);
    settings.mouseInputType = MouseInputType.LookAtTargetEntity;
    settings.planeNormal = new com.hypixel.hytale.protocol.Vector3f(0.0f, 1.0f, 0.0f);
    settings.sendMouseMotion = true;

    playerRef
        .getPacketHandler()
        .writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, settings));
  }

  public static void setDefaultCameraPosition(PlayerRef playerRef) {
    ServerCameraSettings settings = new ServerCameraSettings();
    settings.positionLerpSpeed = 0.1f;
    settings.rotationLerpSpeed = 0.1f;
    settings.distance = 25.0f;
    settings.displayCursor = true;
    settings.isFirstPerson = false;
    settings.movementMultiplier = new com.hypixel.hytale.protocol.Vector3f(0, 0, 0);
    settings.eyeOffset = true;
    settings.positionOffset = new Position(-7.0, 5.0, -7.0);
    settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
    settings.rotationType = RotationType.Custom;
    settings.rotation = new Direction(-2.7f, -0.75f, 0.0f);
    settings.mouseInputType = MouseInputType.LookAtTargetEntity;
    settings.planeNormal = new com.hypixel.hytale.protocol.Vector3f(0.0f, 1.0f, 0.0f);
    settings.sendMouseMotion = true;

    playerRef
        .getPacketHandler()
        .writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, settings));
  }

  private WorldConfig genWorldConfig() {
    var conf = new WorldConfig();
    conf.setGameTimePaused(true);
    conf.setSeed(1769837468940L);
    conf.setSpawningNPC(false);

    return conf;
  }

  public static void setPos(PlayerRef playerRef) {
    var currentUuid = playerRef.getWorldUuid();
    if (currentUuid == null) return;

    var current = Universe.get().getWorld(currentUuid);
    if (current == null) return;

    Ref<EntityStore> ref = playerRef.getReference();
    if (ref == null) return;

    ref.getStore()
        .addComponent(
            ref, Teleport.getComponentType(), new Teleport(VEC.clone(), new Vector3f(0, 0, 0)));

    generateFixedPrefabs(current, ref);
  }

  private static void generateFixedPrefabs(World world, Ref<EntityStore> ref) {
    world.execute(
        () -> {
          JamSave save = world.getChunkStore().getStore().getResource(Plugin.getJamType());
          if (!save.buildings.isEmpty()) {
            return;
          }

          var tentPrefabBuffer =
              PrefabBufferUtil.getCached(
                  AssetModule.get()
                      .getAssetPack("org.echoesfrombeyond:Scrapvengers")
                      .getRoot()
                      .resolve("Server")
                      .resolve("Prefabs")
                      .resolve(JamSave.BuildingType.CommandTent.getPrefabAsset()));

          var towerPrefabBuffer =
              PrefabBufferUtil.getCached(
                  AssetModule.get()
                      .getAssetPack("org.echoesfrombeyond:Scrapvengers")
                      .getRoot()
                      .resolve("Server")
                      .resolve("Prefabs")
                      .resolve(JamSave.BuildingType.RadioTower.getPrefabAsset()));

          var spawnIndicatorPrefabBuffer =
              PrefabBufferUtil.getCached(
                  AssetModule.get()
                      .getAssetPack("org.echoesfrombeyond:Scrapvengers")
                      .getRoot()
                      .resolve("Server")
                      .resolve("Prefabs")
                      .resolve(JamSave.BuildingType.SpawnIndicator.getPrefabAsset()));

          PrefabUtil.paste(
              tentPrefabBuffer,
              world,
              TENT_LOC,
              Rotation.None,
              true,
              new FastRandom(),
              world.getEntityStore().getStore());

          PrefabUtil.paste(
              towerPrefabBuffer,
              world,
              RADIO_LOC,
              Rotation.None,
              true,
              new FastRandom(),
              world.getEntityStore().getStore());

          var spawnloc = ENEMY_SPAWN_LOC.clone().add(0, 3, 0);
          PrefabUtil.paste(
              spawnIndicatorPrefabBuffer,
              world,
              spawnloc,
              Rotation.None,
              true,
              new FastRandom(),
              world.getEntityStore().getStore());

          JamSave.Building tent = new JamSave.Building();
          tent.type = JamSave.BuildingType.CommandTent;
          tent.min =
              new Vector3i(
                  TENT_LOC.x + tentPrefabBuffer.getMinX(),
                  TENT_LOC.y + tentPrefabBuffer.getMinY(),
                  TENT_LOC.z + tentPrefabBuffer.getMinZ());
          tent.max =
              new Vector3i(
                  TENT_LOC.x + tentPrefabBuffer.getMaxX(),
                  TENT_LOC.y + tentPrefabBuffer.getMaxY(),
                  TENT_LOC.z + tentPrefabBuffer.getMaxZ());

          JamSave.Building tower = new JamSave.Building();
          tower.type = JamSave.BuildingType.RadioTower;
          tower.min =
              new Vector3i(
                  RADIO_LOC.x + towerPrefabBuffer.getMinX(),
                  RADIO_LOC.y + towerPrefabBuffer.getMinY(),
                  RADIO_LOC.z + towerPrefabBuffer.getMinZ());
          tower.max =
              new Vector3i(
                  RADIO_LOC.x + towerPrefabBuffer.getMaxX(),
                  RADIO_LOC.y + towerPrefabBuffer.getMaxY(),
                  RADIO_LOC.z + towerPrefabBuffer.getMaxZ());

          JamSave.Building spawnIndicator = new JamSave.Building();
          spawnIndicator.type = JamSave.BuildingType.SpawnIndicator;
          spawnIndicator.min =
              new Vector3i(
                  spawnloc.x + spawnIndicatorPrefabBuffer.getMinX(),
                  spawnloc.y + spawnIndicatorPrefabBuffer.getMinY(),
                  spawnloc.z + spawnIndicatorPrefabBuffer.getMinZ());
          spawnIndicator.max =
              new Vector3i(
                  spawnloc.x + spawnIndicatorPrefabBuffer.getMaxX(),
                  spawnloc.y + spawnIndicatorPrefabBuffer.getMaxY(),
                  spawnloc.z + spawnIndicatorPrefabBuffer.getMaxZ());

          save.buildings.add(tent);
          save.buildings.add(tower);
          save.buildings.add(spawnIndicator);
        });
  }
}
