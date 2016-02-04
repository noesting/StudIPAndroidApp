package de.elanev.studip.android.app.frontend.localization;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by Nils on 15.05.2015.
 */
public class MapField {

    private final String TAG = "MapField";

    public int map_x;
    public int map_y;
    public int id;
    public String name;
    public String bezeichnung;

    private LinkedList<LocalizedUser> people;


    public int area_x_min;
    public int area_x_max;
    public int area_y_min;
    public int area_y_max;

    public MapField(int id, int x, int y, String name, String bezeichnung) {
        this.id = id;
        this.name = name;
        map_x = x;
        map_y = y;
        this.bezeichnung = bezeichnung;
        people = new LinkedList<LocalizedUser>();
    }

    public MapField(int id, int x, int y, String bezeichnung) {
        this.id = id;
        this.name = "default";
        map_x = x;
        map_y = y;
        this.bezeichnung = bezeichnung;
        people = new LinkedList<LocalizedUser>();
    }

    /**
     * Sets the range of the clickable area
     * @param x_min
     * @param x_max
     * @param y_min
     * @param y_max
     */
    public void setArea(int x_min, int x_max, int y_min, int y_max) {
        area_x_min = x_min;
        area_x_max = x_max;
        area_y_min = y_min;
        area_y_max = y_max;
    }

    /**
     * Tests whether a a point is inside the clickable area
     * @param x
     * @param y
     * @return true, if point is inside, false else
     */
    public boolean isInside(int x, int y) {
/*
        Log.d(TAG, "x: " + x);
        Log.d(TAG, "y: " + y);
        Log.d(TAG, "area_x_min: " + area_x_min);
        Log.d(TAG, "area_x_max: " + area_x_max);
        Log.d(TAG, "area_y_min: " + area_y_min);
        Log.d(TAG, "area_y_max: " + area_y_max);
*/
        if(x > area_x_min && x < area_x_max && y > area_y_min && y < area_y_max) {
            Log.d(TAG, "is Inside!");
            return true;
        }
        return false;
    }

    public boolean addToField(LocalizedUser person) {
        //people.add(person);
        for(int i = 0; i < people.size(); i++) {
            if(people.get(i).getId().equals(person.getId())) {
                return false;
            }
        }
        people.add(person);
        return true;
    }

    public boolean deleteFromField(LocalizedUser localizedUser) {
        for (int i = 0; i < people.size(); i++) {
            Log.d(TAG, "People name: " + people.get(i).getName());
            if(people.get(i).getId().equals(localizedUser.getId()) || people.get(i).getName().equals("Deleted User")) {
                people.remove(i);
                return true;
            }
        }
        return false;
    }


    public LinkedList<LocalizedUser> getPeople() {
        return people;
    }

    public boolean deletePeopleFromField() {
        people.clear();
        return true;
    }

    public boolean containsUser(LocalizedUser user) {
        for (int i = 0; i < people.size(); i++) {
            if(people.get(i).getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    public void setMapXY(int x, int y) {
        map_x = x;
        map_y = y;
    }

    public LocalizedUser getLocalizedUser(String userId) {
        for(int i = 0; i < people.size(); i++) {
            if(people.get(i).getId().equals(userId)) {
                return people.get(i);
            }
        }
        return null;
    }

    public boolean containsTutor() {
        for (int i = 0; i < people.size(); i++) {
            if(people.get(i).isTutor()) {
                return true;
            }
        }
        return false;
    }
}
