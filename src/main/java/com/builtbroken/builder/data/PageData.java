package com.builtbroken.builder.data;

import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about a wiki page before it is generated
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageData extends SegmentedHTML
{
    /** Name of the page */
    public String pageName;
    /** Category to display under */
    public String category;
    /** Type of page */
    public String type;
    /** Location of the json data for this page. */
    public final File file;
    /** Data unique to just this page, will be injected into the {@link Page} object */
    public final HashMap<String, String> data;
    /** Image replace keys for this page, used to convert '#img#' to html link code with name in other pages. */
    public HashMap<String, String> imgReplaceKeys;
    /** Link replace keys for this page, used to convert '#link#' to html link code with name in other pages. */
    public HashMap<String, String> linkReplaceKeys;

    /**
     * Creates a new page data object
     *
     * @param file - file path of the page
     */
    public PageData(File file)
    {
        this.file = file;
        data = new HashMap();
    }

    /**
     * Called to load the page from json
     */
    public void load()
    {
        //TODO format nano time outputs on debugger
        debug("Loading new Page Data");

        long startTime = System.nanoTime();
        debug("\tLoading file from disk");
        JsonElement element = Utils.toJsonElement(file);
        startTime = System.nanoTime() - startTime;
        debug("\tDone..." + startTime + "ns");

        if (element.isJsonObject())
        {
            debug("\tParsing json");
            startTime = System.nanoTime();

            JsonObject object = element.getAsJsonObject();
            if (object.has("pageName"))
            {
                pageName = object.getAsJsonPrimitive("pageName").getAsString();
                debug("\tName: " + pageName);
            }
            if (object.has("type"))
            {
                type = object.getAsJsonPrimitive("type").getAsString().toLowerCase();
                debug("\tType: " + type);
            }
            if (object.has("category"))
            {
                category = object.getAsJsonPrimitive("category").getAsString().toLowerCase();
                debug("\tCategory: " + category);
            }
            if (object.has("replaceKeys"))
            {
                Gson gson = new Gson();
                Map<String, String> map = new HashMap();
                map = (Map<String, String>) gson.fromJson(object.get("replaceKeys"), map.getClass());
                linkReplaceKeys.putAll(map);
                debug("\tLinks Keys: " + linkReplaceKeys.size());
            }
            if (object.has("imageKeys"))
            {
                Gson gson = new Gson();
                Map<String, String> map = new HashMap();
                map = (Map<String, String>) gson.fromJson(object.get("imageKeys"), map.getClass());
                imgReplaceKeys.putAll(map);
                debug("\tImage Keys: " + imgReplaceKeys.size());
            }
            if (object.has("content"))
            {
                debug("\tLoading Page Content");
                long time = System.nanoTime();
                //Split HTML into segments for injection
                process(toHTML(object.getAsJsonObject("content")));
                time = System.nanoTime() - time;
                debug("\tDone..." + time + "ns");
            }
            //Debug
            startTime = System.nanoTime() - startTime;
            debug("Done..." + startTime + "ns");
        }
        else
        {
            throw new RuntimeException("File " + file + " is not a valid json object so can not be parsed into a wiki page.");
        }
    }

    private void debug(String msg)
    {
        //TODO add way to disable
        //TODO use actual logging system
        //TODO save log to disk
        System.out.println("[PageData] " + msg);
    }

    /**
     * Gets the path the page will be outputted to
     * <p>
     * This is used both for saving the file and linking
     * it to other pages.
     *
     * @param basePath - base string path for the folder
     *                 the page will be saved inside.
     * @return string path, including name & extension of the file
     */
    public String getOutput(String basePath)
    {
        return basePath + pageName + ".html";
    }
}
