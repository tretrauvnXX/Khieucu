package vn.com.sonhasg.dynamiccalendar;

/**
 * Created by nguyenphuoc on 25-04-2017.
 */

public class EventModel {

    private String strDate;
    private String strGps;
    private String strGpsOk;
    private String strGpsError;


    public EventModel(String strDate, String strGps, String strGpsOk, String strGpsError) {
        this.strDate = strDate;
        this.strGps = strGps;
        this.strGpsOk = strGpsOk;
        this.strGpsError = strGpsError;
    }

    public String getStrDate() {
        return strDate;
    }

    public String getStrGps() {
        return strGps;
    }

    public String getStrGpsOk() {
        return strGpsOk;
    }

    public String getStrGpsError() {
        return strGpsError;
    }
}
