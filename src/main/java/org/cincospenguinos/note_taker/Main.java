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

    private static Logger logger;

    @Override
    public void onEnable() {
        super.onEnable();

        logger = getLogger();

        // Check and make sure the database works properly
        Connection c = DBInterface.getConnection(getDataFolder());

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
}
