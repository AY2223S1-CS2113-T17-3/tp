package seedu.duke.commands;

import seedu.duke.Timetable;
import seedu.duke.module.Module;
import seedu.duke.module.lessons.Lesson;
import seedu.duke.UI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Comparator;
import java.util.Stack;
import java.util.logging.Logger;



public class CommandPrintTimetableVertical {

    private static final String[] DAYS_IN_WEEK = {"MON", "TUE", "WED", "THU", "FRI"};
    private static final Integer FIRST_HOUR = 8; // timetable start at 0800
    private static final Integer COLUMN_WIDTH = 13;
    private static final Integer LEFT_PADDING = 3;
    private static final Integer TOP_PADDING = 1;
    private static final Integer COLUMN_PADDING = 1;
    private static final Integer ROW_DIFFERENCE = 3; // difference between api data and timetable here
    private static final Integer DAY_PER_WEEK = 5; // only considering Mon to Fri
    private static final Integer TIMETABLE_WIDTH = 78;
    private static final Integer TIMETABLE_TIME_WIDTH = 13;
    private static final Integer TIMETABLE_HEIGHT = 28;
    private static final Integer END_SLOT_DIFFERENCE = 1;
    private static final Logger lgr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static String[][] timeTable;
    private static ArrayList<ArrayList<Object[]>> rawTimetable = new ArrayList<>(5);
    // rawTimetable stores the lesson objects for data handling 
    private static ArrayList<ArrayList<Integer[]>> emptySlotList = new ArrayList<>(DAY_PER_WEEK);
    // array of 5 arraylist of integer pairs


    public static String viewTimetable() {
        populateRawTimetable(Timetable.getListOfModules());
        setTable();
        initializeTable();
        writeTableHeader();
        writeTable();

        return printTimetable(timeTable);
    }


    private static void initializeRawTimeTable() {
        for (int i = 0; i < DAY_PER_WEEK; i++) {
            rawTimetable.add(new ArrayList<>());
            emptySlotList.add(new ArrayList<>());
        }
    }


    private static void populateRawTimetable(List<Module> listOfModules) {
        rawTimetable = new ArrayList<>(DAY_PER_WEEK);
        emptySlotList = new ArrayList<>(DAY_PER_WEEK);

        initializeRawTimeTable();

        for (Module module : listOfModules) {
            List<Lesson> lessons = module.getAttendingList();
            try {
                String code = module.getModuleCode();
                writeRawTimetable(lessons, code);
                lgr.fine("Timetable initialized successfully. ");
            } catch (IllegalArgumentException e) {
                UI.printResponse("File may be corrupted, please delete the file and re-try. ");
                lgr.info("Timetable file corrupted. ");
            }
        }
    }


    private static void writeRawTimetable(List<Lesson> lessons, String code) {
        for (Lesson les : lessons) {
            int[] info = convertTimeToIndex(les.getDay(), les.getStartTime(), les.getEndTime());
            if (info[0] == -1) {
                continue;
            }
            populateDailyRawTimetable(code, les, info);
        }
    }


    private static void populateDailyRawTimetable(String code, Lesson les, int[] info) {
        Object[] rawLesson = new Object[4];
        rawLesson[0] = info[1]; // starting slot
        rawLesson[1] = info[2] + END_SLOT_DIFFERENCE; 
        // api data and timetable data here has a difference in lesson ending time
        rawLesson[2] = code; // lesson code
        String type = les.getLessonType();
        rawLesson[3] = type; // lesson type

        Integer[] rawLessonSlot = new Integer[2];
        rawLessonSlot[0] = info[1];
        rawLessonSlot[1] = info[2] + END_SLOT_DIFFERENCE;

        try {
            rawTimetable.get(info[0]).add(rawLesson); // add each rawLesson into respective day
            emptySlotList.get(info[0]).add(rawLessonSlot);
        } catch (NullPointerException e) {
            UI.printResponse("rawTimetable / emptySlotList not initialized! ");
        }

    }


