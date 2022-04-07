/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegramdecoder;

import dpf.ap.gpinf.interfacetelegram.PhotoData;

/**
 *
 * @author ADMHauck
 */
public class Photo implements PhotoData{
    private String name=null;
    private long size=0;
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }
    
    public void setName(String name){
        this.name=name;
    }
    public void setSize(long size){
        this.size=size;
    }
    
}
