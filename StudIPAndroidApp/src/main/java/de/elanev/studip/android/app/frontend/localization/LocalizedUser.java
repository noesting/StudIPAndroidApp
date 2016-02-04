package de.elanev.studip.android.app.frontend.localization;

/**
 * Created by Nils on 08.01.2016.
 */
public class LocalizedUser {

    private String name;
    private String id;
    private String location;
    private int state;

    public LocalizedUser(String n, String i, String l) {
        name = n;
        id = i;
        location = l;
        state = 0;
    }

    public LocalizedUser(String i, String l) {
        name = new String();
        id = i;
        location = l;
        state = 0;
    }

    public LocalizedUser(String i) {
        id = i;
        state = 0;
    }

    public boolean sameUser(LocalizedUser user) {
        if(user.getId().equals(this.getId())) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isTutor() {
        if(state == 1) {
            return true;
        }
        return false;
    }

    public void setState(int state) {
        this.state = state;
    }


}
