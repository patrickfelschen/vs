package de.hsos.vs;


import org.json.JSONObject;

/**
 * Ein BillBoard Eintrag.
 *
 * @author heikerli
 */
public class BillBoardEntry {

    int id;
    String text;
    String owner_ip;
    long timestamp;

    public BillBoardEntry(int i, String text, String caller_ip) {
        id = i;
        this.text = text;
        owner_ip = caller_ip;
        setTimeStamp();
    }

    public long getTimeStamp() {
        return timestamp;
    }

    /* Wird bei Modifikationen und Erzeugung aufgerufen */
    public void setTimeStamp() {
        timestamp = System.currentTimeMillis();
    }

    public void reset() {
        this.text = "<empty>";
        owner_ip = "<not set>";
        timestamp = 0;
    }

    public boolean belongsToCaller(String caller_ip) {
        if (caller_ip.equals(this.owner_ip)) {
            return true;
        }
        return false;
    }

    public JSONObject toJson() {
        return  new JSONObject()
                .put("id", this.id)
                .put("text", this.text)
                .put("owner_ip", this.owner_ip)
                .put("timestamp", this.timestamp);
    }
}
