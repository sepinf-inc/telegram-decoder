package android.text;

public class TextUtils {

    public static boolean isEmpty(String text) {
        if (text == null)
            return true;
        return text.isEmpty();
    }

    public static boolean equals(String s1, String s2) {
        return s1 == s2 || s1 != null && s1.equals(s2);
    }

}
