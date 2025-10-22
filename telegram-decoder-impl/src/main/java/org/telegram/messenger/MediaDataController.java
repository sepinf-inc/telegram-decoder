/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import java.util.ArrayList;

import org.telegram.tgnet.TLRPC;

import android.text.TextUtils;

@SuppressWarnings("unchecked")
public class MediaDataController{

    public static boolean entitiesEqual(TLRPC.MessageEntity entity1, TLRPC.MessageEntity entity2) {
        if (entity1.getClass() != entity2.getClass() ||
                entity1.offset != entity2.offset ||
                entity1.length != entity2.length ||
                !TextUtils.equals(entity1.url, entity2.url) ||
                !TextUtils.equals(entity1.language, entity2.language)) {
            return false;
        }
        if (entity1 instanceof TLRPC.TL_inputMessageEntityMentionName && ((TLRPC.TL_inputMessageEntityMentionName) entity1).user_id != ((TLRPC.TL_inputMessageEntityMentionName) entity2).user_id) {
            return false;
        }
        if (entity1 instanceof TLRPC.TL_messageEntityMentionName && ((TLRPC.TL_messageEntityMentionName) entity1).user_id != ((TLRPC.TL_messageEntityMentionName) entity2).user_id) {
            return false;
        }
        if (entity1 instanceof TLRPC.TL_messageEntityCustomEmoji && ((TLRPC.TL_messageEntityCustomEmoji) entity1).document_id != ((TLRPC.TL_messageEntityCustomEmoji) entity2).document_id) {
            return false;
        }

        return true;
    }

    public static boolean entitiesEqual(ArrayList<TLRPC.MessageEntity> entities1, ArrayList<TLRPC.MessageEntity> entities2) {
        if (entities1.size() != entities2.size()) {
            return false;
        }

        for (int i = 0; i < entities1.size(); i++) {
            TLRPC.MessageEntity entity1 = entities1.get(i);
            TLRPC.MessageEntity entity2 = entities2.get(i);

            if (!MediaDataController.entitiesEqual(entity1, entity2)) {
                return false;
            }
        }

        return true;
    }

}
