package edu.stonybrook.cs.netsys.uiwearlib.helper;

import android.graphics.Rect;
import android.support.v4.util.Pair;
import android.util.Xml;

import com.orhanobut.logger.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qqcao on 11/6/16.
 *
 * XML utils for parsing and serialization
 */

public class XmlUtil {

    private static final String ROOT_TAG = "nodes";
    private static final String NODE_TAG = "node";
    private static final String ID_TAG = "id";
    private static final String RECT_TAG = "rect";

    public static void serializeAppPreference(File preferenceFile,
            ArrayList<Rect> preferredNodes, HashMap<Rect, String> appLeafNodesMap)
            throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        FileUtil.makeDirsIfNotExist(preferenceFile.getPath());
        FileWriter preferenceWriter = new FileWriter(preferenceFile);
        serializeAppPreference(serializer, preferenceWriter, preferredNodes, appLeafNodesMap);
    }

    public static void serializeAppPreference(XmlSerializer serializer,
            FileWriter preferenceWriter, ArrayList<Rect> preferredNodes,
            HashMap<Rect, String> appLeafNodesMap) {

        try {
//            FileWriter fileWriter = new FileWriter(preference);
            serializer.setOutput(preferenceWriter);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, ROOT_TAG);

            // serialize data
            for (Rect rect : preferredNodes) {

                String id = appLeafNodesMap.get(rect);
                String rectString = rect.flattenToString();

                serializer.startTag(null, NODE_TAG);

                serializer.startTag(null, ID_TAG);
                if (id == null) {
                    serializer.text("null");
                } else {
                    serializer.text(id);
                }
                serializer.endTag(null, ID_TAG);

                serializer.startTag(null, RECT_TAG);
                serializer.text(rectString);
                serializer.endTag(null, RECT_TAG);

                serializer.endTag(null, NODE_TAG);
            }

            serializer.endTag(null, ROOT_TAG);
            serializer.endDocument();

        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(e.getMessage());

        }
    }

    public static ArrayList<Pair<String, Rect>> deserializeAppPreference(File preferenceFile) {
        String preferenceString = FileUtil.readFile(preferenceFile.getPath()).toString();
        return deserializeAppPreference(preferenceString);
    }

    public static ArrayList<Pair<String, Rect>> deserializeAppPreference(String preferenceString) {
        ArrayList<Pair<String, Rect>> nodes = new ArrayList<>();
        Pair<String, Rect> pair = null;
        String id = null;
        Rect rect = null;

        StringReader stringReader = new StringReader(preferenceString);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(stringReader);
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String tag = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:

                        if (ID_TAG.equals(tag)) {
                            id = parser.nextText();
                        }

                        if (RECT_TAG.equals(tag)) {
                            rect = Rect.unflattenFromString(parser.nextText());
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        if (tag.equals(NODE_TAG)) {
                            if (id != null && rect != null) {
                                pair = new Pair<>(id, rect);
                            }
                        }

                        if (NODE_TAG.equals(parser.getName())) {
                            nodes.add(pair);
                        }
                        break;
                    default:
                }
                event = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    public static void parseMappingRule(File mappingRuleFile) {

    }
}
