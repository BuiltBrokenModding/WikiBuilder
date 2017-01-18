package com.builtbroken.builder.data;

import com.builtbroken.builder.html.parts.JsonProcessorHTML;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Object containing an HTML page broken down into pieces for easy injection of data.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/17/2017.
 */
public class SegmentedHTML
{
    /** Content converted to HTML code and segmented for injection */
    public String[] htmlSegments;
    /** Points in the content segments to inject data into */
    public HashMap<String, Integer> injectionTags = new HashMap();
    /** Places in the HTML that contain a sub page reference */
    public HashMap<String, Integer> subPages = new HashMap();


    /** References to external pages or pre-existing pages */
    public HashMap<String, Integer> pageReferences;
    /** References to image files */
    public HashMap<String, Integer> imgReferences;


    /** References to other pages that need to go though linkReplaceKeys */
    public HashMap<String, Integer> pageLinks;

    /**
     * Called to load the template from disk
     * and pre-parse it for injection
     *
     * @param home - working folder to load
     *             templates from, unless the file
     *             is set to a direct path
     */
    public void loadHTMLFile(File home, String file_string)
    {
        File file = Utils.getFile(home, file_string);
        process(Utils.readFileAsString(file));
    }

    /**
     * Converts json data into html data
     *
     * @param object - json content object
     * @return HTML as string
     */
    public String toHTML(final JsonObject object)
    {
        String html = "";
        //Convert content into HTML
        Set<Map.Entry<String, JsonElement>> entrySet = object.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet)
        {
            html += JsonProcessorHTML.process(entry.getKey(), entry.getValue());
        }
        return html;
    }

    /**
     * Processes the HTML that was loaded from file.
     * <p>
     * It splits the template by "#" looking for tags
     * to inject data into it. A valid tag must start
     * with "page:" or "data:" in order to be used. If it
     * doesn't then it will be ignored and show up on the
     * final HTML page as a string.
     * <p>
     * Page denotes the injection point for a page.
     * <p>
     * data denotes the injection point for a value.
     *
     * @param templateString - template as a string
     */
    public void process(String templateString)
    {
        //Clear old tags and init
        injectionTags = new HashMap();
        pageReferences = new HashMap();
        pageLinks = new HashMap();
        imgReferences = new HashMap();

        //Split string to make it easier to format
        htmlSegments = templateString.split("#");

        //Loop through segments looking for tags
        for (int i = 0; i < htmlSegments.length; i++)
        {
            final String value = htmlSegments[i];
            //Anything longer than 100 is most likely not an injection key
            if (value.length() < 100)
            {
                //Lower case tag to make it easier to check
                processInjectionTag(value.toLowerCase(), i, value);
            }
        }
    }

    /**
     * Called to handle an injection key that was found.
     * <p>
     * Injection key follows the format "#type:keyName#" with
     * "#" removed before calling this method.
     *
     * @param key      - lower case copy of the tag for faster checks
     * @param index    - index of the key in the segment data
     * @param original - original value of the key
     */
    protected boolean processInjectionTag(String key, int index, String original)
    {
        if (key.startsWith("data:"))
        {
            injectionTags.put(key.substring(5, key.length()), index);
            return true;
        }
        else if (key.startsWith("page:"))
        {
            subPages.put(key.substring(5, key.length()), index);
            return true;
        }
        else if (key.startsWith("pageref:"))
        {
            pageReferences.put(key.substring(8, key.length()), index);
        }
        else if (key.startsWith("link:"))
        {
            pageLinks.put(key.substring(5, key.length()), index);
        }
        else if (key.startsWith("img:"))
        {
            imgReferences.put(key.substring(4, key.length()), index);
        }
        return false;
    }


    /**
     * Converts the segment data into a
     * full HTML page as a string
     *
     * @return html as string
     */
    public String buildHTML()
    {
        String html = "";
        for (String s : htmlSegments)
        {
            html += s;
        }
        return html;
    }
}
