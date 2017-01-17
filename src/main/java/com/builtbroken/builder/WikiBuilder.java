package com.builtbroken.builder;

import com.builtbroken.builder.html.PageBuilder;
import com.builtbroken.builder.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Main Class for the program
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class WikiBuilder
{
    public static final String SETTINGS_FILE_NAME = "settings.json";

    public static void main(String... args)
    {
        Logger logger = LogManager.getRootLogger();

        logger.info("Wiki-Builder has been started...");
        logger.info("Parsing arguments...");

        //Load arguments
        HashMap<String, String> launchSettings = loadArgs(args);

        //Vars
        File workingDirector = null;
        File settingsFile = null;

        //Use settings
        if (launchSettings.containsKey("workingFolder"))
        {
            workingDirector = Utils.getFile(new File("."), launchSettings.get("workingFolder"));
        }
        if (workingDirector == null)
        {
            workingDirector = new File(".");
        }
        if (!workingDirector.exists())
        {
            workingDirector.mkdirs();
        }

        if (launchSettings.containsKey("settingsFile"))
        {
            settingsFile = Utils.getFile(workingDirector, launchSettings.get("settingsFile"));
        }

        if (settingsFile == null)
        {
            settingsFile = new File(workingDirector, SETTINGS_FILE_NAME);
        }
        if (!settingsFile.getParentFile().exists())
        {
            settingsFile.getParentFile().mkdirs();
        }
        if (!settingsFile.exists())
        {
            throw new RuntimeException("Settings file does not exist at location: " + settingsFile);
        }

        //Output settings
        logger.info("Working folder :" + workingDirector);
        logger.info("Settings file  :" + settingsFile);

        logger.info("Creating page builder....");
        PageBuilder builder = new PageBuilder(logger, workingDirector, settingsFile, launchSettings);


        logger.info("Parsing settings....");
        builder.parseSettings();
        logger.info("Done....\n\n");

        logger.info("Loading HTML templates....");
        builder.loadHTML();
        logger.info("Done....\n\n");

        logger.info("Loading wiki data....");
        builder.loadWikiData();
        logger.info("Done....\n\n");

        logger.info("Parsing wiki data....");
        builder.parseWikiData();
        logger.info("Done....\n\n");

        logger.info("Building wiki data....");
        builder.buildWikiData();
        logger.info("Done....\n\n");

        logger.info("Building pages....");
        builder.buildPages();
        logger.info("Done....\n\n");

        //End of program pause
        if (!launchSettings.containsKey("noConfirm"))
        {
            logger.info("Press 'any' key to continue...");
            try
            {
                System.in.read();
            }
            catch (IOException e)
            {
            }
        }
        System.exit(0);
    }

    /**
     * Converts arguments into a hashmap for usage
     *
     * @param args
     * @return
     */
    public static HashMap<String, String> loadArgs(String... args)
    {
        final HashMap<String, String> map = new HashMap();
        if (args != null)
        {
            String currentArg = null;
            String currentValue = "";
            for (int i = 0; i < args.length; i++)
            {
                String next = args[i].trim();
                if (next == null)
                {
                    throw new IllegalArgumentException("Null argument detected in launch arguments");
                }
                else if (next.startsWith("-"))
                {
                    if (currentArg != null)
                    {
                        map.put(currentArg, currentValue);
                        currentValue = "";
                    }

                    if (next.contains("="))
                    {
                        String[] split = next.split("=");
                        currentArg = split[0].substring(1).trim();
                        currentValue = split[1].trim();
                    }
                    else
                    {
                        currentArg = next.substring(1).trim();
                    }
                }
                else if (currentArg != null)
                {
                    if (!currentValue.isEmpty())
                    {
                        currentValue += ",";
                    }
                    currentValue += next.replace("\"", "").replace("'", "").trim();
                }
                else
                {
                    throw new IllegalArgumentException("Value has no argument associated with it [" + next + "]");
                }
            }
            //Add the last loaded value to the map
            if (currentArg != null)
            {
                map.put(currentArg, currentValue);
            }
        }
        return map;
    }
}
