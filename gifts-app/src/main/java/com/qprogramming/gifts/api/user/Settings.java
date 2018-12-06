package com.qprogramming.gifts.api.user;

public class Settings {
    private boolean newsletter;
    private boolean publicList;
    private String language;

    public boolean getNewsletter() {
        return newsletter;
    }

    public void setNewsletter(boolean newsletter) {
        this.newsletter = newsletter;
    }

    public boolean getPublicList() {
        return publicList;
    }

    public void setPublicList(boolean publicList) {
        this.publicList = publicList;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
