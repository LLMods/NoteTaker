package org.cincospenguinos.note_taker;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

/**
 * NoteCommand - manages notes for each player
 */
public class NoteCommand implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 0 || !verifyCommand(strings))
            return false;

        // TODO: Figure out what command is requested

        for(String str : strings){
            Logger.getLogger("minecraft").info("[NoteCommand] " + str);
        }

        return true;
    }

    private boolean verifyCommand(String[] commandStrings) {
        String cmd = commandStrings[0];

        if(cmd.equalsIgnoreCase("list"))
            return true;
        else if(cmd.equalsIgnoreCase("delete")){
            return commandStrings.length == 2 && isValidInteger(commandStrings[1]);
        } else if(cmd.equalsIgnoreCase("create")){

        } else if(cmd.equalsIgnoreCase("read")){

        }

        return true;
    }

    private boolean create(){
        // TODO: this
        return true;
    }

    private boolean list(){
        // TODO: This
        return true;
    }

    private boolean delete(){
        // TODO: This
        return true;
    }

    private boolean isValidInteger(String s){
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }
}
