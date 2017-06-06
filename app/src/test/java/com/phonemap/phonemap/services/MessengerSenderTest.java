package com.phonemap.phonemap.services;


import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.phonemap.phonemap.wrapper.MessengerSender;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MessengerSenderTest {
    private Message mockMessage;
    private Messenger mockMessenger;
    private MessengerSender messengerSender;

    @Before
    public void setup() {
        mockMessage = mock(Message.class);
        mockMessenger = mock(Messenger.class);
        messengerSender = new MessengerSender(mockMessenger).setMessage(mockMessage);
    }

    @Test
    public void canSetData() {
        Bundle mockBundle = mock(Bundle.class);
        messengerSender.setData(mockBundle);
        verify(mockMessage, times(1)).setData(mockBundle);
    }

    @Test
    public void canSendMessage() throws RemoteException {
        messengerSender.send();
        verify(mockMessenger, times(1)).send(mockMessage);
    }
}
