package com.jeffdamon.qrviewfx.ui;

import javafx.beans.property.SimpleStringProperty;

public class QRCode {
    private final SimpleStringProperty qrCodeText;
    private final SimpleStringProperty alias;
    private final SimpleStringProperty timesSeen;

    public QRCode(String qrCodeText, String alias, String times){
        this.qrCodeText = new SimpleStringProperty(qrCodeText);
        this.alias = new SimpleStringProperty(alias);
        this.timesSeen = new SimpleStringProperty(times);
    }

    public String getQRCodeText(){
        return qrCodeText.get();
    }

    public void setQRCodeText(String text){
        qrCodeText.set(text);
    }

    public String getAlias(){
        return alias.get();
    }

    public void setAlias(String a){
        alias.set(a);
    }

    public String getTimesSeen(){
        return timesSeen.get();
    }

    public void setTimesSeen(String t){
        timesSeen.set(t);
    }

    @Override
    public String toString(){
        if(alias.getValueSafe().isEmpty()){
            return qrCodeText.getValueSafe();
        } else{
            return alias.getValueSafe();
        }
    }
}
