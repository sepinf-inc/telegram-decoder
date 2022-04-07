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
import java.util.Date;

public interface MessageInterface {

    public long getId();

    public void setId(long l);

    public String getMediaHash();

    public void setMediaHash(String string);

    public String getMediaFile();

    public void setMediaFile(String string);

    public boolean isFromMe();

    public void setFromMe(boolean bln);

    public void setToId(long l);

    public String getData();

    public void setData(String string);

    public Date getTimeStamp();

    public void setTimeStamp(Date date);

    public String getMediaMime();

    public void setMediaMime(String string);

    public boolean isLink();

    public void setLink(boolean bln);

    public byte[] getLinkImage();

    public void setLinkImage(byte[] bytes);

    public byte[] getThumb();

    public void setThumb(byte[] bytes);

    public String getHashThumb();

    public void setHashThumb(String string);

    public String getMediaName();

    public void setMediaName(String string);

    public String getType();

    public void setType(String string);

    public void setLatitude(Double d);

    public Double getLatitude();

    public void setLongitude(Double d);

    public Double getLongitude();
}
