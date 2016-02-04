package de.elanev.studip.android.app.frontend.localization;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by Nils on 28.04.2015.
 */
public class Place implements Comparable<Place> {

    private final String TAG = "Place";

    private int place;  //identifier for the specific place
    private FingerPrintElement[] fingerprint;  //the fingerprint to compare places with

    private int fail_limit = 3; //limit for different wifi signals
    private int diff_limit = 10; //limit for signal strength difference

    /**
     * Creates a Place-Object from a given id and Fingerprint as string
     * @param p the id of the created place
     * @param fp the fingerprint of the created place
     */
    public Place(int p, String fp) {
        place = p;
        getPrintFromString(fp);
    }

    /**
     * Converts the Fingerprint in String-Format to a custom data-type.
     * @param fp The Fingerprint in String-Format
     */
    private void getPrintFromString(String fp) {
        String[] fps = fp.split(";");  //Splits to format [BSSID_0,LEVEL_0] [BSSID_1.LEVEL_1]
        fingerprint = new FingerPrintElement[fps.length]; //Now, we know the length of our FP-Array


        String[] single_print;  //Stackholder for FingerPrintElement as String
        for(int i = 0 ; i < fps.length; i++) {
            single_print = fps[i].split(","); //Splitting BSSID and LEVEL
            fingerprint[i] = new FingerPrintElement(single_print[0], Double.parseDouble(single_print[1]));
        }
    }

    /**
     * Returns the identifier of this place-object
     * @return
     */
    public int getPlace() {
        return place;
    }

    /**
     * Returns the Fingerprint in custom data-type format.
     * @return The Fingerprint as array of FingerPrintElements
     */
    public FingerPrintElement[] getFingerprint() {
        return fingerprint;
    }

    /**
     * Compares two place-objects by it's Fingerprint.
     * This method only provides a statement concerning equality, not order.
     * @return 0 if objects are considered equal, 1 else
     */
    public int compareTo(Place p) {
        int fail_counter = 0;    //counts the number of failures of element comparison
        for(int i = 0; i < fingerprint.length; i++) {
            boolean found = false;
            int j = 0;
            while(!found && j < p.getFingerprint().length) {  //try to find equal bssids
                if(fingerprint[i].getBssid().equals(p.getFingerprint()[j].getBssid())) {
                    found = true;
                }
                j++;
            }
            j--; //one too far, because if we've found a match, j will still be incremented

            double diff = fingerprint[i].getLevel() - p.getFingerprint()[j].getLevel();  //get signal strength difference

            if(!found || Math.abs(diff) > diff_limit) {
                if(fail_counter > fail_limit) {
                    System.out.println("failed");
                    return 1;         //Differ more than 3 (fail_counter) times -> not equal
                }
                else {
                    fail_counter++;
                }
            }
        }
        return 0;        // don't differ -> equal places
    }

    public double distanceTo(Place p) {

        double distance = 0.0;

        boolean[] found_elements_this = new boolean[fingerprint.length];
        boolean[] found_elements_p = new boolean[p.getFingerprint().length];

        //finding matching BSSIDs, also store in found_elements_this
        // which elements were not found of this fingerprint
        for (int i = 0; i < fingerprint.length; i++) {
            boolean found = false;
            found_elements_this[i] = false;
            int j = 0;
            while(!found && j < p.getFingerprint().length) {  //try to find equal bssids
                if(fingerprint[i].getBssid().equals(p.getFingerprint()[j].getBssid())) {
                    found = true;
                    found_elements_this[i] = true;
                }
                j++;
            }
            j--; //one too far, because if we've found a match, j will still be incremented

           // Log.d(TAG, "Diff between: " + fingerprint[i].getBssid() + " and " + p.getFingerprint()[j].getBssid());
            double diff = fingerprint[i].getLevel() - p.getFingerprint()[j].getLevel(); // get the x-y
            distance += diff * diff; // get the (x-y)^2
        }

        //store in found_elements_p which elements of are not in this
        for (int i = 0; i < p.getFingerprint().length; i++) {
            boolean found = false;
            found_elements_p[i] = false;
            int j = 0;
            while(!found && j < fingerprint.length) {  //try to find equal bssids
                if(p.getFingerprint()[i].getBssid().equals(fingerprint[j].getBssid())) {
                    found = true;
                    found_elements_p[i] = true;
                }
                j++;
            }
        }

        //add missing (not found) elements to distance
        //the ones that are in this print but not in p
        for (int i = 0; i < found_elements_this.length; i++) {
            if(!found_elements_this[i]) {
                distance += fingerprint[i].getLevel() * fingerprint[i]. getLevel();
            }
        }
        //the ones that are in p but not in this one
        for (int i = 0; i < found_elements_p.length; i++) {
            if(!found_elements_p[i]) {
                distance += p.getFingerprint()[i].getLevel() * p.getFingerprint()[i]. getLevel();
            }
        }

        distance = Math.sqrt(distance);
        return distance;
    }

    public FingerPrintElement[] calculateAveragePrint(String[] fingerprints) {

        int devider = fingerprints.length;

        //adding all BSSIDs to a list, adding values of sig str of each print,
        // devide each value by devider

        //this is the resluting print, will later be stored in array
        LinkedList<FingerPrintElement> resPrint = new LinkedList<FingerPrintElement>();


        //now each print must be stored in the resPrint list
        for(int i = 0; i < fingerprints.length; i++) {

            Log.d(TAG, "i=" + i + " length=" + fingerprints.length);


            //first we get the print from the string
            FingerPrintElement[] fingerprintlocal;
            Log.d(TAG, "Current Fingerprint: " + fingerprints[i]);

            String[] fps = fingerprints[i].split(";");  //Splits to format [BSSID_0,LEVEL_0] [BSSID_1.LEVEL_1]
            fingerprintlocal = new FingerPrintElement[fps.length]; //Now, we know the length of our FP-Array


            String[] single_print;  //Stackholder for FingerPrintElement as String
            for(int j = 0 ; j < fps.length; j++) {
                Log.d(TAG, "Current Fingerprintsingle: " + fps[j]);
                single_print = fps[j].split(","); //Splitting BSSID and LEVEL
                fingerprintlocal[j] = new FingerPrintElement(single_print[0], Double.parseDouble(single_print[1]));
            }

            boolean found = false;

            for(int k = 0; k < fingerprintlocal.length; k++) {
                for(int l = 0; l < resPrint.size(); l++) {
                    //finding matching bssids
                    if(resPrint.get(l).getBssid().equals(fingerprintlocal[k].getBssid())) {
                        resPrint.get(l).setLevel(resPrint.get(l).getLevel() + fingerprintlocal[k].getLevel());
                        found = true;
                    }
                }
                if (!found) {
                    resPrint.add(new FingerPrintElement(fingerprintlocal[k].getBssid(), fingerprintlocal[k].getLevel()));
                }
                found = false;
            }

        }

        FingerPrintElement[] res = new FingerPrintElement[resPrint.size()];

        for (int i = 0; i < resPrint.size(); i++) {
            res[i] = new FingerPrintElement(resPrint.get(i).getBssid(), resPrint.get(i).getLevel() / devider);
        }

        return res;
    }

}
