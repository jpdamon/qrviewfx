package com.jeffdamon.qrviewfx;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

/** Filter to return only image files (.jpg, .jpeg, .png). **/
public class ImageFileFilter implements FilenameFilter {
    private final ArrayList<String> extensions = new ArrayList<>(Arrays.asList(new String[]{
            ".jpg", ".jpeg", ".png"
    }));

    @Override
    public boolean accept(File dir, String name) {
        name = name.toLowerCase();
        for(String ext : extensions){
            if (name.endsWith(ext)){
                return true;
            }
        }

        return false;
    }
}
