package org.cincospenguinos.note_taker;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the plugin.
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("note").setExecutor(new NoteCommand());
    }

    @Override
    public void onDisable(){

    }
}
