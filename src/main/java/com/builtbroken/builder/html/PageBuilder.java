package com.builtbroken.builder.html;

import com.builtbroken.builder.data.CategoryData;
import com.builtbroken.builder.data.Page;
import com.builtbroken.builder.data.PageData;
import com.builtbroken.builder.html.parts.HTMLPartHeader;
import com.builtbroken.builder.html.parts.HTMLPartParagraph;
import com.builtbroken.builder.html.parts.JsonProcessorHTML;
import com.builtbroken.builder.theme.PageTheme;
import com.builtbroken.builder.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    /** Main directory to read and write files inside */
    public final File outputDirectory;
    /** File for loading settings */
    public final File settingsFile;

    /** Location to access images */
    public File imageDirectory;
    /** Location to get cateogry data from */
    public File categoryFile;

    /** Extra variables loaded from settings to be used later */
    public HashMap<String, String> vars;

    /** Theme/templates used to generate pages. */
    public PageTheme pageTheme;

    /** All categories for the wiki */
    public List<CategoryData> categoryData;
    /** All pages for the wiki */
    public List<PageData> loadedWikiData;
    /** Actual generated pages in data form */
    public List<Page> generatedPages;
    /** List of used images */
    public List<String> usedImages;
    /** Map of image keys to paths on disk (relative to workspace) */
    public HashMap<String, String> images;

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
        if (launchSettings.containsKey("outputPath"))
        {
            outputDirectory = Utils.getFile(workingDirectory, launchSettings.get("outputPath"));
        }
        else
        {
            outputDirectory = new File(workingDirectory, "output");
        }
    }

    public void parseSettings()
    {
        if (settingsFile.exists() && settingsFile.isFile())
        {
            JsonElement element = Utils.toJsonElement(settingsFile);

            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("vars"))
                {
                    Gson gson = new Gson();
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("vars"), map.getClass());
                    vars.putAll(map);
                }
                if (object.has("images"))
                {
                    String value = object.getAsJsonPrimitive("images").getAsString();
                    imageDirectory = Utils.getFile(workingDirectory, value);
                    vars.put("imagePath", value);
                }
                else
                {
                    throw new RuntimeException("Missing image folder location from " + settingsFile);
                }
                if (object.has("categories"))
                {
                    String value = object.getAsJsonPrimitive("categories").getAsString();
                    categoryFile = Utils.getFile(workingDirectory, value);
                }
                else
                {
                    throw new RuntimeException("Missing categories data from " + settingsFile);
                }
                if (object.has("theme"))
                {
                    pageTheme = new PageTheme(object.getAsJsonPrimitive("theme").getAsString());
                }
                else
                {
                    throw new RuntimeException("Missing theme data from " + settingsFile);
                }
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + settingsFile + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading or missing [" + settingsFile + "]");
        }
    }

    /**
     * Finds all files for parsing.
     */
    public void loadWikiData()
    {
        loadedWikiData = new ArrayList();
        if (categoryFile.exists() && categoryFile.isFile())
        {
            JsonElement element = Utils.toJsonElement(categoryFile);

            if (element.isJsonObject())
            {
                JsonObject object = element.getAsJsonObject();
                if (object.has("categories"))
                {
                    categoryData = new ArrayList();
                    Gson gson = new Gson();
                    Map<String, String> map = new HashMap();
                    map = (Map<String, String>) gson.fromJson(object.get("categories"), map.getClass());
                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        CategoryData data = new CategoryData(entry.getKey(), entry.getValue());
                        data.load(workingDirectory);
                        data.getPages(loadedWikiData);
                    }
                }
            }
            else
            {
                throw new RuntimeException("File does not contain a json object [" + categoryFile + "]");
            }
        }
        else
        {
            throw new RuntimeException("File is invalid for reading or missing [" + categoryFile + "]");
        }
    }

    /**
     * Parses the data from said files
     */
    public void parseWikiData()
    {
        for (PageData data : loadedWikiData)
        {
            data.load();
        }
    }

    /**
     * Finalizes all wiki data and
     * links files together as needed.
     */
    public void buildWikiData()
    {
        usedImages = new ArrayList();
        images = new HashMap();

        //Build up the map of replacement data
        HashMap<String, String> linkReplaceKeys = new HashMap();
        HashMap<String, String> imageReplaceKeys = new HashMap();
        for (PageData data : loadedWikiData)
        {
            for (Map.Entry<String, String> entry : data.linkReplaceKeys.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (!linkReplaceKeys.containsKey(key))
                {
                    linkReplaceKeys.put(key, "<a href=\"" + data.getOutput(vars.get("outputPath")) + "\">" + entry.getValue() + "</a>");
                }
                else
                {
                    throw new RuntimeException("Duplicate link key[" + entry.getKey() + "] was found for page [" + data.pageName + "] key is linked to " + linkReplaceKeys.get(key));
                }
            }
            for (Map.Entry<String, String> entry : data.imgReplaceKeys.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (!imageReplaceKeys.containsKey(key))
                {
                    String path = vars.get("imagePath") + entry.getValue();
                    imageReplaceKeys.put(key, "<img src=\"" + entry.getValue() + "\">");
                    images.put(key, path);
                }
                else
                {
                    throw new RuntimeException("Duplicate image key[" + entry.getKey() + "] was found for page [" + data.pageName + "] key is linked to " + imageReplaceKeys.get(key));
                }
            }
        }
        //Loop all pages to replace data
        for (PageData data : loadedWikiData)
        {
            for (Map.Entry<String, Integer> entry : data.pageLinks.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (linkReplaceKeys.containsKey(key))
                {
                    data.htmlSegments[entry.getValue()] = linkReplaceKeys.get(key);
                }
                else
                {
                    System.out.println("Warning: " + data.pageName + " is missing a link reference for " + entry.getKey());
                }
            }
            for (Map.Entry<String, Integer> entry : data.imgReferences.entrySet())
            {
                final String key = entry.getKey().toLowerCase();
                if (imageReplaceKeys.containsKey(key))
                {
                    data.htmlSegments[entry.getValue()] = imageReplaceKeys.get(key);
                    //Keep track of what images we have used
                    usedImages.add(key);
                }
                else
                {
                    System.out.println("Warning: " + data.pageName + " is missing an image reference for " + entry.getKey());
                }
            }
        }
        //TODO generate category footer
    }

    /**
     * Converts the data into
     * HTML files using the provided
     * templates
     */
    public void buildPages()
    {
        //Inject missing data into
        for (PageData data : loadedWikiData)
        {
            Page page = new Page();
            page.outputFile = new File(outputDirectory, data.getOutput(vars.get("outputPath")));
            page.setTheme(pageTheme);

            //Inject page main content
            page.inject("wikiContentHtml", data.buildHTML());
            //Inject page data
            page.inject(data.data);
            //Inject global data
            page.inject(vars);
            //Add page to generated pages
            generatedPages.add(page);
        }

        //Output pages to file
        for (Page page : generatedPages)
        {
            String html = page.buildPage();
            //TODO validate pages (Check that tags match, that images exist, that links work)
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(page.outputFile)))
            {
                bw.write(html);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        List<String> unusedImages = new ArrayList();
        if (!vars.containsKey("doNotCopyImages"))
        {
            for (Map.Entry<String, String> entry : images.entrySet())
            {
                if (usedImages.contains(entry.getKey()))
                {
                    final File file = new File(imageDirectory, entry.getValue());
                    if (file.exists())
                    {
                        copyFile(file, new File(outputDirectory, entry.getValue()));
                    }
                    else
                    {
                        System.out.println("Warning: Image[key='" + entry.getKey() + "' path='" + entry.getValue() + "'] is missing!!");
                    }
                }
                else
                {
                    unusedImages.add(entry.getKey());
                }
            }
        }
        //TODO if in GUI mode show pages to user (Ask user first)
    }

    public static void copyFile(File sourceFile, File destFile)
    {
        //http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
        try
        {
            if (!destFile.exists())
            {
                destFile.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try
            {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error: Failed to copy " + sourceFile + " to " + destFile, e);
            }
            finally
            {
                if (source != null)
                {
                    source.close();
                }
                if (destination != null)
                {
                    destination.close();
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unexpected error while copying " + sourceFile + " to " + destFile, e);
        }
    }

    /**
     * Loads the HTML templates
     * and validates to make sure
     * they will work.
     */
    public void loadHTML()
    {
        pageTheme.load(workingDirectory);
        pageTheme.loadTemplates();

        JsonProcessorHTML.registerPart("h", new HTMLPartHeader());
        JsonProcessorHTML.registerPart("p", new HTMLPartParagraph());
    }
}
