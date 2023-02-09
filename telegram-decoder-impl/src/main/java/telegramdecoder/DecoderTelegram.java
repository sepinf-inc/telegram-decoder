/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegramdecoder;


import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.TLRPC.DocumentAttribute;

import dpf.ap.gpinf.interfacetelegram.ContactInterface;
import dpf.ap.gpinf.interfacetelegram.DecoderTelegramInterface;
import dpf.ap.gpinf.interfacetelegram.MessageInterface;
import dpf.ap.gpinf.interfacetelegram.PhotoData;

/**
 *
 * @author ADMHauck
 */
public class DecoderTelegram implements DecoderTelegramInterface{
    
    TLRPC.Message m=null;
    TLRPC.User u=null;
    TLRPC.Chat c=null;
    TLRPC.ChatFull cf=null;
    @Override
    public void setDecoderData(byte[] data,int TYPE) {
        SerializedData s=new SerializedData(data);
        int aux=s.readInt32(false);
        m = null;
        u = null;
        c = null;
        cf = null;
        if(TYPE==MESSAGE){
            m=TLRPC.Message.TLdeserialize(s,aux, false);
        }
        if(TYPE==USER){
            u=TLRPC.User.TLdeserialize(s,aux, false);
        }
        if(TYPE==CHAT){
            c=TLRPC.Chat.TLdeserialize(s, aux, false);
            s=new SerializedData(data);
            aux=s.readInt32(false);
            
            cf=TLRPC.ChatFull.TLdeserialize(s, aux, false);
            
            
        }
        
    }

    @Override
    public void getUserData(ContactInterface user)  {
        if(user!=null && u!=null){
            user.setUsername(u.username);
            user.setId(u.id);
            user.setName(u.first_name);
            user.setLastName(u.last_name);
            user.setPhone(u.phone);
        }else{
            System.err.println("Erro ao carregar usuario");
        }
    }
    
