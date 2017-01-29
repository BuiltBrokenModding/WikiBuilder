package com.builtbroken.builder.html.page;

import com.builtbroken.builder.html.theme.PageTemplate;

import java.io.File;

/**
 * A page containing an entry in the wiki
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/28/2017.
 */
public class EntryPage extends Page
{
    /** Location to save this page */
    public File outputFile;

    @Override
    protected PageTemplate getPrimaryTemplate()
    {
        return theme.mainTemplate;
    }
}
