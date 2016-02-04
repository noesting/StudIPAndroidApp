package de.elanev.studip.android.app.frontend.localization;

/**
 * Created by Nils on 28.04.2015.
 */
public class FingerPrintElement {

    private String bssid;
    private double level;  //signal strength

    /**
     * Creates a new element for the Fingerprint consisting of a
     * BSSID and signal strength of an access-point nearby.
     * @param bssid The access-points BSSID
     * @param level The measured signal-strength
     */
    public FingerPrintElement(String bssid, double level) {
        this.bssid = bssid;
        this.level = level;
    }

    /**
     * Returns the BSSID of this element.
     * @return The BSSID of this element.
     */
    public String getBssid() {
        return bssid;
    }

    /**
     * Returns the signal-strength of this element.
     * @return The signal-strength of this element.
     */
    public double getLevel() {
        return level;
    }

    /**
     * Sets the level
     * @param new_level the new level
     */
    public void setLevel(double new_level) {
        this.level = new_level;
    }

    /**
     * Returns the data of this object as a readable string in
     * the format [BSSID,LEVEL;] like the storage in the database.
     * @return The String with the object-information.
     */
    @Override
    public String toString() {
        String str = new String();
        str = str + bssid + "," + String.valueOf(level) + ";";
        return str;
    }

}