    private static int[] convertTimeToIndex(String day, String startTime, String endTime) {
        int[] info = new int[3];

        if (isInvalidTimings(startTime, endTime)) {
            info[0] = -1;
            return info;
        }

        info[0] = determineDay(day);
        info[1] = parseTimeToIndex(startTime);
        info[2] = parseTimeToIndex(endTime) - 1;

        return info;
    }


    private static int parseTimeToIndex(String time) {
        String newTime = time.replaceFirst("^0+(?!$)", "");
        return (Integer.parseInt(newTime) - 770) / 50;
    }


    private static boolean isInvalidTimings(String startTime, String endTime) {
        return (Objects.equals(startTime, "Undetermined") || Objects.equals(endTime, "Undetermined")
                || Integer.parseInt(startTime) > 2000 || Integer.parseInt(startTime) < 800
                || Integer.parseInt(endTime) > 2000 || Integer.parseInt(endTime) < 800);
    }


    private static int determineDay(String day) {
        switch (day) {
        case "Monday":
            return 0;
        case "Tuesday":
            return 1;
        case "Wednesday":
            return 2;
        case "Thursday":
            return 3;
        case "Friday":
            return 4;
        default:
            return -1;
        }
    }


    private static void sortDailySlots(Integer day, ArrayList<ArrayList<Integer[]>> emptySlotList) {
        emptySlotList.get(day).sort(Comparator.comparingInt(i -> i[0]));
    }


    private static void setTable() {
        timeTable = new String[TIMETABLE_HEIGHT][TIMETABLE_WIDTH];
    }


    private static void initializeTable() {
        for (int i = 0; i < TIMETABLE_HEIGHT; i++) {
            for (int j = 0; j < TIMETABLE_WIDTH; j++) {
                timeTable[i][j] = " ";
            }
        }
    }


    private static void writeTableHeader() {
        // draw border below days
        for (int i = 0; i < TIMETABLE_WIDTH; i++) {
            timeTable[2 * TOP_PADDING][i] = UI.TABLE_HEADER;
        }
        drawDaysHeader();
        drawTopSeparator();
        drawColumnSeparator();
    }

    private static void drawDaysHeader() {
        for (int i = LEFT_PADDING; i < TIMETABLE_HEIGHT; i++) {
            write(indexToTime(i), i, LEFT_PADDING);
        }
    }

    private static void drawTopSeparator() {
        for (int i = 0; i < DAY_PER_WEEK; i++) {
            write(UI.DOTTED_CHAR + " " + DAYS_IN_WEEK[i], TOP_PADDING,
                    getDayColumnIndex(i) - COLUMN_PADDING);
        }
    }

    private static void drawColumnSeparator() {
        for (int i = 0; i < TIMETABLE_HEIGHT; i++) {
            for (int j = 0; j < DAY_PER_WEEK; j++) {
                write("" + UI.DOTTED_CHAR, i, getDayColumnIndex(j) - COLUMN_PADDING);
            }
        }
    }


    private static void write(String text, int row, int column) {
        timeTable[row][column] = "" + text.charAt(0);
        for (int i = 1; i < text.length(); i++) {
            timeTable[row][column + i] = "" + text.charAt(i);
        }
    }


    private static String indexToTime(int index) {
        boolean isHalf = index % 2 == 0; // even index in timetable is half hours
        int hours = (index - ROW_DIFFERENCE) / 2 + FIRST_HOUR; 
        return String.format("%02d", hours) + (isHalf ? "30" : "00");
    }


    private static int getDayColumnIndex(int day) { // day 0 is Monday
        int columnIndex = 0;
        for (int i = 0; i < day; i++) {
            columnIndex += COLUMN_WIDTH;
        }

        return columnIndex + TIMETABLE_TIME_WIDTH; 
    }


    private static void writeTable() {
        for (int i = 0; i < rawTimetable.size(); i++) {
            if (!rawTimetable.get(i).isEmpty()) {
                sortDailySlots(i,emptySlotList);
                ArrayList<Object[]> dayIterator = rawTimetable.get(i);
                // proceed with non-empty days to process
                initializeClashSlotList(i);
                createTimetableString(i, dayIterator);
            }
        }

    }


