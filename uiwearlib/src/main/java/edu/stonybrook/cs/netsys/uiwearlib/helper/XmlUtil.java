package edu.stonybrook.cs.netsys.uiwearlib.helper;

import static edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil.printAccNodeTreeD;

import android.graphics.Rect;
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
import java.util.Arrays;
import java.util.HashMap;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.AccNode;

/**
 * Created by qqcao on 11/6/16.
 *
 * XML utils for parsing and serialization
 */

public class XmlUtil {

    private static final String NODES_TAG = "nodes";
    private static final String NODE_TAG = "node";
    private static final String ID_TAG = "id";
    private static final String RECT_TAG = "rect";

    public static void serializeAppPreference(File preferenceFile,
            ArrayList<Rect> preferredNodes, HashMap<Rect, AccNode> appLeafNodesMap)
            throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        FileUtil.makeDirsIfNotExist(preferenceFile.getPath());
        FileWriter preferenceWriter = new FileWriter(preferenceFile);
        serializeAppPreference(serializer, preferenceWriter, preferredNodes, appLeafNodesMap);
    }

    public static void serializeAppPreference(XmlSerializer serializer,
            FileWriter preferenceWriter, ArrayList<Rect> preferredNodes,
            HashMap<Rect, AccNode> appLeafNodesMap) {

        Logger.d("xml nodes: " + Arrays.toString(preferredNodes.toArray()));
        try {
//            FileWriter fileWriter = new FileWriter(preference);
            serializer.setOutput(preferenceWriter);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, NODES_TAG);

            // serialize data
            for (Rect rect : preferredNodes) {
                AccNode node = appLeafNodesMap.get(rect);
                int count = node.getChildCount();
                if (count > 0) {
                    printAccNodeTreeD("xml", node);
                    String id = node.getViewId();
                    serializer.startTag(null, NODE_TAG);
                    serializer.startTag(null, ID_TAG);
                    if (id == null) {
                        // container node can have null id
                        serializer.text("null");
                    } else {
                        serializer.text(id);
                    }
                    serializer.endTag(null, ID_TAG);

                    serializer.startTag(null, RECT_TAG);
                    serializer.text(rect.flattenToString());
                    serializer.endTag(null, RECT_TAG);
                    // serialize all leaf children
                    serializer = serializeNode(serializer, node);
                    serializer.endTag(null, NODE_TAG);
                } else {
                    serializer = serializeNode(serializer, node);
                }

            }

            serializer.endTag(null, NODES_TAG);
            serializer.endDocument();

        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(e.getMessage());

        }
    }

    private static XmlSerializer serializeNode(XmlSerializer serializer, AccNode node)
            throws IOException {
        if (node == null) {
            Logger.e("node can't be null");
            return serializer;
        }

        int count = node.getChildCount();

        if (count == 0) {
            String id = node.getViewId();
            if (id == null) {
                // leaf node should not have null id
                return serializer;
            }
            Rect rect = node.getRectInScreen();
            String rectString = rect.flattenToString();
            serializer.startTag(null, NODE_TAG);
            serializer.startTag(null, ID_TAG);
            serializer.text(id);
            serializer.endTag(null, ID_TAG);

            serializer.startTag(null, RECT_TAG);
            serializer.text(rectString);
            serializer.endTag(null, RECT_TAG);

            serializer.endTag(null, NODE_TAG);
        } else {
            for (int i = 0; i < count; i++) {
                serializer = serializeNode(serializer, node.getChild(i));
            }
        }


        return serializer;
    }

    public static ArrayList<AccNode> deserializeAppPreference(File preferenceFile) {
        String preferenceString = FileUtil.readFile(preferenceFile.getPath()).toString();
        return deserializeAppPreference(preferenceString);
    }

    public static ArrayList<AccNode> deserializeAppPreference(String preferenceString) {
        ArrayList<AccNode> nodes = new ArrayList<>();
        StringReader stringReader = new StringReader(preferenceString);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(stringReader);
            parser.nextTag();
            nodes = readNodes(parser);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return nodes;

    }


    private static ArrayList<AccNode> readNodes(XmlPullParser parser) {
        ArrayList<AccNode> nodes = new ArrayList<>();
        try {
            parser.require(XmlPullParser.START_TAG, null, NODES_TAG);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (NODE_TAG.equals(name)) {
                    nodes.add(readNode(parser));
                } else {
                    skip(parser);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    private static AccNode readNode(XmlPullParser parser) {
        AccNode node = new AccNode();
        try {
            parser.require(XmlPullParser.START_TAG, null, NODE_TAG);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch (name) {
                    case ID_TAG:
                        String viewId = readViewId(parser);
                        node.setViewId(viewId);
                        break;
                    case RECT_TAG:
                        Rect rectInScreen = readRectInScreen(parser);
                        node.setRectInScreen(rectInScreen);
                        break;
                    case NODE_TAG:
                        node.addChild(readNode(parser));
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }


        return node;
    }

    private static String readViewId(XmlPullParser parser) {
        String id = null;
        try {
            parser.require(XmlPullParser.START_TAG, null, ID_TAG);
            id = readText(parser);
            parser.require(XmlPullParser.END_TAG, null, ID_TAG);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return id;
    }

    private static Rect readRectInScreen(XmlPullParser parser) {
        Rect rect = new Rect();
        try {
            parser.require(XmlPullParser.START_TAG, null, RECT_TAG);
            String id = readText(parser);
            rect = Rect.unflattenFromString(id);
            parser.require(XmlPullParser.END_TAG, null, RECT_TAG);

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return rect;
    }

    private static String readText(XmlPullParser parser) {
        String result = "";
        try {
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) {
        try {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        int depth = 1;
        while (depth != 0) {
            try {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                    default:
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parseMappingRule(File mappingRuleFile) {

    }
}
