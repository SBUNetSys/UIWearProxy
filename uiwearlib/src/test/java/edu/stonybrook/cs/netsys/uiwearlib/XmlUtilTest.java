package edu.stonybrook.cs.netsys.uiwearlib;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import android.graphics.Rect;
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
import java.util.HashSet;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.AccNode;
import edu.stonybrook.cs.netsys.uiwearlib.helper.FileUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.XmlUtil;

/**
 * Created by qqcao on 11/6/16.
 *
 * Test for xml utils
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class XmlUtilTest {

    private XmlSerializer mXmlSerializer;
    private FileWriter mFileWriter;
    private ArrayList<Rect> mPreferenceNodes = new ArrayList<>();
    private HashMap<Rect, AccNode> mAppNodesMap = new HashMap<>();
    private HashSet<AccNode> mPreferenceNodesIdPairs = new HashSet<>();
    private File mInputPrefFile = new File(getClass().getClassLoader()
            .getResource("sample_pref.xml").getFile());
    private File mTestPrefFile = new File(mInputPrefFile.getParent() + "sample_pref_test.xml");

    private XmlSerializer mXmlSerializerList;
    private FileWriter mFileWriterList;
    private ArrayList<Rect> mPreferenceNodesList = new ArrayList<>();
    private HashMap<Rect, AccNode> mAppNodesMapList = new HashMap<>();
    private HashSet<AccNode> mPreferenceNodesIdPairsList = new HashSet<>();
    private File mInputPrefFileList = new File(getClass().getClassLoader()
            .getResource("list_pref.xml").getFile());
    private File mTestPrefFileList = new File(
            mInputPrefFileList.getParent() + "list_pref_test.xml");

    @Before
    public void setUp() throws Exception {
        if (!mTestPrefFile.exists()) {
            boolean newFile = mTestPrefFile.createNewFile();
            if (!newFile) {
                throw new IOException("cannot create test file");
            }
        }
        mFileWriter = new FileWriter(mTestPrefFile);
        mXmlSerializer = Xml.newSerializer();
        mXmlSerializer.setOutput(mFileWriter);
        Rect one = new Rect(420, 1485, 660, 1725);
        Rect two = new Rect(96, 254, 984, 1142);
        Rect three = new Rect(168, 1192, 912, 1253);
        String first = "com.spotify.music:id/btn_play";
        String second = "com.spotify.music:id/image";
        String third = "com.spotify.music:id/title";
        AccNode pair1 = new AccNode(first, one);
        AccNode pair2 = new AccNode(second, two);
        AccNode pair3 = new AccNode(third, three);
        mPreferenceNodes.add(one);
        mPreferenceNodes.add(two);
        mPreferenceNodes.add(three);
        mAppNodesMap.put(one, pair1);
        mAppNodesMap.put(two, pair2);
        mAppNodesMap.put(three, pair3);
        mPreferenceNodesIdPairs.add(pair1);
        mPreferenceNodesIdPairs.add(pair2);
        mPreferenceNodesIdPairs.add(pair3);

        if (!mTestPrefFileList.exists()) {
            boolean newFile = mTestPrefFileList.createNewFile();
            if (!newFile) {
                throw new IOException("cannot create test file for list");
            }
        }
        mFileWriterList = new FileWriter(mTestPrefFileList);
        mXmlSerializerList = Xml.newSerializer();
        mXmlSerializerList.setOutput(mFileWriterList);
        String rect1 = "0 264 1080 456";
        String rect2 = "48 288 192 432";
        String rect3 = "228 301 530 362";
        String rect4 = "228 370 566 419";
        String rect5 = "936 1620 1080 1764";
        String rect6 = "168 1642 912 1699";
        String rect7 = "168 1695 605 1744";
        String rect8 = "48 1668 96 1716";
        String id1 = "com.spotify.music:id/row_view";
        String id2 = "android:id/icon";
        String id3 = "android:id/text1";
        String id4 = "android:id/text2";
        String id5 = "com.spotify.music:id/playPause";
        String id6 = "com.spotify.music:id/title";
        String id7 = "com.spotify.music:id/subtitle";
        String id8 = "com.spotify.music:id/btn_chevron_up";
        AccNode item1 = new AccNode(id1, rect1);
        AccNode item2 = new AccNode(id2, rect2);
        AccNode item3 = new AccNode(id3, rect3);
        AccNode item4 = new AccNode(id4, rect4);
        AccNode item5 = new AccNode(id5, rect5);
        AccNode item6 = new AccNode(id6, rect6);
        AccNode item7 = new AccNode(id7, rect7);
        AccNode item8 = new AccNode(id8, rect8);
        item1.addChild(item2);
        item1.addChild(item3);
        item1.addChild(item4);
        mPreferenceNodesList.add(Rect.unflattenFromString(rect1));
        mPreferenceNodesList.add(Rect.unflattenFromString(rect5));
        mPreferenceNodesList.add(Rect.unflattenFromString(rect6));
        mPreferenceNodesList.add(Rect.unflattenFromString(rect7));
        mPreferenceNodesList.add(Rect.unflattenFromString(rect8));
        mAppNodesMapList.put(Rect.unflattenFromString(rect1), item1);
        mAppNodesMapList.put(Rect.unflattenFromString(rect5), item5);
        mAppNodesMapList.put(Rect.unflattenFromString(rect6), item6);
        mAppNodesMapList.put(Rect.unflattenFromString(rect7), item7);
        mAppNodesMapList.put(Rect.unflattenFromString(rect8), item8);
        mPreferenceNodesIdPairsList.add(item1);
        mPreferenceNodesIdPairsList.add(item5);
        mPreferenceNodesIdPairsList.add(item6);
        mPreferenceNodesIdPairsList.add(item7);
        mPreferenceNodesIdPairsList.add(item8);
    }

    @After
    public void tearDown() throws Exception {
        if (mTestPrefFile.exists()) {
            boolean delete = mTestPrefFile.delete();
            if (!delete) {
                throw new IOException("test file cannot delete");
            }
        }

        if (mTestPrefFileList.exists()) {
            boolean delete = mTestPrefFileList.delete();
            if (!delete) {
                throw new IOException("test file cannot delete");
            }
        }
    }

    @Test
    public void serializeAppPreference() throws Exception {
        XmlUtil.serializeAppPreference(mXmlSerializer, mFileWriter, mPreferenceNodes,
                mAppNodesMap);
        assertEquals(FileUtil.readFile(mTestPrefFile.getPath()).toString().
                        replaceAll("(\r|\n| )", ""),
                FileUtil.readFile(mInputPrefFile.getPath()).toString().
                        replaceAll("(\r|\n| )", ""));

    }

    @Test
    public void serializeAppPreferenceListView() throws Exception {
        XmlUtil.serializeAppPreference(mXmlSerializerList, mFileWriterList, mPreferenceNodesList,
                mAppNodesMapList);
        assertEquals(FileUtil.readFile(mTestPrefFileList.getPath()).toString().
                        replaceAll("(\r|\n| )", ""),
                FileUtil.readFile(mInputPrefFileList.getPath()).toString().
                        replaceAll("(\r|\n| )", ""));

    }

    @Test
    public void deserializeAppPreference() throws Exception {

        HashSet<AccNode> preferenceNodesIdPairs = XmlUtil.deserializeAppPreference(
                FileUtil.readFile(mInputPrefFile.getPath()).toString());
        System.out.println(preferenceNodesIdPairs);
        System.out.println(mPreferenceNodesIdPairs);
        assertTrue(mPreferenceNodesIdPairs.equals(preferenceNodesIdPairs));

    }

    @Test
    public void deserializeAppPreferenceListView() throws Exception {

        HashSet<AccNode> preferenceNodesIdPairs = XmlUtil.deserializeAppPreference(
                FileUtil.readFile(mInputPrefFileList.getPath()).toString());
        System.out.println(preferenceNodesIdPairs);
        System.out.println(mPreferenceNodesIdPairsList);
        assertTrue(mPreferenceNodesIdPairsList.equals(preferenceNodesIdPairs));

    }
}
