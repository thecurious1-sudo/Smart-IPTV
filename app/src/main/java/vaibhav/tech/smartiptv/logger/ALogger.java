package vaibhav.tech.smartiptv.logger;

import android.os.Environment;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by Sergey on 06.04.2017.
 */

public class ALogger {
    public static org.apache.log4j.Logger getLogger(Class clazz) {
        String LogFile = Environment.getExternalStorageDirectory().toString() + File.separator + "oxycasttv/log/file.log";
        File file = new File(LogFile);
        if(file.exists() == false)
        {
            if(file.getParentFile().exists() == false)
            {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(LogFile);
        logConfigurator.setRootLevel(Level.ALL);
        logConfigurator.setLevel("org.apache", Level.ALL);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
        Logger log = Logger.getLogger(clazz);
        return log;
    }
}
