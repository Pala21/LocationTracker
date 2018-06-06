package com.example.pau.locationtracker;

/**
 * Created by pau on 05/06/2018.
 */

public class Groups {

    public String groupname;
    public String image;
    public String key;
    public boolean visibility;

    public Groups(String groupname, String image, Boolean visibility, String key) {
        this.image = image;
        this.groupname = groupname;
        this.visibility = visibility;
        this.key = key;
    }

    public Groups(){}

    public String getGroupname()
    {
        return groupname;
    }

    public void setGroupname(String groupname)
    {

        this.groupname = groupname;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)

    {
        this.image = image;
    }


    public boolean getVisibility(){
        return visibility;
    }


    public String getKey() {
        return key;
    }
}


