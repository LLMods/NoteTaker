package org.cincospenguinos.note_taker;

import com.avaje.ebean.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the plugin.
 */
public class Main extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        super.onEnable();

        if(!setupConfiguration()){
            getLogger().log(Level.SEVERE, "Please modify config.yml properly to use this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }


        this.getCommand("note").setExecutor(new NoteCommand());
    }

    @Override
    public void onDisable(){

        super.onDisable();
    }

    /**
     * Attempts to setup the configuration file properly.
     *
     * @return true if it worked
     */
    private boolean setupConfiguration() {
        if(!getDataFolder().exists()){
            getLogger().info("Data folder does not exist; creating...");
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if(!file.exists()){
            getLogger().log(Level.WARNING, "config.yml not found! Creating...");

            FileConfiguration configuration = getConfig();
            configuration.addDefault("username", "");
            configuration.addDefault("password", "");
            configuration.addDefault("host", "");
            configuration.addDefault("schema", "");
            saveDefaultConfig();

            return false;
        }

        getLogger().info("config.yml found; attempting to pull up config...");
        config = getConfig();
        return verifyConfig(config);
    }

    private boolean verifyConfig(FileConfiguration config) {
        return config.getString("username") != null && !config.getString("username").isEmpty()
                && config.getString("password") != null && !config.getString("password").isEmpty()
                && config.getString("host") != null && !config.getString("host").isEmpty()
                && config.getString("schema") != null && !config.getString("schema").isEmpty();
    }
}
