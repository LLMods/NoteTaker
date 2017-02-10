package org.cincospenguinos.note_taker;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.logging.Level;

/**
 * Interface for the database.
 */
public class DBInterface {

    private static Connection connection;
    private static DatabaseEngine databaseEngine;
    private static Plugin plugin;

    private enum DatabaseEngine {
        MYSQL, SQLITE, POSTGRES, INVALID
    }

    /**
     * Returns a connection to the database, or null if an issue occurred.
     *
     * @param configuration - File config to pass
     * @return Connection or null
     */
    public static Connection getConnection(FileConfiguration configuration, Plugin currentPlugin){
        if (connection == null) {
            DatabaseEngine type = getDatabaseType(configuration);

            String username = configuration.getString("username");
            String password = configuration.getString("password");
            String engine = configuration.getString("engine");
            String schema = configuration.getString("schema");
            String host = configuration.getString("host");
            String url = "jdbc:" + engine;

            switch (type) {
                case MYSQL:
                case POSTGRES:
                    url += "://" + host + "/" + schema;
                    break;
                case SQLITE:
                    url += ":" + currentPlugin.getDataFolder().toString() + "/note_taker.db";
                    break;
                case INVALID:
                    currentPlugin.getLogger().log(Level.SEVERE, "Invalid database option (did you specify a database engine?)");
                    return null;
            }

            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                currentPlugin.getLogger().log(Level.SEVERE, "Cannot connect to database!");
                e.printStackTrace();
                return null;
            }

            databaseEngine = type;
            plugin = currentPlugin;
        }
        return connection;
    }

    /**
     * Sets up the table according to the type.
     *
     * @return true if the table was setup
     */
    public static boolean setupTable(){
        if(connection == null)
            return false;

        String query = "";

        switch (databaseEngine) {
            case SQLITE:
            case MYSQL:
                query = "CREATE TABLE IF NOT EXISTS NoteTakerNotes (" +
                        "id INT PRIMARY KEY NOT NULL," +
                        "username VARCHAR(50) NOT NULL," +
                        "note TEXT NOT NULL)";
                break;
            case POSTGRES:
                query = "CREATE TABLE IF NOT EXISTS NoteTakerNotes (" +
                        "id SERIAL PRIMARY KEY," +
                        "username VARCHAR(50) NOT NULL," +
                        "note TEXT NOT NULL)";
                break;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to create table!");
            e.printStackTrace();
        }

        return true;
    }

    private static DatabaseEngine getDatabaseType(FileConfiguration configuration){
        String type = configuration.getString("engine");

        if(type.equalsIgnoreCase("mysql"))
            return DatabaseEngine.MYSQL;
        else if(type.equalsIgnoreCase("postgres") || type.equalsIgnoreCase("postgresql"))
            return DatabaseEngine.POSTGRES;
        else if(type.equalsIgnoreCase("sqlite"))
            return DatabaseEngine.SQLITE;

        return DatabaseEngine.INVALID;
    }
}
