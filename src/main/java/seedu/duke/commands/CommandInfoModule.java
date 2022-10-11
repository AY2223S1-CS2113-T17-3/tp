package seedu.duke.commands;

import seedu.duke.Timetable;
import seedu.duke.commands.nusmodsapi.Nusmods;
import seedu.duke.module.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CommandInfoModule {

    public static String findModule(Timetable timetable) {
        Nusmods mods = new Nusmods();
        String[] found = new String[3];
        System.out.println("Please the module code you wish to get information about");
        try {
            found = mods.getModuleInfo();
        } catch (IOException e) {
            System.out.println("Some IO errors has occurred, please try again");
        } catch (InterruptedException e) {
            System.out.println("The API request was interrupted, please try again.");
        }
        return formatFoundModules(found);
    }

    private static String formatFoundModules(String[] found) {
        StringBuilder message = new StringBuilder();
        message.append("Here are some information about the module:\n");
        message.append("Module Code: " + found[0] + "\n");
        message.append("Module Name: " + found[1] + "\n");
        message.append("Module Description: " + found[2] + "\n");
        return message.toString();
    }
}
