package org.telegram.messenger.utils.tlutils;

import java.util.ArrayList;

import org.telegram.tgnet.TLRPC;

/** Stub of upstream TlUtils with only the methods used by tgnet. */
public final class TlUtils {
    private TlUtils() {
    }

    /**
     * Same logic as upstream (called from readParams): drops all entities if any of them is out of
     * the text bounds. The upstream ApplicationLoader.isAndroidTestEnvironment() check is always false here.
     */
    public static void validateEntities(ArrayList<TLRPC.MessageEntity> entities, String text) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        final int textLength = text != null ? text.length() : 0;
        for (int i = 0, n = entities.size(); i < n; i++) {
            final TLRPC.MessageEntity e = entities.get(i);
            if (e == null || e.offset < 0 || e.offset > textLength || e.length <= 0 || e.length > textLength - e.offset) {
                entities.clear();
                return;
            }
        }
    }
}
