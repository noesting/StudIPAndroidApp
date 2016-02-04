package de.elanev.studip.android.app.backend.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.gson.annotations.SerializedName;


/**
 * Created by Nils on 19.12.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonRootName(value = "user")
public class LocationRoom {

    /*
    public static final String ID = LocationRoom.class.getName() + ".id";
    public static final String TITLE = LocationRoom.class.getName() + ".title";
    */

   @JsonProperty("resource_id")
    public String resourceId;

    @JsonProperty("room_id")
    public int roomId;
/*
    @SerializedName("resource_id") String resource_id;

    @SerializedName("room_id") int room_id;

    public LocationRoom(String resource_id, int room_id) {
        this.resource_id = resource_id;
        this.room_id = room_id;
    }*/
}
