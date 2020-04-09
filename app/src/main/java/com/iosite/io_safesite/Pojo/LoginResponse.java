package com.iosite.io_safesite.Pojo;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    public Access access;
    public String comet_auth;
    public String comet_appid;


    public class Access implements Serializable {
        public String token;
    }

}
