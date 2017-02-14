package org.cincospenguinos.note_taker;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Interface for the database.
 *
 * TODO: DB Upgrades?
 */
public class DBInterface {

    // Name of the DB table
    private static final String TABLE_NAME = "NoteTakerNotes";

    // Connection to the DB
    private static Connection connection;

    /**
     * Returns a connection to the database, or null if an issue occurred.
     *
     * @return Connection or null
     */
    public static Connection getConnection(File dataFolder){
        if (connection == null) {
            String url = "jdbc:sqlite:" + dataFolder.toString() + "note_taker.db";

            try {
                connection = DriverManager.getConnection(url);
            } catch (SQLException e) {
                Main.log(Level.SEVERE, "Cannot connect to database!");
                e.printStackTrace();
                return null;
            }

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

        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR(50) NOT NULL," +
                "note TEXT NOT NULL)";

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

        String sql = "INSERT INTO " + TABLE_NAME + "(username, note) VALUES (?, ?)";

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
}
