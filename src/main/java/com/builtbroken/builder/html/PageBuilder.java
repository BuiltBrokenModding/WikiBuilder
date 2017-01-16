package com.builtbroken.builder.html;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Main logic class for building all pages
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageBuilder
{
    /** Main directory to read and write files inside */
    public final File workingDirectory;
    /** */
    public final File settingsFile;

    /** Location to access images */
    public File imageDirectory;
    /** location to access content pages */
    public File contentDirectory;
    /** Location to get cateogry data from */
    public File categoryFile;

    /** Extra variables loaded from settings to be used later */
    public HashMap<String, String> vars;

    public HashMap<String, PageTemplate> templates;

    /**
     * Creates a new page builder instance
     *
     * @param workingDirectory - directory to place all files inside
     * @param settingsFile     - location of the primiary settings file
     * @param launchSettings   - launch arguments, overrides settings file in some cases and changes logic
     */
    public PageBuilder(File workingDirectory, File settingsFile, HashMap<String, String> launchSettings)
    {
        this.workingDirectory = workingDirectory;
        this.settingsFile = settingsFile;
    }

    public void parseSettings()
    {
        if (settingsFile.exists() && settingsFile.isFile())
        {
            JsonElement element = readElement(settingsFile);

            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("images"))
                {
                    String value = object.getAsJsonPrimitive("images").getAsString();
                    if (value.startsWith("."))
                    {
                        imageDirectory = new File(workingDirectory, value.replace("." + File.separator, ""));
                    }
                    else
                    {
                        imageDirectory = new File(value);
                    }
                }

                if (object.has("content"))
                {
                    String value = object.getAsJsonPrimitive("content").getAsString();
                    if (value.startsWith("."))
                    {
                        contentDirectory = new File(workingDirectory, value.replace("." + File.separator, ""));
                    }
                    else
                    {
                        contentDirectory = new File(value);
                    }
                }

                if (object.has("categories"))
                {
                    String value = object.getAsJsonPrimitive("categories").getAsString();
                    if (value.startsWith("."))
                    {
                        categoryFile = new File(workingDirectory, value.replace("." + File.separator, ""));
                    }
                    else
                    {
                        categoryFile = new File(value);
                    }
                }
                if (object.has("vars"))
                {
                    Gson gson = new Gson();
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("vars"), map.getClass());
                    vars.putAll(map);
                }
                if (object.has("template"))
                {

                }
                //TODO print loaded settings
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + settingsFile + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading [" + settingsFile + "]");
        }
    }

    /**
     * Reads a file from disk as a json element
     *
     * @param file - file to load, does not check if the
     *             file exists or is a json file.
     * @return element
     */
    public static JsonElement readElement(final File file)
    {
        try
        {
            FileReader stream = new FileReader(file);
            BufferedReader reader = new BufferedReader(stream);

            JsonReader jsonReader = new JsonReader(reader);
            JsonElement element = Streams.parse(jsonReader);

            reader.close();
            stream.close();
            return element;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse file as json [" + file + "]");
        }
    }

    /**
     * Converts a file read from disk into a string for parsing
     *
     * @param file - file, does not check if the file is valid
     * @return string
     */
    public static String readFileAsString(final File file)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            final StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read [" + file + "]");
        }
    }

    /**
     * Finds all files for parsing. As
     * well pre-builds some data needed
     * in order to parse said files.
     */
    public void loadWikiData()
    {
        //TODO load category file
        //TODO load category data
        //TODO use category data to file files
    }

    /**
     * Parses the data from said files
     */
    public void parseWikiData()
    {
        //TODO parse all found files
    }

    /**
     * Finalizes all wiki data and
     * links files together as needed.
     */
    public void buildWikiData()
    {
        //TODO link wiki pages together
        //TODO generate category footer
        //TODO link all images
        //TODO replace 'keys' with page links
    }

    /**
     * Converts the data into
     * HTML files using the provided
     * templates
     */
    public void buildPages()
    {
        //TODO inject data into page templates
        //TODO output pages
        //TODO validate pages
        //TODO if in GUI mod show pages to user (Ask first
    }

    /**
     * Loads the HTML templates
     * and validates to make sure
     * they will work.
     */
    public void loadHTML()
    {
        //TODO load templates
        //TODO validate templates
        //TODO convert templates into writer files for easier injection
    }
}
