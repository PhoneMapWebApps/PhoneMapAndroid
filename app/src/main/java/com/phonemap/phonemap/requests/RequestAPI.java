package com.phonemap.phonemap.requests;

public class RequestAPI {
    private ServerListener serverListenerContext;

    public RequestAPI(ServerListener context){
        this.serverListenerContext = context;
    }

    public void getTasks(){
        new GetTasks(serverListenerContext);
    }
}
