package com.builtbroken.builder.data;

import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores information about a wiki page before it is generated
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/14/2017.
 */
public class PageData
{
    /** Name of the page */
    public String pageName;
    /** Location of the json data for this page. */
    public final File file;
    /** Category this was loaded for */
    public CategoryData category;
    /** Data unique to just this page, will be combined with global data right before creating the page. */
    public final HashMap<String, String> data;

    /** Content converted to HTML code and segmented for injection */
    public String[] contentSegments;
    /** Points in the content segments to inject data into */
    public HashMap<String, Integer> injectionTags;
    /** References to other pages in this page's HTML */
    public HashMap<String, Integer> pageReferences;
    /** References to other pages that need to go though linkReplaceKeys */
    public HashMap<String, Integer> pageLinks;
    /** Link replace keys for this page, used to convert '#link#' to html link code with name in other pages. */
    public HashMap<String, String> linkReplaceKeys;

    public PageData(CategoryData category, File file)
    {
        this.category = category;
        this.file = file;
        data = new HashMap();
    }

    /**
     * Called to load the page from json
     */
    public void load()
    {
        JsonElement element = Utils.toJsonElement(file);
        if (element.isJsonObject())
        {
            JsonObject object = element.getAsJsonObject();
            if (object.has("pageName"))
            {
                pageName = object.getAsJsonPrimitive("pageName").getAsString();
            }
            if (object.has("type"))
            {
                String value = object.getAsJsonPrimitive("type").getAsString().toLowerCase();
                if (value.equals("content"))
                {
                    category.pages.add(pageName);
                }
            }
            if (object.has("replaceKeys"))
            {
                Gson gson = new Gson();
                Map<String, String> map = new HashMap();
                map = (Map<String, String>) gson.fromJson(object.get("replaceKeys"), map.getClass());
                linkReplaceKeys.putAll(map);
            }
            if (object.has("content"))
            {
                //Split HTML into segments for injection
                process(buildHTML(object.getAsJsonObject("content")));

            }
        }
        else
        {
            throw new RuntimeException("File " + file + " is not a valid json object so can not be parsed into a wiki page.");
        }
    }

    /**
     * Called to build the HTML data from json
     *
     * @param object - json content object
     * @return HTML as string
     */
    public String buildHTML(final JsonObject object)
    {
        String html = "";

        //Convert content into HTML
        Set<Map.Entry<String, JsonElement>> entrySet = object.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet)
        {
            //TODO replace 'startsWith' with regex or something more specific
            JsonElement value = entry.getValue();
            if (entry.getKey().startsWith("p"))
            {
                html += "<p>" + entry.getValue() + "</p>";
            }
            else if (entry.getKey().startsWith("h"))
            {
                if (value.isJsonObject())
                {
                    JsonObject h = value.getAsJsonObject();
                    String text = h.getAsJsonPrimitive("text").getAsString();
                    int size = 2;
                    if (h.has("size"))
                    {
                        JsonPrimitive p = h.getAsJsonPrimitive("size");
                        if (p.isString())
                        {
                            String s = p.getAsString().toLowerCase();
                            if (s.equals("small"))
                            {
                                size = 3;
                            }
                            else if (s.equals("medium"))
                            {
                                size = 2;
                            }
                            else if (s.equals("large"))
                            {
                                size = 1;
                            }
                        }
                        else
                        {
                            size = p.getAsInt();
                        }
                    }
                    if (h.has("link"))
                    {
                        String link = h.getAsJsonPrimitive("link").getAsString();
                        if (link.startsWith("url"))
                        {
                            html += "<h" + size + "><a href=\"" + link + "\">" + text + "</h" + size + ">";
                        }
                        else if (link.endsWith(".json"))
                        {
                            html += "<h" + size + "><a href=\"#PageRef:" + link + "#\">" + text + "</h" + size + ">";
                        }
                        else
                        {
                            html += "<h" + size + "><a href=\"#" + link + "#\">" + text + "</h" + size + ">";
                        }
                    }
                }
                else
                {
                    html += "<h2>" + entry.getValue() + "</h2>";
                }
            }
            else
            {
                throw new RuntimeException("Unrecognized json element key [" + entry.getKey() + "] for html parsing from page [" + pageName + "] from file [" + file + "]");
            }
        }
        return html;
    }

    /**
     * Processes the HTML to find segments
     * that need to be replaced to complete
     * the page
     *
     * @param html - content of the page
     */
    public void process(String html)
    {
        //Clear old tags and init
        injectionTags = new HashMap();
        pageReferences = new HashMap();
        //Split string to make it easier to format
        contentSegments = html.split("#");

        //Loop through segments looking for tags
        for (int i = 0; i < contentSegments.length; i++)
        {
            final String s = contentSegments[i];
            //Anything longer than 100 is most likely not an injection key
            if (s.length() < 100)
            {
                //Lower case tag to make it easier to check
                String string = s.toLowerCase();
                if (string.startsWith("pageref:"))
                {
                    pageReferences.put(string.substring(s.indexOf(":") + 1), i);
                }
                else if (string.startsWith("data:"))
                {
                    //Add tag as lower case to make it easier to check
                    injectionTags.put(string.substring(s.indexOf(":") + 1), i);
                }
                else if (string.startsWith("link:"))
                {
                    //Add tag as lower case to make it easier to check
                    pageLinks.put(string.substring(s.indexOf(":") + 1), i);
                }
            }
        }
    }

    public String buildHTML()
    {
        return null;
    }
}
