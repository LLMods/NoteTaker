package org.cincospenguinos.note_taker;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

/**
 * Interface for the database.
 */
public class DBInterface {

    private static Connection connection;

    private enum DatabaseType {
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
            DatabaseType type = getDatabaseType(configuration);

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
                    getLogger().log(Level.SEVERE, "Invalid database option (did you specify a database engine?)");
                    return null;
            }

            getLogger().info("URL: " + url);

            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Cannot connect to database!");
                e.printStackTrace();
                return null;
            }
        }

        return connection;
    }

    private static DatabaseType getDatabaseType(FileConfiguration configuration){
        String type = configuration.getString("engine");

        if(type.equalsIgnoreCase("mysql"))
            return DatabaseType.MYSQL;
        else if(type.equalsIgnoreCase("postgres") || type.equalsIgnoreCase("postgresql"))
            return DatabaseType.POSTGRES;
        else if(type.equalsIgnoreCase("sqlite"))
            return DatabaseType.SQLITE;

        return DatabaseType.INVALID;
    }
}