    private static void createTimetableString(int day, ArrayList<Object[]> dayIterator) {
        for (Object[] rawLesson : dayIterator) {
            int modStartSlot = (Integer) rawLesson[0];
            int modEndSlot = (Integer) rawLesson[1];
            assert modStartSlot < modEndSlot;
            String currentModCode = (String) rawLesson[2];
            String currentModType = (String) rawLesson[3];
            StringBuilder upperBoarder = new StringBuilder();
            StringBuilder lowerBoarder = new StringBuilder();

            buildLowBoarder(modStartSlot, modEndSlot, currentModType, lowerBoarder);
            upperBoarder.append(UI.HORIZONTAL_BORDER.repeat(COLUMN_WIDTH - 1));

            Integer columnIndex = getDayColumnIndex(day);
            Integer thisSlotRowIndex = modStartSlot + ROW_DIFFERENCE;
            //boolean thisSlotWritten = checkSlotWritten(thisSlotRowIndex, columnIndex);
            boolean nextSlotWritten = checkSlotWritten(thisSlotRowIndex + 1, columnIndex);

            if (!nextSlotWritten) {
                // if no clash is indicated in timetable, write the timetable
                writeLesson(modStartSlot, currentModCode, currentModType, columnIndex);
                writeBoarder(modStartSlot, modEndSlot, upperBoarder, lowerBoarder, columnIndex);
            }
        }
    }

