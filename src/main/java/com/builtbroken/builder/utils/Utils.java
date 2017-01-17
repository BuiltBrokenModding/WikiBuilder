package com.builtbroken.builder.utils;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2017.
 */
public class Utils
{
    /**
     * Reads a file from disk as a json element
     *
     * @param file - file to load, does not check if the
     *             file exists or is a json file.
     * @return element
     */
    public static JsonElement toJsonElement(final File file)
    {
        try
        {
            FileReader stream = new FileReader(file);
            BufferedReader reader = new BufferedReader(stream);

            JsonReader jsonReader = new JsonReader(reader);
            JsonElement element = Streams.parse(jsonReader);

            reader.close();
            stream.close();
            return element;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse file as json [" + file + "]");
        }
    }

    /**
     * Converts a file read from disk into a string for parsing
     *
     * @param file - file, does not check if the file is valid
     * @return string
     */
    public static String readFileAsString(final File file)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            final StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read [" + file + "]", e);
        }
    }

    public static File getFile(File workingDirectory, String value)
    {
        if (value.startsWith("."))
        {
            return new File(workingDirectory, value.substring(2));
        }
        else
        {
            return new File(value);
        }
    }
}
