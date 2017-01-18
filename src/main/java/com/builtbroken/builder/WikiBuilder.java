package com.builtbroken.builder;

import com.builtbroken.builder.html.PageBuilder;
import com.builtbroken.builder.html.data.ImageData;
import com.builtbroken.builder.html.data.LinkData;
import com.builtbroken.builder.html.parts.HTMLPartHeader;
import com.builtbroken.builder.html.parts.HTMLPartParagraph;
import com.builtbroken.builder.html.parts.JsonProcessorHTML;
import com.builtbroken.builder.html.theme.PageTheme;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

        //TODO implement GUI
        //Load arguments
        HashMap<String, String> launchSettings = loadArgs(args);

        if (launchSettings.containsKey("batchFile"))
        {
            File batchFile = Utils.getFile(new File("."), launchSettings.get("batchFile"));
            if (batchFile.exists() && batchFile.isFile())
            {
                JsonElement element = Utils.toJsonElement(batchFile);
                if (element.isJsonObject())
                {
                    JsonObject object = element.getAsJsonObject();
                    if (object.has("jobs"))
                    {
                        ImageData imageData = new ImageData();
                        LinkData linkData = new LinkData();
                        List<PageBuilder> builders = new ArrayList();

                        PageTheme pageTheme = null;
                        if (launchSettings.containsKey("theme"))
                        {
                            File file = Utils.getFile(batchFile.getParentFile(), launchSettings.get("theme"));
                            if (file.isDirectory())
                            {
                                file = new File(file, "theme.json");
                            }
                            pageTheme = new PageTheme(file);
                            logger.info("Theme is being set by program arguments! Theme in settings file will not be used.");
                            logger.info("Theme : " + pageTheme.themeFile);
                        }

                        Set<Map.Entry<String, JsonElement>> entrySet = object.entrySet();

                        if (!entrySet.isEmpty())
                        {
                            for (Map.Entry<String, JsonElement> entry : entrySet)
                            {
                                if (element.isJsonObject())
                                {
                                    object = element.getAsJsonObject();
                                    HashMap<String, String> settings = new HashMap();
                                    //Limit settings as some are not valid for batch
                                    if (launchSettings.containsKey("outputDirectory"))
                                    {
                                        settings.put("outputDirectory", launchSettings.get("outputDirectory"));
                                    }

                                    File workingDirectory;
                                    File settingsFile;

                                    if (object.has("directory"))
                                    {
                                        workingDirectory = Utils.getFile(batchFile.getParentFile(), object.getAsJsonPrimitive("directory").getAsString());
                                    }
                                    else
                                    {
                                        throw new RuntimeException("Batch job '" + entry.getKey() + "' is missing the directory tag");
                                    }
                                    if (object.has("settingsFile"))
                                    {
                                        settingsFile = Utils.getFile(batchFile.getParentFile(), object.getAsJsonPrimitive("settingsFile").getAsString());
                                    }
                                    else
                                    {
                                        settingsFile = Utils.getFile(batchFile.getParentFile(), "./settings.json");
                                    }

                                    builders.add(new PageBuilder(logger, workingDirectory, settingsFile, settings, pageTheme, imageData, linkData));
                                }
                                else
                                {
                                    throw new RuntimeException("Batch job '" + entry.getKey() + "' format is invalid and should be a json object");
                                }
                            }

                            if (launchSettings.containsKey("linkData"))
                            {
                                //TODO load and parse
                            }
                            if (launchSettings.containsKey("imageData"))
                            {
                                //TODO load and parse
                            }

                            //Run each wiki build one phase at a time to allow data to be shared correctly
                            builders.forEach(PageBuilder::parseSettings);

                            //If changed update page build html load process
                            logger.info("\tLoading theme");
                            pageTheme.load();
                            pageTheme.loadTemplates();
                            logger.info("\tDone");

                            logger.info("\tLoading HTML processors");
                            JsonProcessorHTML.registerPart("h", new HTMLPartHeader());
                            JsonProcessorHTML.registerPart("p", new HTMLPartParagraph());
                            logger.info("\tDone");

                            builders.forEach(PageBuilder::parseSettings);
                            builders.forEach(PageBuilder::loadWikiData);
                            builders.forEach(PageBuilder::parseWikiData);
                            builders.forEach(PageBuilder::buildWikiData);
                            builders.forEach(PageBuilder::buildPages);
                        }
                        else
                        {
                            throw new RuntimeException("Batch file's job list is empty");
                        }
                    }
                    else
                    {
                        throw new RuntimeException("Batch file does not contain the 'jobs' tag");
                    }
                }
                else
                {
                    throw new RuntimeException("Batch file is not a valid json object");
                }
            }
            else
            {
                throw new RuntimeException("Batch file is missing or invalid.");
            }
        }
        else
        {
            //Vars
            File workingDirector = null;
            File settingsFile = null;

            //Get our working folder
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

            //Get our settings file
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

            //Start process
            PageTheme pageTheme = null;
            if (launchSettings.containsKey("theme"))
            {
                File file = Utils.getFile(workingDirector, launchSettings.get("theme"));
                if (file.isDirectory())
                {
                    file = new File(file, "theme.json");
                }
                pageTheme = new PageTheme(file);
                logger.info("Theme is being set by program arguments! Theme in settings file will not be used.");
                logger.info("Theme : " + pageTheme.themeFile);
            }
            PageBuilder builder = new PageBuilder(logger, workingDirector, settingsFile, launchSettings, pageTheme, new ImageData(), new LinkData());
        }

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
