package edu.stonybrook.cs.netsys.uiwearlib;

import android.graphics.Bitmap;
import android.os.Environment;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * File Utils
 * <ul>
 * Read or write file
 * <li>{@link #readFile(String)} read file</li>
 * <li>{@link #writeFile(String, String, boolean)} write file from String</li>
 * </ul>
 */
public class FileUtils {

    private FileUtils() {
        throw new AssertionError();
    }

    /**
     * read file
     *
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator FileReader
     */
    public static StringBuilder readFile(String filePath) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (!file.isFile()) {
            return fileContent;
        }

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("cannot read file: " + filePath, e);
        }

        return fileContent;
    }

    /**
     * write file
     *
     * @param filePath the file to save
     * @param content  the string content to write
     * @param append   is append, if true, write to the end of file,
     *                 else clear content of file and write into it
     * @return return false if content is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content, boolean append) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (folderName == null || folderName.isEmpty()) {
            return false;
        }
        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    private static String getFolderName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    // imageName does not need a file extension like .png, just the name
    public static void storeBitmap(Bitmap bitmap, String folder, String imageName) {
        try {
            //create app folder
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getPath() + File.separator + folder);
            String appFolder = "";

            boolean isDirCreated = dir.exists() || dir.mkdirs();

            if (isDirCreated) {
                appFolder = dir.getPath();
                Logger.d("dir created success:" + appFolder);
            } else {
                Logger.e("dir failed to create");
            }

            File imageFile = new File(appFolder
                    + File.separator
                    + imageName
                    + ".png");

            FileOutputStream outputStream = new FileOutputStream(imageFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }
}
