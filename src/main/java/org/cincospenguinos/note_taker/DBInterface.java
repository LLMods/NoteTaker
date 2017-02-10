package org.cincospenguinos.note_taker;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Interface for the database.
 */
public class DBInterface {

    private static final String TABLE_NAME = "NoteTakerNotes";

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
                query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        "id INT PRIMARY KEY NOT NULL AUTOINCREMENT," +
                        "username VARCHAR(50) NOT NULL," +
                        "note TEXT NOT NULL)";
            case MYSQL:
                query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        "id INT NOT NULL AUTO_INCREMENT," +
                        "username VARCHAR(50) NOT NULL," +
                        "note TEXT NOT NULL," +
                        "PRIMARY KEY(id))";
                break;
            case POSTGRES:
                query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
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

    /**
     * Creates note for user provided with text provided
     * @param username - User adding the note
     * @param note - Note to add
     * @return true if it worked or false if something happened
     */
    public static boolean createNote(String username, String note) {
        String sql = "INSERT INTO " + TABLE_NAME + "VALUES('?', '?')";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, note);

            stmt.execute();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to create a note!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String readNote (int id) {
        return null; // TODO: This
    }

    public static boolean deleteNote(int id){
        return false; // TODO: This
    }

    public static ArrayList<String> listNotes(String username){
        String sql = "SELECT * FROM " + TABLE_NAME + "WHERE username = '?'";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet r = stmt.executeQuery();

            // TODO: Finish this
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // TODO: This
    }

    /*
     * HELPERS
     */


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
