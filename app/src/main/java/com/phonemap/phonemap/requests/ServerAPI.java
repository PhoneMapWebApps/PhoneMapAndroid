package com.phonemap.phonemap.requests;

public class ServerAPI {
    private ServerListener serverListenerContext;

    public ServerAPI(ServerListener context){
        this.serverListenerContext = context;
    }

    public void getTasks(){
        new GetTasks(serverListenerContext);
    }
}
