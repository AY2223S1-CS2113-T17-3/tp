package seedu.duke.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import seedu.duke.Timetable;
import seedu.duke.module.Module;
import seedu.duke.module.lessons.Lesson;
import seedu.duke.module.lessons.Lecture;
import seedu.duke.module.lessons.Tutorial;
import seedu.duke.module.lessons.Laboratory;
import seedu.duke.module.lessons.Others;

/*
 * ### Public Functions: ###
 * initModuleDataFile()
 * addModule(String code, String name, String description)
 * deleteModule(Module module)
 * loadModuleIntoTimetable()
 * 
 * ### Helper (private) Functions: ###
 * rewriteModuleData()
 * removeModuleFromDataList(Module module)
 */

public class ModuleManager {
    static ArrayList<String> moduleDataList = new ArrayList<String>();
    static String dataDirectoryPath;
    static String currSemester;

    /*
     * Creates ModuleData.txt if does not exist
     */
    public static void initModuleDataFile() {
        currSemester = DataManager.getCurrentSem();
        dataDirectoryPath = DataManager.getDirPath();

        try {
            File moduleDataFile = new File(dataDirectoryPath + "/ModuleData.txt");
            if (!moduleDataFile.exists()) {
                moduleDataFile.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating ModuleData.txt");
        }
    }

    /*
    * Adds a module into ModuleData.txt in the following format:
    * <code>|<name>|<description>\n
    */
    public static void addModule(String code, String name, String description) {
        try {
            FileWriter myWriter = new FileWriter(dataDirectoryPath + "/ModuleData.txt", true);
            String line = code + "|" + name + "|" + description;
            myWriter.write(line + "\n");
            moduleDataList.add(line + "\n");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Sorry, failed to add module to ModuleData.txt");
        }
    }

    /*
     * Removes all Modules for a given module from ModuleDataList
     */
    public static void removeModuleFromDataList(Module module) {
        String moduleCode = module.getModuleCode();
        String[] currLine;

        for (int i = 0; i < moduleDataList.size(); i++) {
            currLine = moduleDataList.get(i).split("\\|");
            if (currLine[0].equals(moduleCode)) {
                moduleDataList.remove(i);
            }
        }
    }

    /*
     * Erases data in moduleDataFile and writes data from moduleDataList into moduleDataFile
     */
    public static void rewriteModuleData() {
        try {
            // Erase Data
            FileWriter myWriter = new FileWriter(dataDirectoryPath + "/ModuleData.txt");
            myWriter.write("");
            myWriter.close();

            myWriter = new FileWriter(dataDirectoryPath + "/ModuleData.txt", true);
            for (String line : moduleDataList) {
                myWriter.write(line);
            }

            myWriter.close();
        } catch (IOException e) {
            System.out.println("Failed to erase ModuleData.txt");
        }
    }

    /*
     * Removes all instances of Modules from moduleDataList and moduleDataFile
     */
    public static void deleteModule(Module module) {
        removeModuleFromDataList(module);
        rewriteModuleData();
    }

    /*
     * Loads modules from moduleDataFile and lessonDataFile into Timetable and moduleDataList
     */
    public static void loadModuleIntoTimetable() {
        // Initiate readers
        FileReader moduleFileReader;
        BufferedReader moduleBufferedReader;

        // Get dataFile
        File moduleDataFile = new File(dataDirectoryPath + "/ModuleData.txt");

        // Variables for iterating through moduleDataList
        String currModuleLine;
        String[] moduleInfoList;

        // Variables to add lessons to module
        ArrayList<String> lessonDataList = LessonManager.getDataList();
        String[] lessonInfoList;

        try {
            moduleFileReader = new FileReader(moduleDataFile);
            moduleBufferedReader = new BufferedReader(moduleFileReader);
            
            List<Lesson> lessons = new ArrayList<Lesson>();

            //create and add module
            while ((currModuleLine = moduleBufferedReader.readLine()) != null) {
                lessons.clear();

                moduleDataList.add(currModuleLine + "\n");
                moduleInfoList = currModuleLine.split("\\|");

                String code = moduleInfoList[0];
                String name = moduleInfoList[1];
                String description = moduleInfoList[2];

                //load lessonData for currModule into module object
                for (String line : lessonDataList) {
                    lessonInfoList = line.split("\\|");

                    String lessonModuleCode = lessonInfoList[0];
                    String lessonType = lessonInfoList[1];
                    String lessonDay = lessonInfoList[2];
                    String lessonStart = lessonInfoList[3];
                    String lessonEnd = lessonInfoList[4];

                    if (lessonModuleCode.equals(code)) {
                        switch (lessonType) {
                        case "Lecture":
                            lessons.add(new Lecture(lessonDay, lessonStart, lessonEnd, lessonType));
                            break;
                        case "Tutorial":
                            lessons.add(new Tutorial(lessonDay, lessonStart, lessonEnd, lessonType));
                            break;
                        case "Laboratory":
                            lessons.add(new Laboratory(lessonDay, lessonStart, lessonEnd, lessonType));
                            break;
                        default:
                            lessons.add(new Others(lessonDay, lessonStart, lessonEnd, lessonType));
                            break;
                        }
                    }
                }
                Timetable.addNewModuleFromFile(code, name, description, lessons);
            }
            moduleFileReader.close();
            moduleBufferedReader.close();
        } catch (IOException e) {
            System.out.println("Failed to transfer data from ModuleData.txt to ModuleList");
        }
    }
}