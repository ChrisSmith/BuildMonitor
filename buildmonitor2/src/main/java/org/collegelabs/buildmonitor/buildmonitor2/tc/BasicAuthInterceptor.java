package org.collegelabs.buildmonitor.buildmonitor2.tc;

import retrofit.RequestInterceptor;

/**
*/
public class BasicAuthInterceptor implements retrofit.RequestInterceptor {
    private String authHeader;

    public BasicAuthInterceptor(String authHeader){
        this.authHeader = authHeader;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", authHeader);
        request.addHeader("Accept", "application/json");
    }
}
