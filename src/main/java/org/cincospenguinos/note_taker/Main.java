package org.cincospenguinos.note_taker;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the plugin.
 */
public class Main extends JavaPlugin {

    private FileConfiguration config;
    private static Logger logger;

    @Override
    public void onEnable() {
        super.onEnable();

        // First setup the configuration file and ensure that that's working
        if(!setupConfiguration()){
            getLogger().log(Level.SEVERE, "Please modify config.yml properly to use this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Then check and make sure the database works properly
        Connection c = DBInterface.getConnection(config, getDataFolder());

        if(c == null){
            getLogger().log(Level.SEVERE, "There was an issue connecting to the database. Please make sure " +
                    "that connection is possible and try to reload.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!DBInterface.setupTable()) {
            getLogger().log(Level.SEVERE, "The database table could not be setup! Please attempt to fix and " +
                    "then reload.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Database connected!");
        this.getCommand("note").setExecutor(new NoteCommand());

        logger = getLogger();
    }

    @Override
    public void onDisable(){
        DBInterface.disconnect();

        super.onDisable();
    }

    /**
     * Static method to log directly to the log file.
     *
     * @param lvl - Log level
     * @param message - to put in the log file
     */
    public static void log(Level lvl, String message){
        logger.log(lvl, message);
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

    /**
     * Helper method. Returns true if the config file is properly configured.
     *
     * @param config - FileConfiguration to check
     * @return true if the config file is managed properly
     */
    private boolean verifyConfig(FileConfiguration config) {
        return config.getString("username") != null && config.getString("password") != null
                && config.getString("host") != null && config.getString("schema") != null
                && config.getString("engine") != null;
    }
}
