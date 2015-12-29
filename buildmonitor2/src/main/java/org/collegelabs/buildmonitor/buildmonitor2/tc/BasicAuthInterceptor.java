package org.collegelabs.buildmonitor.buildmonitor2.tc;

import retrofit.RequestInterceptor;

/**
*/
public class BasicAuthInterceptor extends  JsonHeaderInterceptor {
    private String authHeader;

    public BasicAuthInterceptor(String authHeader){
        this.authHeader = authHeader;
    }

    @Override
    public void intercept(RequestFacade request) {
        super.intercept(request); //JSON header
        request.addHeader("Authorization", authHeader);
    }
}