    @Override
    public long getRemetenteId(){
        if(m!=null){
            if(m.from_id.user_id!=0){
                return m.from_id.user_id;
            }
            if(m.from_id.chat_id!=0){
                return m.from_id.chat_id;
            }
            if(m.from_id.channel_id!=0){
                return m.from_id.channel_id;
            }
            if(m instanceof TLRPC.TL_message){
                TLRPC.TL_message tl_m=(TLRPC.TL_message)m;
                if(tl_m.via_bot_id!=0){
                    return tl_m.via_bot_id;
                }                  
            }
        }        
        return 0;
    }
    private String objToString(Object o){
        Class<?> c=o.getClass();
        StringBuilder sb=new StringBuilder();
        while(c!=null){
        Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                try {
                    sb.append(field.getName()).append(" - ").append(field.get(o));
                    sb.append("\n");
                }catch (IllegalArgumentException | IllegalAccessException ex) {
                    //ignore
                }
            }
            c=c.getSuperclass();
            
        }
        return sb.toString();
    }

    @Override
    public void getMessageData(MessageInterface message) {
       
        if(message==null)
            return;
        if (m!=null) {
            
            message.setFromMe(m.out);
            message.setData(m.message);
            
            if(m.action!=null) {
                    message.setType(m.action.getClass().getSimpleName());
                    
                    if(m.action.call_id!=0) {
                        message.setType(message.getType()+":"+m.action.duration);
                    }
                    if(m.action instanceof TLRPC.TL_messageActionSetMessagesTTL){
                        TLRPC.TL_messageActionSetMessagesTTL aux=(TLRPC.TL_messageActionSetMessagesTTL)m.action;
                        message.setType(message.getType()+": "+aux.period + "s");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionTTLChange) {
                        message.setType(message.getType()+": "+m.action.ttl + "s");
                    }
                   
                    if(m.action instanceof TLRPC.TL_messageActionChatEditTitle) {   	                        		
                        message.setType(message.getType()+":"+m.action.title);
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatAddUser ){
                        if(m.action.users!=null){
                            String ids="";
                            boolean first=true;
                            for(long id:m.action.users){
                                if(first){
                                    ids+="Ids ";
                                    first=false;
                                }else{
                                    ids+=", ";
                                }
                                ids+=id;
                            }
                            message.setData(ids);
                        }
                    }

            }
            if(m.peer_id!=null){
                message.setToId(m.peer_id.user_id);
            }

            message.setTimeStamp(Date.from(Instant.ofEpochSecond(m.date)));
            //message.timeStamp=LocalDateTime.ofInstant(Instant.ofEpochSecond(), ZoneId.systemDefault())
            if(m.media!=null) {
                String msgDataAndSeparator = "";
                if (message.getData() != null && !message.getData().trim().isEmpty()) {
                    msgDataAndSeparator = message.getData().trim() + " | ";
                }
                if(m.media.document!=null) {
                    if(m.media.document instanceof TLRPC.TL_documentEmpty){
                        message.setData(msgDataAndSeparator + "Empty media");
                    }
                    message.setMediaMime(m.media.document.mime_type);
                }else
                if(m.media.photo!=null){
                    message.setMediaMime("image/jpeg");

                }else
                if(m.media.webpage!=null) {
                    message.setLink(true);
                    message.setMediaMime("link");

                }else
                if(m.media.description!=null){
                    message.setData(msgDataAndSeparator + "Desc: "+m.media.description);
                }else
                if(m.media.game!=null){
                    message.setData(msgDataAndSeparator + "Game: "+m.media.game.title);
                }else
                if(m.media.geo!=null){
                    message.setLatitude(m.media.geo.lat);
                    message.setLongitude(m.media.geo._long);
                    message.setMediaMime("geo");
                }else                
                if(m.media.vcard!=null){
                    message.setData(msgDataAndSeparator + "Vcard: "+m.media.vcard);
                }else
                if(m.media.phone_number!=null){
                    message.setData(msgDataAndSeparator + "Phone: "+m.media.phone_number);
                }else
                if(m.media instanceof TLRPC.TL_messageMediaEmpty){
                    //ignore see https://github.com/sepinf-inc/IPED/issues/1454
                    //message.setData(message.getData()+" Empty media");
                }else
                if(m.media instanceof TLRPC.TL_messageMediaPoll){
                    TLRPC.TL_messageMediaPoll mpool=(TLRPC.TL_messageMediaPoll)m.media;
                    String text = "Pool: " + mpool.poll.question;
                    int i = 0;
                    for(TLRPC.TL_pollAnswer a : mpool.poll.answers){
                       text += " | Answer " + (++i) + ": " + a.text;
                    }
                    text = msgDataAndSeparator + text;
                    message.setData(text);
                    
                }

            }
            
            if(m instanceof TLRPC.TL_messageEmpty){
                message.setData("Empty message");
            }
            

        }else{
            message.setData("unknow msg for decode");
        }
        if(message.getMediaMime()==null && message.getType()==null && (message.getData()==null || message.getData().isEmpty()) ){
            if(m.media!=null){
                System.out.println(objToString(m.media));
            }else{
                System.out.println(objToString(m));
            }
        }

    	                
    }


    @Override
    public void getChatData(ContactInterface chat) {
        if(chat!=null && c!=null){
            chat.setUsername(c.username);
            chat.setId(c.id);
            chat.setName(c.title);
            chat.setLastName(null);
            chat.setPhone(null);
        }
        
    }

    @Override
    public Map<String, Object> getAlltMetadata() {
        Map<String, Object> other = new TreeMap<>();

        Util.fieldsToMap(other, this.c);
        Util.fieldsToMap(other, cf);
        Util.fieldsToMap(other, this.u);
        Util.fieldsToMap(other, this.m);

        return other;
    }

    

    @Override
    public List<String> getDocumentNames() {
        ArrayList<String> list=new ArrayList<>();
        if(m!=null && m.media!=null && m.media.document!=null){
             list.add(m.media.document.id+"");
             for( DocumentAttribute at :m.media.document.attributes){
                //tentar achar pelo nome do arquivo original
                if(at.file_name!=null){
                	list.add(at.file_name);
                }
            }
         }
        return list;
    }

    @Override
    public List<PhotoData> getPhotoData() {
        ArrayList<PhotoData> list=new ArrayList<>();
        if(u!=null && u.photo!=null){
            if(u.photo.photo_big!=null){
                Photo p=new Photo();
                p.setName(""+ u.photo.photo_big.volume_id + "_" + u.photo.photo_big.local_id);
                list.add(p);
            }
            if(u.photo.photo_small!=null){
                Photo p=new Photo();
                p.setName(""+ u.photo.photo_small.volume_id + "_" + u.photo.photo_small.local_id);
                list.add(p);
            }
            
        }
        if(m!=null && m.media!=null){
            if(m.media.photo!=null){
                if(m.media.photo.sizes!=null &&m.media.photo.sizes.size()>0){
                    list.addAll(getPhotosFromSize(m.media.photo.sizes));
                }
            }
            if(m.media.webpage!=null){
                if(m.media.webpage.photo!=null && m.media.webpage.photo.sizes!=null){
                    list.addAll(getPhotosFromSize(m.media.webpage.photo.sizes));
                }
            }
            if(m.media.document!=null){
                if(m.media.document.thumbs!=null){
                    list.addAll(getPhotosFromSize(m.media.document.thumbs));
                }
            }
        }
        if(c!=null && c.photo!=null){
            if(c.photo.photo_big!=null){
                Photo p=new Photo();
                p.setName(""+ c.photo.photo_big.volume_id + "_" + c.photo.photo_big.local_id);
                list.add(p);
            }
            if(c.photo.photo_small!=null){
                Photo p=new Photo();
                p.setName(""+ c.photo.photo_small.volume_id + "_" + c.photo.photo_small.local_id);
                list.add(p);
            }
        }
        return list;
    }
    
    private List<PhotoData> getPhotosFromSize(ArrayList<TLRPC.PhotoSize> photos){
        ArrayList<PhotoData> l=new ArrayList<PhotoData>();
        if(photos==null){
            return l;
        }
        for(TLRPC.PhotoSize photo:photos){
            if(photo.location!=null){
                Photo p=new Photo();

                p.setName(photo.location.volume_id+"_"+photo.location.local_id);
                p.setSize(photo.size);
                l.add(p);
            }
        }
        return l;
    }

    @Override
    public long getDocumentSize() {
        if(m!=null && m.media!=null && m.media.document!=null){
            return m.media.document.size;
        }
        return -1;
    }
    public static Connection createConnection(String database){
         try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:"+database);
            } catch (Exception ex ) {
                System.err.println(ex.toString());
            }

        return null;
    }
    public static Object load(byte[] data){
        SerializedData s=new SerializedData(data);
        int aux=s.readInt32(false);
        return TLRPC.User.TLdeserialize(s,aux, false);
        
    }

    
    
    
    
    
    
}
