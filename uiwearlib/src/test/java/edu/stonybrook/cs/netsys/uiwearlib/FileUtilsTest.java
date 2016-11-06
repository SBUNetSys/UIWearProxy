package edu.stonybrook.cs.netsys.uiwearlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by qqcao on 11/6/16.
 *
 * Test for FileUtils
 */
public class FileUtilsTest {

    private String testFilePath;
    public static final String testFileContent = "Lorem ipsum dolor sit amet, consectetur "
            + "adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
            + ". Ut enim ad minim\n"
            + "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo "
            + "consequat. Duis aute irure dolor in reprehenderit\n"
            + "in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint "
            + "occaecat cupidatat non proident, sunt in culpa qui officia\n"
            + "deserunt mollit anim id est laborum.";
    private File outFile = new File("out_text.txt");

    @Before
    public void setUp() throws Exception {
        testFilePath = getClass().getClassLoader().getResource("test_text.txt").getFile();
        System.out.println(testFilePath);
        if (!outFile.exists()) {
            boolean newFile = outFile.createNewFile();
            if (!newFile) {
                throw new IOException("cannot create output text file");
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        if (outFile.exists()) {
            boolean delete = outFile.delete();
            if (!delete) {
                throw new IOException("output text file cannot delete");
            }
        }
    }

    @Test
    public void readFile() throws Exception {
        assertEquals(FileUtils.readFile(testFilePath).toString(),
                testFileContent.replaceAll("(\r|\n)", ""));
    }

    @Test
    public void writeFile() throws Exception {
        FileUtils.writeFile(outFile.getPath(), testFileContent, false);
        assertTrue(outFile.exists());
        assertEquals(FileUtils.readFile(testFilePath).toString(),
                testFileContent.replaceAll("(\r|\n)", ""));
    }

    @Test
    public void writeFileAppend() throws Exception {
        FileUtils.writeFile(outFile.getPath(), testFileContent, false);
        // append text to the end of file
        FileUtils.writeFile(outFile.getPath(), testFileContent, true);
        assertTrue(outFile.exists());
        assertEquals(FileUtils.readFile(outFile.getPath()).toString(),
                (testFileContent + testFileContent).replaceAll("(\r|\n)", ""));
    }

}
