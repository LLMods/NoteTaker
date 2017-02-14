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
                    return delete(strings);
                case READ:
                    return read(strings, (Player) commandSender);
            }

        } else
            commandSender.sendMessage("This command is only permitted for players, not server admins." + ChatColor.RED);

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

        for(int i = 1; i < command.length; i++)
            builder.append(command[i]);

        if(!DBInterface.createNote(sender.getDisplayName(), builder.toString()))
            sender.sendMessage("An error occurred with the database. Please inform your system administrator." + ChatColor.RED);

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
            sender.sendMessage("An error occured with the database. Please inform your system administrator." + ChatColor.RED);
            return true;
        }

        if(notes.size() == 0)
            sender.sendMessage("There are no notes to show!" + ChatColor.RED);

        for(Map.Entry<Integer, String> e : notes.entrySet()){
            sender.sendMessage(e.getKey() + " - " + e.getValue().substring(0, Math.min(e.getValue().length(), 15)));
        }

        return true;
    }

    private boolean delete(String[] command){
        // TODO: This
        return true;
    }

    private boolean read(String[] command, Player sender){
        // TODO: This
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
