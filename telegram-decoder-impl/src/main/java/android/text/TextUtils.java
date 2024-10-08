package android.text;

public class TextUtils {

    public static boolean isEmpty(String text) {
        if (text == null)
            return true;
        return text.isEmpty();
    }

}
