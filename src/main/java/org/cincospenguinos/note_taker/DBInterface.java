package org.cincospenguinos.note_taker;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Interface for the database.
 */
public class DBInterface {

    // Name of the DB table
    private static final String TABLE_NAME = "NoteTakerNotes";

    // Connection to the DB
    private static Connection connection;

    // What type of DB engine we are using
    private static DatabaseEngine databaseEngine;

    /**
     * Helps to figure out what DB engine the user wants to use
     */
    private enum DatabaseEngine {
        MYSQL, SQLITE, POSTGRES, INVALID
    }

    /**
     * Returns a connection to the database, or null if an issue occurred.
     *
     * @param configuration - File config to pass
     * @return Connection or null
     */
    public static Connection getConnection(FileConfiguration configuration, File dataFolder){
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
                    url += ":" + dataFolder.toString() + "/note_taker.db";
                    break;
                case INVALID:
                    Main.log(Level.SEVERE, "Invalid database option (did you specify a database engine?)");
                    return null;
            }

            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                Main.log(Level.SEVERE, "Cannot connect to database!");
                e.printStackTrace();
                return null;
            }

            databaseEngine = type;
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
            Main.log(Level.SEVERE, "An error occurred when attempting to create table!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Creates note for user provided with text provided
     *
     * @param username - User adding the note
     * @param note - Note to add
     * @return true if it worked or false if something happened
     */
    public static boolean createNote(String username, String note) {
        if(connection == null)
            return false;

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
            Main.log(Level.SEVERE, "An error occurred when attempting to create a note!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Returns the full note matching the id provided and the username provided,
     * or null if none exists.
     *
     * @param id - ID of the note to look for
     * @param username - User requesting the note
     * @return Note String or null
     */
    public static String readNote (int id, String username) {
        if(connection == null)
            return null;

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ? AND username = ? LIMIT 1";
        String note = null;


        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, username);

            ResultSet results = stmt.executeQuery();

            while(results.next())
                note = results.getString("note");

        } catch (SQLException e) {
            Main.log(Level.SEVERE, "A SQL error occurred when attempting to read a note!");
            e.printStackTrace();
        }


        return note;
    }

    /**
     * Deletes the note matching the ID from the user provided.
     *
     * @param id - ID of note
     * @param username - User who wishes to delete that note
     * @return true if the note was deleted
     */
    public static boolean deleteNote(int id, String username) {
        if(connection == null)
            return false;

        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ? AND username = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, username);

            return stmt.executeUpdate() >= 1;
        } catch (SQLException e) {
            Main.log(Level.SEVERE, "An error occurred when attempting to delete a note!");
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Returns a map of notes given the proper username. Returns null if an error
     * occurred.
     *
     * @param username - User who requested a notes list
     * @return The list of notes
     */
    public static TreeMap<Integer, String> listNotes(String username){
        if(connection == null)
            return null;

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ?";

        TreeMap<Integer, String> notes = new TreeMap<Integer, String>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet set = stmt.executeQuery();

            while(set.next())
                notes.put(set.getInt("id"), set.getString("note"));


        } catch (SQLException e){
            Main.log(Level.SEVERE, "An SQL exception occurred when attempting to get the notes list!");
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
            Main.log(Level.SEVERE, "An exception was thrown when attempting to disconnect from the DB!");
            e.printStackTrace();
        }
    }

    /*
     * HELPERS
     */

    /**
     * Extracts the database engine from the config file provided
     *
     * @param configuration - FileConfiguration of the plugin
     * @return Which DatabaseEngine to use, or INVALID if the one provided is not supported
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
