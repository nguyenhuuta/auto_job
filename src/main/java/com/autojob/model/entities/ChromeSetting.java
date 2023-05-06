package com.autojob.model.entities;

public class ChromeSetting {
    private boolean visible = false;
    private int wight = 0;
    private int height = 0;
    private String profilePath;
    public String profileName = "";
    private boolean maximized = true;

    public ChromeSetting() {
    }

    public ChromeSetting(boolean visible, int wight, int height, String profilePath, boolean maximized) {
        this.visible = visible;
        this.wight = wight;
        this.height = height;
        this.profilePath = profilePath;
        this.maximized = maximized;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getWight() {
        return wight;
    }

    public int getHeight() {
        return height;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public boolean isMaximized() {
        return maximized;
    }
}
