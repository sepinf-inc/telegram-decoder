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

public interface ContactInterface {

    public long getId();

    public void setId(long l);

    public String getName();

    public void setName(String string);

    public String getPhone();

    public void setPhone(String string);

    public String getUsername();

    public void setUsername(String string);

    public byte[] getAvatar();

    public void setAvatar(byte[] bytes);

    public String getLastName();

    public void setLastName(String string);

    public void setBigName(String string);

    public void setSmallName(String string);
}
