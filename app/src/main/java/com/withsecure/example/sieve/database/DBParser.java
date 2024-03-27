package com.withsecure.example.sieve.database;

import android.util.Log;
import android.util.Xml;

import com.withsecure.example.sieve.util.PasswordEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DBParser {
    public static String ns = null;

    static {
        DBParser.ns = null;
    }

    public static String getKey(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser0 = Xml.newPullParser();
        xmlPullParser0.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
        xmlPullParser0.setInput(in, null);
        xmlPullParser0.nextTag();
        xmlPullParser0.require(2, DBParser.ns, PWTable.TABLE_NAME);
        return xmlPullParser0.getAttributeValue(null, PWTable.KEY_TABLE_NAME);
    }

    public static String getPIN(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser0 = Xml.newPullParser();
        xmlPullParser0.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
        xmlPullParser0.setInput(in, null);
        xmlPullParser0.nextTag();
        xmlPullParser0.require(2, DBParser.ns, PWTable.TABLE_NAME);
        return xmlPullParser0.getAttributeValue(null, "Pin");
    }

    public static String processElement(PasswordEntry pe) {
        return "<entry>" + ("<service>" + pe.service + "</service>") + ("<username>" + pe.username + "</username>") + ("<email>" + pe.email + "</email>") + ("<password>" + pe.password + "</password>") + "</entry>";
    }

    private static String readElement(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        parser.require(2, DBParser.ns, tag);
        String s1 = DBParser.readText(parser);
        parser.require(3, DBParser.ns, tag);
        return s1;
    }

    private static PasswordEntry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        String service = null;
        String username = null;
        String email = null;
        String password = null;
        while(parser.next() != 3) {
            if(parser.getEventType() != 2) {
                continue;
            }

            String s4 = parser.getName();
            if(s4.equals(PWTable.COLUMN_NAME_SERVICE)) {
                service = DBParser.readElement(parser, PWTable.COLUMN_NAME_SERVICE);
                continue;
            }

            if(s4.equals(PWTable.COLUMN_NAME_USERNAME)) {
                username = DBParser.readElement(parser, PWTable.COLUMN_NAME_USERNAME);
                continue;
            }

            if(s4.equals(PWTable.COLUMN_NAME_EMAIL)) {
                email = DBParser.readElement(parser, PWTable.COLUMN_NAME_EMAIL);
                continue;
            }

            if(!s4.equals(PWTable.COLUMN_NAME_PASSWORD)) {
                continue;
            }

            password = DBParser.readElement(parser, PWTable.COLUMN_NAME_PASSWORD);
        }

        return new PasswordEntry(service, username, email, password);
    }

    private static List<PasswordEntry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, DBParser.ns, PWTable.TABLE_NAME);
        List<PasswordEntry> pe = new LinkedList<>();
        while(parser.next() != 3) {
            if(parser.getEventType() != 2) {
                continue;
            }

            if(parser.getName().equals("entry")) {
                pe.add(DBParser.readEntry(parser));
                continue;
            }

            DBParser.skip(parser);
        }

        return pe;
    }

    public static List<PasswordEntry> readFile(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser0 = Xml.newPullParser();
        xmlPullParser0.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
        xmlPullParser0.setInput(in, null);
        xmlPullParser0.nextTag();
        return DBParser.readFeed(xmlPullParser0);
    }

    private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if(parser.next() == 4) {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if(parser.getEventType() != 2) {
            Log.i("parser", "IllegalState thrown");
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
            }
        }
    }
}

