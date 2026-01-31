package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.player.MouseInteraction;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public class Plugin extends JavaPlugin {
  private static final Vector3d VEC = new Vector3d(684, 148, -2334);

  private static @Nullable ComponentType<ChunkStore, JamComponent> JAM_TYPE;

  public static ComponentType<ChunkStore, JamComponent> getJamType() {
    assert JAM_TYPE != null;
    return JAM_TYPE;
  }

  public Plugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    JAM_TYPE =
        getChunkStoreRegistry()
            .registerComponent(JamComponent.class, "Jam_Component", JamComponent.CODEC);

    getChunkStoreRegistry().registerSystem(new MouseClickSystem());

    PacketAdapters.registerInbound(
        new PlayerPacketWatcher() {
          @Override
          public void accept(PlayerRef playerRef, Packet packet) {
            if (packet instanceof MouseInteraction interaction) {
              var in = interaction.worldInteraction;
              if (in == null) return;

              var bp = in.blockPosition;
              if (bp == null) return;

              var mb = interaction.mouseButton;
              if (mb == null) return;

              var worldUuid = playerRef.getWorldUuid();
              if (worldUuid == null) return;
              var world = Universe.get().getWorld(worldUuid);
              if (world == null) return;

              world.execute(
                  () -> {
                    // counterstrike reference?
                    var cs = world.getChunkStore();
                    var chuncc =
                        ChunkUtil.indexChunkFromBlock(
                            (int) Math.floor(VEC.x), (int) Math.floor(VEC.z));

                    var cref = cs.getChunkReference(chuncc);
                    if (cref == null) return;

                    var actualStore = cref.getStore();

                    actualStore.ensureComponent(cref, getJamType());

                    actualStore.invoke(
                        cref,
                        new MouseClickEvent(new Vector3i(bp.x, bp.y, bp.z), mb.mouseButtonType));
                  });
            }
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
                    else joinSimWorld(playerRef);
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
    ref.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
  }

  private static void joinSimWorld(PlayerRef ref) {
    setPos(ref);

    ServerCameraSettings settings = new ServerCameraSettings();
    settings.positionLerpSpeed = 0.2f;
    settings.rotationLerpSpeed = 0.2f;
    settings.distance = 20.0f;
    settings.displayCursor = true;
    settings.isFirstPerson = false;
    settings.movementForceRotationType = MovementForceRotationType.Custom;
    settings.movementForceRotation = new Direction(-0.7853981634f, 0.0f, 0.0f);
    settings.movementMultiplier = new com.hypixel.hytale.protocol.Vector3f(0, 0, 0);
    settings.eyeOffset = true;
    settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
    settings.rotationType = RotationType.Custom;
    settings.rotation = new Direction(0.0f, -1.5707964f, 0.0f);
    settings.mouseInputType = MouseInputType.LookAtTargetEntity;
    settings.planeNormal = new com.hypixel.hytale.protocol.Vector3f(0.0f, 1.0f, 0.0f);

    ref.getPacketHandler()
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
  }
}
