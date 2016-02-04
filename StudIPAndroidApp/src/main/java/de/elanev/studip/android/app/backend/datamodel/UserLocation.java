package de.elanev.studip.android.app.backend.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Nils on 05.01.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLocation {



    @JsonProperty("user_id")
    public String userId;

    @JsonProperty("resource_id")
    public String resourceId;

    @JsonProperty("geoLocation")
    public String geoLocation;

    @JsonProperty("mkdate")
    public long mkdate;

    @JsonProperty("chdate")
    public long chdate;


/*
    @SerializedName("resource_id")
    public String resource_id;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("geoLocation")
    public String geoLocation;

    @SerializedName("chdate")
    public long chdate;

    @SerializedName("mkdate")
    public long mkdate;

    public UserLocation(String resource_id, String user_id) {
        this.resource_id = resource_id;
        this.user_id = user_id;
        geoLocation = "";
        chdate = 0;
        mkdate = 0;
    }

    public UserLocation() {

    }*/
}
