package org.cincospenguinos.note_taker;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

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
                    return create(strings);
                case LIST:
                    return list();
                case DELETE:
                    return delete(strings);
                case READ:
                    return read(strings);
            }
        } else
            commandSender.sendMessage("This command is only permitted for players, not server admins.");

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

    private boolean create(String[] command){
        // TODO: this
        return true;
    }

    private boolean list(){
        // TODO: This
        return true;
    }

    private boolean delete(String[] command){
        // TODO: This
        return true;
    }

    private boolean read(String[] command){
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
