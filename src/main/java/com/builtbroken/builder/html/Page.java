package com.builtbroken.builder.html;

import com.builtbroken.builder.data.PageData;

import java.util.HashMap;
import java.util.Map;

/**
 * Tempory data object to store the page as it is being processed
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class Page
{
    /** Information about the page */
    public PageData data;
    /** Actual page as segments */
    public String[] pageSegments;

    public Page(PageData data)
    {
        this.data = data;
    }

    /**
     * Injected data into a page template
     *
     * @param template - form used to generate the page and what the data
     *                 is injected into
     * @param data     - injection data (Tag, data)
     * @return new page
     */
    public void inject(PageTemplate template, HashMap<String, String> data)
    {
        //Copy the segment data
        if (pageSegments == null)
        {
            pageSegments = template.pageSegments.clone();
        }
        //Loop data looking tags to inject
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            //Get key lower case to compare faster
            String key = entry.getKey().toLowerCase();
            if (template.injectionTags.containsKey(key))
            {
                //Inject data into tag's location
                int index = template.injectionTags.get(key);
                pageSegments[index] = entry.getValue();
            }
        }
    }

    /**
     * Called to build the page
     * as a string that will be
     * saved to file.
     *
     * @return
     */
    public String buildPage()
    {
        String output = "";
        for (String string : pageSegments)
        {
            output += string;
        }
        return output;
    }
}
