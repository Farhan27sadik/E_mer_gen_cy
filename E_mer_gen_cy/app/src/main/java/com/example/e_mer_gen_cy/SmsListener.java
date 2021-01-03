package com.example.e_mer_gen_cy;


public interface SmsListener
{
    public void messageReceived(String messageText, String sender);
}