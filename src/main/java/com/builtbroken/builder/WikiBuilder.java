package com.builtbroken.builder;

import com.builtbroken.builder.html.PageBuilder;
import com.builtbroken.builder.utils.Utils;

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
        System.out.println("Wiki-Builder has been started...");
        System.out.println("Parsing arguments...");

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
        System.out.println("Working folder :" + workingDirector);
        System.out.println("Settings file  :" + settingsFile);

        System.out.println("Creating page builder....");
        PageBuilder builder = new PageBuilder(workingDirector, settingsFile, launchSettings);

        System.out.println("Parsing settings....");
        builder.parseSettings();

        System.out.println("Loading HTML templates....");
        builder.loadHTML();

        System.out.println("Loading wiki data....");
        builder.loadWikiData();

        System.out.println("Parsing wiki data....");
        builder.parseWikiData();

        System.out.println("Building wiki data....");
        builder.buildWikiData();

        System.out.println("Building pages....");
        builder.buildPages();

        //End of program pause
        if (!launchSettings.containsKey("noConfirm"))
        {
            System.out.println("Press 'any' key to continue...");
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
                String next = args[i];
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
                    currentArg = next.replaceFirst("-", "").trim();
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
        }
        return map;
    }
}
