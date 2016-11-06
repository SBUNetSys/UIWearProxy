package edu.stonybrook.cs.netsys.uiwearlib;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import android.graphics.Rect;
import android.support.v4.util.Pair;
import android.util.Xml;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qqcao on 11/6/16.
 *
 * Test for xml utils
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class XmlUtilsTest {

    private XmlSerializer mXmlSerializer;

    private FileWriter mFileWriter;
    private ArrayList<Rect> mPreferenceNodes = new ArrayList<>();
    private HashMap<Rect, String> mAppLeafNodesMap = new HashMap<>();
    private ArrayList<Pair<String, Rect>> mPreferenceNodesIdPairs = new ArrayList<>();

    private File inputPrefFile = new File(
            getClass().getClassLoader().getResource("sample_pref.xml").getFile());
    private File testPrefFile = new File("sample_pref_test.xml");


    @Before
    public void setUp() throws Exception {
        if (!testPrefFile.exists()) {
            boolean newFile = testPrefFile.createNewFile();
            if (!newFile) {
                throw new IOException("cannot create test file");
            }
        }
        mFileWriter = new FileWriter(testPrefFile);

        mXmlSerializer = Xml.newSerializer();
        mXmlSerializer.setOutput(mFileWriter);

        Rect one = new Rect(420, 1485, 660, 1725);
        Rect two = new Rect(96, 254, 984, 1142);
        Rect three = new Rect(168, 1192, 912, 1253);
        String first = "com.spotify.music:id/btn_play";
        String second = "com.spotify.music:id/image";
        String third = "com.spotify.music:id/title";

        mPreferenceNodes.add(one);
        mPreferenceNodes.add(two);
        mPreferenceNodes.add(three);

        mAppLeafNodesMap.put(one, first);
        mAppLeafNodesMap.put(two, second);
        mAppLeafNodesMap.put(three, third);

        Pair<String, Rect> pair1 = new Pair<>(first, one);
        Pair<String, Rect> pair2 = new Pair<>(second, two);
        Pair<String, Rect> pair3 = new Pair<>(third, three);

        mPreferenceNodesIdPairs.add(pair1);
        mPreferenceNodesIdPairs.add(pair2);
        mPreferenceNodesIdPairs.add(pair3);
    }

    @After
    public void tearDown() throws Exception {
        if (testPrefFile.exists()) {
            boolean delete = testPrefFile.delete();
            if (!delete) {
                throw new IOException("test file cannot delete");
            }
        }
    }

    @Test
    public void serializeAppPreference() throws Exception {
        XmlUtils.serializeAppPreference(mXmlSerializer, mFileWriter, mPreferenceNodes,
                mAppLeafNodesMap);
        assertEquals(FileUtils.readFile(testPrefFile.getPath()).toString().
                        replaceAll("(\r|\n| )", ""),
                FileUtils.readFile(inputPrefFile.getPath()).toString().
                        replaceAll("(\r|\n| )", ""));

    }


    @Test
    public void deserializeAppPreference() throws Exception {

        ArrayList<Pair<String, Rect>> preferenceNodesIdPairs = XmlUtils.deserializeAppPreference(
                FileUtils.readFile(inputPrefFile.getPath()).toString());
        assertTrue(mPreferenceNodesIdPairs.equals(preferenceNodesIdPairs));

    }
}
