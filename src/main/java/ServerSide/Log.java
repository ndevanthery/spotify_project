package ServerSide;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zurich"));
    private Logger logger = Logger.getLogger("log");
    private FileHandler fh;
    private String monthstr = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    private int year = calendar.get(Calendar.YEAR);
    private String extension = "[" + year + "_" + monthstr + "]" + ".log";
    private String filename = "logServer" + extension;
    private File file = new File(filename);
    private SimpleFormatter formatter = new SimpleFormatter();

    public Log() {


    }

    public void info(String string) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileHandler fh = null;
        try {
            fh = new FileHandler(filename, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(fh);

        fh.setFormatter(formatter);
        logger.info(string);
        fh.close();
    }

    public void warning(String string) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileHandler fh = null;
        try {
            fh = new FileHandler(filename, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(fh);

        fh.setFormatter(formatter);
        logger.warning(string);
        fh.close();

    }

    public void severe(String string)  {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileHandler fh = null;
        try {
            fh = new FileHandler(filename, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(fh);

        fh.setFormatter(formatter);
        logger.severe(string);
        fh.close();


    }
}
