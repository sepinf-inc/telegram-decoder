package org.telegram.messenger.utils.tlutils;

import java.util.ArrayList;

import org.telegram.tgnet.TLRPC;

public final class TlUtils {
    private TlUtils() {
    }

    /** Same contract as upstream: drops all entities if any is out of bounds for text. */
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
