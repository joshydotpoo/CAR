package com.car.carsquad.carapp;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chatroom {
    private String name, msgTime, lastMsg;
    private CircleImageView profileImg;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMsgTime() { return msgTime; }
    public void setMsgTime(String msgTime) { this.msgTime = msgTime; }

    public String getLastMsg() { return lastMsg; }
    public void setLastMsg(String lastMsg) { this.lastMsg = lastMsg; }

    /*
    public CircleImageView getProfileImg() { return profileImg; }
    public void setProfileImg(CircleImageView profileImg) {
        this.profileImg = profileImg; }
    */

}