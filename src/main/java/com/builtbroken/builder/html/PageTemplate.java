package com.builtbroken.builder.html;

import com.builtbroken.builder.utils.Utils;

import java.io.File;
import java.util.HashMap;

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

    /** Places in the template that call for injection */
    public HashMap<String, Integer> injectionTags;
    public HashMap<String, Integer> subPages;

    /** Page broken into segments for easy injection */
    public String[] pageSegments;

    /**
     * Creates a new template instance
     *
     * @param tag  - name of the template, also used as an injection tag
     * @param file - location of the template on disk
     */
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
    public void load(File home)
    {
        File  file = Utils.getFile(home, file_string);
        process(Utils.readFileAsString(file));
    }

    /**
     * Processes the template that was loaded from file.
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
        //Split string to make it easier to format
        pageSegments = templateString.split("#");

        //Loop through segments looking for tags
        for (int i = 0; i < pageSegments.length; i++)
        {
            final String s = pageSegments[i];
            //Anything longer than 100 is most likely not an injection key
            if (s.length() < 100)
            {
                //Lower case tag to make it easier to check
                String string = s.toLowerCase();
                if (string.startsWith("data"))
                {
                    //Add tag as lower case to make it easier to check
                    injectionTags.put(string.toLowerCase(), i);
                }
                else if(string.startsWith("page"))
                {
                    subPages.put(string.toLowerCase(), i);
                }
            }
        }
    }
}
