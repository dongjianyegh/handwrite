package com.dongjianye.handwrite.base;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author dongjianye on 4/27/21
 */
public class TextResourceReader {

    public static String readTextFileFromResource(final Context context, int resourceId) {
        if (context == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        InputStream inputStream;
        try {
            inputStream = context.getResources().openRawResource(resourceId);

            InputStreamReader reader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(reader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                builder.append(nextLine);
                builder.append('\n');
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } finally {

        }

        return builder.toString();
    }
}