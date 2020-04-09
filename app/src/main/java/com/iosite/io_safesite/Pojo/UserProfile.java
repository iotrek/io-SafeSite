package com.iosite.io_safesite.Pojo;

import java.io.Serializable;
import java.util.ArrayList;

public class UserProfile implements Serializable {
    public String _id;
    public UserAddress address;
    public String age_group;
    public String covid19_status;
    public String first_name;
    public String last_name;
    public String gender;
    public String mobile;
    public String role;
    public String quarantine_date;
    public ArrayList<UserTravelHistory> travel_history;

}
