/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dpf.ap.gpinf.interfacetelegram;

/**
 *
 * @author HAUCK
 */



import java.util.List;
import java.util.Map;

public interface DecoderTelegramInterface {

    public static final int MESSAGE = 1;
    public static final int USER = 2;
    public static final int CHAT = 3;

    public void setDecoderData(byte[] bytes, int i);

    public void getUserData(ContactInterface ci);

    public void getMessageData(MessageInterface mi);

    public void getChatData(ContactInterface ci);

    public long getRemetenteId();

    public List<String> getDocumentNames();

    public List<PhotoData> getPhotoData();

    public long getDocumentSize();

    public Map<String, Object> getAlltMetadata();
}
