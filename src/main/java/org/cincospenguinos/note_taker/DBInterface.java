package org.cincospenguinos.note_taker;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Interface for the database.
 */
public class DBInterface {

    private static final String TABLE_NAME = "NoteTakerNotes";

    private static Connection connection;
    private static DatabaseEngine databaseEngine;
    private static Plugin plugin;
    private static String schemaName;

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
            schemaName = schema;
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
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username VARCHAR(50) NOT NULL," +
                        "note TEXT NOT NULL)";
                break;
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
            return false;
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
        String sql = "";

        switch(databaseEngine){
            case SQLITE:
                sql = "INSERT INTO " + TABLE_NAME + "(username, note) VALUES (?, ?)";
                break;
            case MYSQL:
                sql = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?)";
                break;
            case POSTGRES:
                break;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, note);
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to create a note!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String readNote (int id, String username) {
        String sql = "SELECT 1 FROM " + TABLE_NAME + " WHERE id = ? AND username = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, username);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "A SQL error occurred when attempting to read a note!");
            e.printStackTrace();
        }


        return null; // TODO: This
    }

    public static boolean deleteNote(int id, String username){
        return false; // TODO: This
    }

    public static TreeMap<Integer, String> listNotes(String username){
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ?";

        TreeMap<Integer, String> notes = new TreeMap<Integer, String>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet set = stmt.executeQuery();

            while(set.next())
                notes.put(set.getInt("id"), set.getString("note"));


        } catch (SQLException e){
            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred when attempting to get the notes list!");
            e.printStackTrace();
            return null;
        }

        return notes;
    }

    /**
     * Disconnect from the database
     */
    public static void disconnect() {
        try {
            if(connection != null && !connection.isClosed())
                connection.close();

            connection = null; // TODO: Is this ok?
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "An exception was thrown when attempting to disconnect from the DB!");
            e.printStackTrace();
        }
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
