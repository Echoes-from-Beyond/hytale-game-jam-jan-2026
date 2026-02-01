package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class HudUpdateSystem extends EntityEventSystem<EntityStore, HudUpdateSystem.Event> {
  private final Archetype<EntityStore> archetype;

  public HudUpdateSystem() {
    super(Event.class);
    this.archetype = Archetype.of(Player.getComponentType());
  }

  @Override
  public Query<EntityStore> getQuery() {
    return archetype;
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer,
      HudUpdateSystem.Event ignored) {
    var world = buffer.getExternalData().getWorld();
    var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());

    var player = chunk.getComponent(i, Player.getComponentType());
    assert player != null;

    if (!(player.getHudManager().getCustomHud() instanceof SimHud simHud)) return;

    UICommandBuilder builder = new UICommandBuilder();
    builder.set("#colonists.Text", string(jam.colonists));
    builder.set("#food.Text", string(jam.food));
    builder.set("#water.Text", string(jam.water));
    builder.set("#scrap.Text", string(jam.scrap));
    simHud.update(false, builder);
  }

  private static String string(int val) {
    if (val == 69) return "69 (nice)";

    return Integer.toString(val);
  }

  public static class Event extends EcsEvent {}
}
