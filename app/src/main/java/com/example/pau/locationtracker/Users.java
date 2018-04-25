package com.example.pau.locationtracker;

/**
 * Created by pau on 24/04/2018.
 */

public class Users {
    public String username;
    public String image;
    public String fullname;

    public Users(String username, String fullname, String image) {
        this.username = username;
        this.fullname = fullname;
        this.image = image;
    }

    public Users(){}


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

    public void setUserImage(String image)

    {
        this.image = image;
    }
}
