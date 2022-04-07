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
public interface ChatInterface {

    public ContactInterface getC();

    public void setC(ContactInterface ci);

    public String getName();

    public void setName(String string);

    public boolean isGroup();

    public void setGroup(boolean bln);

    public long getId();

    public void setId(long l);
}
