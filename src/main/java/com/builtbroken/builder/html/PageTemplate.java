package com.builtbroken.builder.html;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Templates are used to turn data into actual HTML pages.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class PageTemplate
{
    /** Replace tag or file ID */
    public final String tag;
    /** Location of the file on disc as a string */
    public final String file_string;

    public HashMap<String, Integer> injectionTags;
    public String[] pageSegments;

    public PageTemplate(String tag, String file)
    {
        this.tag = tag;
        file_string = file;
    }

    /**
     * Called to load the template from disk
     * and pre-parse it for injection
     *
     * @param home - working folder to load
     *             templates from, unless the file
     *             is set to a direct path
     */
    public void loadAndProcess(File home)
    {
        File file;
        if (file_string.startsWith("./"))
        {
            file = new File(home, file_string.replace("." + File.separator, ""));
        }
        else
        {
            file = new File(file_string);
        }

        String templateString = PageBuilder.readFileAsString(file);

        injectionTags = new HashMap();
        pageSegments = templateString.split("#");

        for (int i = 0; i < pageSegments.length; i++)
        {
            final String s = pageSegments[i];
            //Anything longer than 100 is most likely not an injection key
            if (s.length() < 100)
            {
                String string = s.toLowerCase();
                if (string.startsWith("data") || string.startsWith("page"))
                {
                    injectionTags.put(string.toLowerCase(), i);
                }
            }
        }
    }

    /**
     * Creates a page using this template
     *
     * @param data - injection data (Tag, data)
     * @return new page
     */
    public String createPage(HashMap<String, String> data)
    {
        String[] copy = pageSegments.clone();
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            String key = entry.getKey().toLowerCase();
            if (injectionTags.containsKey(key))
            {
                int index = injectionTags.get(key);
                copy[index] = entry.getValue();
            }
        }
        String output = "";
        for (String string : pageSegments)
        {
            output += string;
        }
        return output;
    }
}