    private static void writeLesson(int start, String code, String type, Integer col) {
        try {
            Integer codeRowIndex = start + ROW_DIFFERENCE + 1;
            write(code, codeRowIndex, col);
            String lessonType = type.substring(0, 3).toUpperCase();
            // type is printed with first three letters
            Integer typeRowIndex = start + ROW_DIFFERENCE + 2;
            write(lessonType, typeRowIndex, col + 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            UI.printResponse("index out of bounds when writing lesson");
        }
    }

    private static void writeBoarder(int start, int end, StringBuilder upLine, StringBuilder lowLine, Integer col) {
        Integer endRowIndex = end + ROW_DIFFERENCE;
        try {
            write(lowLine.toString(), endRowIndex, col);
            writeTopBoarder(start, upLine, col);
        } catch (ArrayIndexOutOfBoundsException e) {
            UI.printResponse("index out of bounds when writing boarder");
        }

    }


    private static void writeTopBoarder(int startSlot, StringBuilder upBoarder, Integer columnIndex) {
        Integer rowIndex = startSlot + ROW_DIFFERENCE;
        String stringToCheck = timeTable[rowIndex][columnIndex + 1];
        boolean isOccupied = stringToCheck.equals(UI.HORIZONTAL_BORDER);
        if (!isOccupied) {
            try {
                write(upBoarder.toString(), startSlot + 3, columnIndex);
            } catch (ArrayIndexOutOfBoundsException e) {
                UI.printResponse("index out of bounds when writing top boarder");
            }
        }
    }


    private static void buildLowBoarder(int startSlot, int endSlot, String modType, StringBuilder lowBoarder) {
        assert endSlot > startSlot : "End slot index is smaller than start index!";
        if (endSlot - startSlot < 3) {
            // lower boarder joins lesson type, height not enough
            buildNarrowLowBoarder(modType, lowBoarder);
        } else {
            String stringToWrite = UI.HORIZONTAL_BORDER.repeat(COLUMN_WIDTH - 1);
            lowBoarder.append(stringToWrite);
        }
    }


    private static void buildNarrowLowBoarder(String currentModType, StringBuilder lowerBoarder) {
        lowerBoarder.append(UI.HORIZONTAL_BORDER);
        String lessonType = currentModType.substring(0,3).toUpperCase();
        lowerBoarder.append(lessonType);
        int currentLength = lowerBoarder.length();
        String stringToWrite = UI.HORIZONTAL_BORDER.repeat(COLUMN_WIDTH - 1 - currentLength);
        lowerBoarder.append(stringToWrite);
    }


    private static void initializeClashSlotList(int day) {
        boolean dailyClashFlag = checkDailySlotClash(day);
        if (dailyClashFlag) {
            // if clash exists, pre-write timetable with "X"
            ArrayList<Integer[]> clashSlotList = getDailyClashSlot(day);

            for (Integer[] slot : clashSlotList) {
                Integer clashStartIndex = slot[0];
                Integer clashEndIndex = slot[1];
                try {
                    writeClashSlot(day, clashStartIndex, clashEndIndex);
                } catch (ArrayIndexOutOfBoundsException e) {
                    UI.printResponse("index out of bounds when writing clashed lessons ");
                }


            }
        }
    }

    private static void writeClashSlot(int day, Integer clashStartIndex, Integer clashEndIndex) {
        for (int l = clashStartIndex; l < clashEndIndex + 1; l++) {
            // write the end with X for now
            String stringToWrite = "X".repeat(COLUMN_WIDTH - 1);
            write(stringToWrite,l + ROW_DIFFERENCE,getDayColumnIndex(day));
        }
    }


    private static String printTimetable(String[][] timeTable) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < TIMETABLE_HEIGHT; i++) {
            for (int j = 0; j < TIMETABLE_WIDTH; j++) {
                output.append(timeTable[i][j]);
            }
            output.append(System.lineSeparator());
        }
        addRemarks(output);
        return output.toString();
    }


    private static boolean checkDailySlotClash(Integer day) {
        if (emptySlotList.size() > 0) {
            sortDailySlots(day, emptySlotList);
        }

        for (int i = 1; i < emptySlotList.get(day).size(); i++) {
            if ((emptySlotList.get(day).get(i - 1)[1]) > emptySlotList.get(day).get(i)[0]) {
                return true; 
            }
        }
        return false;

    }


    private static ArrayList<Integer[]> getDailyClashSlot(Integer day) {
        if (emptySlotList.size() > 0) {
            sortDailySlots(day, emptySlotList);
        }

        Stack<Integer[]> stack = new Stack<>();
        stack.push(emptySlotList.get(day).get(0));

        sortSlotList(day, stack);
        ArrayList<Integer[]> clashSlotList = createClashList(stack);

        removeUnclashSlot(day, clashSlotList);

        return clashSlotList;

    }

    private static ArrayList<Integer[]> createClashList(Stack<Integer[]> stack) {
        ArrayList<Integer[]> clashSlotList = new ArrayList<>();
        while (!stack.isEmpty()) {
            clashSlotList.add(stack.pop());
        }
        return clashSlotList;
    }

    private static void removeUnclashSlot(Integer day, ArrayList<Integer[]> clashSlotList) {
        for (Integer[] originalSlot : emptySlotList.get(day)) {
            if (!clashSlotList.contains(originalSlot)) { // slot not found
                clashSlotList.remove(originalSlot);
            }
        }
    }


    private static void sortSlotList(Integer day, Stack<Integer[]> stack) {
        Integer[] top = stack.peek();
        for (int i = 1; i < emptySlotList.get(day).size(); i++) {
            if (top[1] < emptySlotList.get(day).get(i)[0]) { //[1] is pair.second, [0] is pair.first
                stack.pop();
                stack.push(emptySlotList.get(day).get(i));
            } else if (top[1] == emptySlotList.get(day).get(i)[0]) {
                continue;
            } else if (top[1] < emptySlotList.get(day).get(i)[1]) {
                top[1] = emptySlotList.get(day).get(i)[1];
                stack.pop();
                stack.push(top);
            }
        }

    }


    private static boolean checkSlotWritten(Integer row, Integer column) {
        return timeTable[row][column].equals("X");
    }


    private static String addRemarks(StringBuilder timetable) {
        timetable.append("\n * Note that timings indicated refers to the start of "
                + "the corresponding 30 minutes timeslot.\n"
                + " * Slots with XXXXXX indicates that there is a clash between two or more lessons.\n"
                + " * Modules, if any, that start or end beyond the 8am to 8pm timings are excluded.\n"
                + " * Timings are approximated to 30 minutes block with valid assumption that "
                + "NUS mods are typically designed in such blocks.\n");
        return timetable.toString();
    }


}


