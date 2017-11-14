/**
 * ClGatewayLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.crosslang.clgateway.ws.gateway.impl;

public class ClGatewayLocator extends org.apache.axis.client.Service implements com.crosslang.clgateway.ws.gateway.impl.ClGateway {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3466227772860955420L;

	public ClGatewayLocator() {
    }


    public ClGatewayLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ClGatewayLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for clGatewayPort
    private java.lang.String clGatewayPort_address = "http://gateway.crosslang.com:8080/services/clGateway";

    public java.lang.String getclGatewayPortAddress() {
        return clGatewayPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String clGatewayPortWSDDServiceName = "clGatewayPort";

    public java.lang.String getclGatewayPortWSDDServiceName() {
        return clGatewayPortWSDDServiceName;
    }

    public void setclGatewayPortWSDDServiceName(java.lang.String name) {
        clGatewayPortWSDDServiceName = name;
    }

    public com.crosslang.gateway.clgateway.CLGateway getclGatewayPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(clGatewayPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getclGatewayPort(endpoint);
    }

    public com.crosslang.gateway.clgateway.CLGateway getclGatewayPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.crosslang.clgateway.ws.gateway.impl.ClGatewayPortBindingStub _stub = new com.crosslang.clgateway.ws.gateway.impl.ClGatewayPortBindingStub(portAddress, this);
            _stub.setPortName(getclGatewayPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setclGatewayPortEndpointAddress(java.lang.String address) {
        clGatewayPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
	@SuppressWarnings("rawtypes")
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.crosslang.gateway.clgateway.CLGateway.class.isAssignableFrom(serviceEndpointInterface)) {
                com.crosslang.clgateway.ws.gateway.impl.ClGatewayPortBindingStub _stub = new com.crosslang.clgateway.ws.gateway.impl.ClGatewayPortBindingStub(new java.net.URL(clGatewayPort_address), this);
                _stub.setPortName(getclGatewayPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @SuppressWarnings("rawtypes")
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("clGatewayPort".equals(inputPortName)) {
            return getclGatewayPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://impl.gateway.ws.clgateway.crosslang.com/", "clGateway");
    }

    @SuppressWarnings("rawtypes")
	private java.util.HashSet ports = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://impl.gateway.ws.clgateway.crosslang.com/", "clGatewayPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("clGatewayPort".equals(portName)) {
            setclGatewayPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
