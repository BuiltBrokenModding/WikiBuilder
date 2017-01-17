package com.builtbroken.builder.html;

import com.builtbroken.builder.data.SegmentedHTML;

import java.io.File;

/**
 * Templates are used to turn data into actual HTML pages.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class PageTemplate extends SegmentedHTML
{
    /** Replace tag or file ID */
    public final String tag;
    /** Location of the file on disc as a string */
    public final String file_string;

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

    public void load(File home)
    {
        super.loadHTMLFile(home, file_string);
    }
}
