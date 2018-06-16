package com.example.pau.locationtracker;

public class Friends {
    public String username;
    public String image;
    public String fullname;
    public String key;

    public Friends(String username, String fullname, String image, String key) {
        this.username = username;
        this.fullname = fullname;
        this.image = image;
        this.key = key;
    }

    public Friends(){}


    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }


    public String getUserImage()
    {
        return image;
    }

    public String getKey() { return key; }

    public void setUserImage(String image)

    {
        this.image = image;
    }
}

