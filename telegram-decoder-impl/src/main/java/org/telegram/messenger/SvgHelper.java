package org.telegram.messenger;

import android.graphics.Path;
public class SvgHelper {
    public static String decompress(byte[] encoded) {
        try {
            StringBuilder path = new StringBuilder(encoded.length * 2);
            path.append('M');
            for (int i = 0; i < encoded.length; i++) {
                int num = encoded[i] & 0xff;
                if (num >= 128 + 64) {
                    int start = num - 128 - 64;
                    path.append("AACAAAAHAAALMAAAQASTAVAAAZaacaaaahaaalmaaaqastava.az0123456789-,".charAt(start));
                } else {
                    if (num >= 128) {
                        path.append(',');
                    } else if (num >= 64) {
                        path.append('-');
                    }
                    path.append(num & 63);
                }
            }
            path.append('z');
            return path.toString();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return "";
    }

    public static Path doPath(String s) {
        return null;
    }

}
