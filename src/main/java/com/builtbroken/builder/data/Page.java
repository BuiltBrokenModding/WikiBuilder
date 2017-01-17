package com.builtbroken.builder.data;

import com.builtbroken.builder.theme.PageTemplate;
import com.builtbroken.builder.theme.PageTheme;

import java.io.File;
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
    /** Location to save this page */
    public File outputFile;
    /** Theme that defines that layout of this page */
    public PageTheme theme;
    /** Actual page as segments */
    public HashMap<PageTemplate, String[]> pageSegments;


    /**
     * Called to set the primary template used to generate this page
     *
     * @param theme - theme used to define this page's layout
     */
    public void setTheme(PageTheme theme)
    {
        this.theme = theme;
        pageSegments = new HashMap();
        addTemplate(theme, theme.templates.get(theme.mainTemplate));
    }

    /**
     * Used to recursively add template pages to this page
     *
     * @param theme    - theme used to define
     * @param template
     */
    private void addTemplate(PageTheme theme, PageTemplate template)
    {
        if (!pageSegments.containsKey(template))
        {
            pageSegments.put(template, null);
            if (template.subPages.size() > 0)
            {
                for (String templateName : template.subPages.keySet())
                {
                    PageTemplate subTemplate = theme.templates.get(templateName.toLowerCase());
                    if (subTemplate != null)
                    {
                        addTemplate(theme, subTemplate);
                    }
                }
            }
        }
    }

    /**
     * Injected data into a page template(s)
     *
     * @param data - injection data (Tag, data)
     * @return new page
     */
    public void inject(HashMap<String, String> data)
    {
        for (PageTemplate template : pageSegments.keySet())
        {
            String[] segment = pageSegments.get(template);
            if (segment == null)
            {
                segment = template.htmlSegments.clone();
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
                    segment[index] = entry.getValue();
                }
            }
            pageSegments.put(template, segment);
        }
    }

    /**
     * Called to inject a single piece of data into the template(s)
     *
     * @param tag   - key for the data
     * @param value - value to inject
     */
    public void inject(String tag, String value)
    {
        for (PageTemplate template : pageSegments.keySet())
        {
            if (template.injectionTags.containsKey(tag.toLowerCase()))
            {
                String[] segment = pageSegments.get(template);
                if (segment == null)
                {
                    segment = template.htmlSegments.clone();
                }
                int index = template.injectionTags.get(tag.toLowerCase());
                segment[index] = value;
                pageSegments.put(template, segment);
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
        return buildPages(theme.templates.get(theme.mainTemplate));
    }

    /**
     * Recursively generates the page from the templates
     *
     * @param template - page template
     * @return page as string
     */
    private String buildPages(PageTemplate template)
    {
        String output = "";
        String[] segments = pageSegments.get(template);
        for (Map.Entry<String, Integer> entry : template.subPages.entrySet())
        {
            segments[entry.getValue()] = buildPages(theme.templates.get(entry.getKey()));
        }
        for (String s : segments)
        {
            output += s;
        }
        return output;
    }
}
