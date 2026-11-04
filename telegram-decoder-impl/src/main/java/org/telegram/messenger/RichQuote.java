package org.telegram.messenger;

import android.text.TextUtils;

import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_iv;

import java.util.ArrayList;

/**
 * Trimmed port of Telegram's org.telegram.messenger.RichQuote, keeping only the
 * flat-text conversion used while deserializing TL_messageReplyHeader.quote_rich.
 * UI/selection related code was removed.
 */
public class RichQuote {

    /* ---------- flat text ---------- */

    public static String flatText(TL_iv.RichText text) {
        final StringBuilder sb = new StringBuilder();
        appendFlat(text, sb, null);
        return sb.toString();
    }

    public static int flatLength(TL_iv.RichText text) {
        return flatText(text).length();
    }

    /** Fragments joined with \n, as plain text with entities, for displaying the quote. */
    public static String flatText(ArrayList<TL_iv.RichText> fragments, ArrayList<TLRPC.MessageEntity> entities) {
        final StringBuilder sb = new StringBuilder();
        if (fragments != null) {
            for (int i = 0; i < fragments.size(); ++i) {
                if (i > 0) sb.append('\n');
                appendFlat(fragments.get(i), sb, entities);
            }
        }
        return sb.toString();
    }

    private static void appendFlat(TL_iv.RichText text, StringBuilder sb, ArrayList<TLRPC.MessageEntity> entities) {
        if (text == null || text instanceof TL_iv.textEmpty || text instanceof TL_iv.textImage) {
            return;
        }
        if (text instanceof TL_iv.textPlain) {
            final String s = ((TL_iv.textPlain) text).text;
            if (s != null) sb.append(s);
            return;
        }
        if (text instanceof TL_iv.textMath) {
            final String s = ((TL_iv.textMath) text).source;
            if (s != null) sb.append(s);
            return;
        }
        final int start = sb.length();
        if (text instanceof TL_iv.textCustomEmoji) {
            final TL_iv.textCustomEmoji emoji = (TL_iv.textCustomEmoji) text;
            sb.append(TextUtils.isEmpty(emoji.alt) ? "😀" : emoji.alt);
            if (entities != null) {
                final TLRPC.TL_messageEntityCustomEmoji entity = new TLRPC.TL_messageEntityCustomEmoji();
                entity.document_id = emoji.document_id;
                addEntity(entities, entity, start, sb.length());
            }
            return;
        }
        if (text instanceof TL_iv.textConcat) {
            for (TL_iv.RichText child : text.texts) {
                appendFlat(child, sb, entities);
            }
            return;
        }
        if (text instanceof TL_iv.textDiff) {
            final TL_iv.textDiff diff = (TL_iv.textDiff) text;
            appendFlat(isEmpty(diff.text) ? diff.old_text : diff.text, sb, entities);
            return;
        }
        appendFlat(text.text, sb, entities);
        if (entities == null) return;
        final TLRPC.MessageEntity entity;
        if (text instanceof TL_iv.textBold) {
            entity = new TLRPC.TL_messageEntityBold();
        } else if (text instanceof TL_iv.textItalic) {
            entity = new TLRPC.TL_messageEntityItalic();
        } else if (text instanceof TL_iv.textUnderline) {
            entity = new TLRPC.TL_messageEntityUnderline();
        } else if (text instanceof TL_iv.textStrike) {
            entity = new TLRPC.TL_messageEntityStrike();
        } else if (text instanceof TL_iv.textFixed) {
            entity = new TLRPC.TL_messageEntityCode();
        } else if (text instanceof TL_iv.textSpoiler) {
            entity = new TLRPC.TL_messageEntitySpoiler();
        } else if (text instanceof TL_iv.textUrl) {
            entity = new TLRPC.TL_messageEntityTextUrl();
            entity.url = ((TL_iv.textUrl) text).url;
        } else {
            entity = null;
        }
        if (entity != null) {
            addEntity(entities, entity, start, sb.length());
        }
    }

    private static void addEntity(ArrayList<TLRPC.MessageEntity> entities, TLRPC.MessageEntity entity, int start, int end) {
        if (end <= start) return;
        entity.offset = start;
        entity.length = end - start;
        entities.add(entity);
    }

    /** Fills quote_text and quote_entities from quote_rich, so the reply header can be displayed like a plain quote. */
    public static void fillFlat(TLRPC.MessageReplyHeader header) {
        if (header == null || header.quote_rich == null || header.quote_text != null || ApplicationLoader.isAndroidTestEnvironment()) {
            return;
        }
        final ArrayList<TLRPC.MessageEntity> entities = new ArrayList<>();
        header.quote_text = flatText(header.quote_rich, entities);
        header.flags |= 64;
        header.quote_entities = entities;
        if (!entities.isEmpty()) {
            header.flags |= 128;
        }
    }

    /** Copy of org.telegram.ui.iv.RichTextStyle.isEmpty (UI class not available here). */
    private static boolean isEmpty(TL_iv.RichText rt) {
        if (rt == null || rt instanceof TL_iv.textEmpty) {
            return true;
        }
        if (rt instanceof TL_iv.textPlain) {
            String text = ((TL_iv.textPlain) rt).text;
            return text == null || text.isEmpty();
        }
        if (rt instanceof TL_iv.textCustomEmoji) {
            return false;
        }
        if (rt instanceof TL_iv.textMath) {
            String source = ((TL_iv.textMath) rt).source;
            return source == null || source.isEmpty();
        }
        if (rt instanceof TL_iv.textConcat) {
            for (TL_iv.RichText child : ((TL_iv.textConcat) rt).texts) {
                if (!isEmpty(child)) {
                    return false;
                }
            }
            return true;
        }
        if (rt instanceof TL_iv.textDiff) {
            TL_iv.textDiff diff = (TL_iv.textDiff) rt;
            return isEmpty(diff.text) && isEmpty(diff.old_text);
        }
        return isEmpty(rt.text);
    }
}
