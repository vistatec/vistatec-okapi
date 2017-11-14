/**
 * CLGateway.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.crosslang.gateway.clgateway;

public interface CLGateway extends java.rmi.Remote {
    public java.lang.String translateSentence(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String sentence) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean;
    public java.lang.String translateFile(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String base64Content, java.lang.String fileType) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean;
    public void translateFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String base64Content, java.lang.String fileType, javax.xml.rpc.holders.StringHolder asyncIdentifier, javax.xml.rpc.holders.CalendarHolder estimatedEndTime) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean;
    public void checkFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String asyncIdentifier, javax.xml.rpc.holders.CalendarHolder estimatedEndTime, javax.xml.rpc.holders.BooleanWrapperHolder finished, javax.xml.rpc.holders.StringHolder base64Response) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean;
    public java.lang.Boolean deleteAllFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean;
    public java.lang.Boolean deleteFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String asyncIdentifier) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean;
}
