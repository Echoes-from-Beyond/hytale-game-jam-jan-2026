package org.echoesfrombeyond.jam;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@NullMarked
public class Plugin extends JavaPlugin {
  public Plugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    var universe = Universe.get();
  }
}
