package org.cincospenguinos.note_taker;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;

/**
 * NoteCommand - manages notes for each player
 */
public class NoteCommand implements CommandExecutor {

    private enum NoteCommandType {
        LIST, DELETE, CREATE, READ, INVALID
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player){
            NoteCommandType t = getCommandType(strings);

            if(t == NoteCommandType.INVALID)
                return false;

            switch(t){
                case CREATE:
                    return create((Player) commandSender, strings);
                case LIST:
                    return list((Player) commandSender);
                case DELETE:
                    return delete((Player) commandSender, strings);
                case READ:
                    return read((Player) commandSender, strings);
            }

        } else
            commandSender.sendMessage(ChatColor.RED + "This command is only permitted for players, not server admins.");

        return true;
    }

    /**
     * Returns which type of command the user is requesting.
     *
     * @param commandStrings - the command strings that came in from the user
     * @return Some command type
     */
    private NoteCommandType getCommandType(String[] commandStrings){
        if(commandStrings.length == 0)
            return NoteCommandType.INVALID;

        String cmd = commandStrings[0];

        if(cmd.equalsIgnoreCase("list"))
            return NoteCommandType.LIST;
        else if(cmd.equalsIgnoreCase("delete") && commandStrings.length == 2 && isValidInteger(commandStrings[1]))
            return NoteCommandType.DELETE;
        else if(cmd.equalsIgnoreCase("create") && commandStrings.length > 1)
            return NoteCommandType.CREATE;
        else if(cmd.equalsIgnoreCase("read") && commandStrings.length == 2 && isValidInteger(commandStrings[1]))
            return NoteCommandType.READ;

        return NoteCommandType.INVALID;
    }

    /**
     * Creates a note for the player provided.
     *
     * @param sender - User who wants to create the note
     * @param command - The command that was provided
     * @return true, always
     */
    private boolean create(Player sender, String[] command){
        StringBuilder builder = new StringBuilder();

        for(int i = 1; i < command.length; i++) {
            builder.append(command[i]);
            builder.append(" ");
        }

        if(!DBInterface.createNote(sender.getDisplayName(), builder.toString().trim()))
            sender.sendMessage(ChatColor.RED + "An error occurred with the database. Please inform your system administrator.");
        else
            sender.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "Note created");

        return true;
    }

    /**
     * Prints out a list of notes to the Player.
     *
     * @param sender - User who wants to see his/her notes
     * @return true, always
     */
    private boolean list(Player sender){
        TreeMap<Integer, String> notes = DBInterface.listNotes(sender.getDisplayName());

        if(notes == null){
            sender.sendMessage(ChatColor.RED + "An error occured with the database. Please inform your system administrator.");
            return true;
        }

        if(notes.size() == 0) {
            sender.sendMessage(ChatColor.RED + "There are no notes to show!");
            return true;
        }

//        sender.sendMessage(ChatColor.GREEN + "ID - FIRST FEW WORDS");
        for(Map.Entry<Integer, String> e : notes.entrySet()){
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "" + e.getKey() + "" + ChatColor.RESET
                    + " - " + e.getValue().substring(0, Math.min(e.getValue().length(), 20)));
        }

        return true;
    }

    /**
     * Deletes the note the sender requests, if it exists.
     *
     * @param sender of the command
     * @param command - the command info
     * @return true, always
     */
    private boolean delete(Player sender, String[] command){
        int id = Integer.parseInt(command[1]);

        if(DBInterface.deleteNote(id, sender.getDisplayName()))
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + "Note deleted");
        else
            sender.sendMessage(ChatColor.RED + "There is no note that you own with that ID!");

        return true;
    }

    /**
     * Gives the note text to the user who requests it, if it exists.
     *
     * @param command that was sent
     * @param sender of the command
     * @return true, always
     */
    private boolean read(Player sender, String[] command){
        int id = Integer.parseInt(command[1]);
        String note = DBInterface.readNote(id, sender.getDisplayName());

        if(note != null)
            sender.sendMessage(note);
        else
            sender.sendMessage(ChatColor.RED + "There is no note that you own with that ID!");

        return true;
    }

    /**
     * Checks to see if the String passed is a valid integer.
     * @param s - string to check
     * @return true if it's a valid string
     */
    private boolean isValidInteger(String s){
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }
}
