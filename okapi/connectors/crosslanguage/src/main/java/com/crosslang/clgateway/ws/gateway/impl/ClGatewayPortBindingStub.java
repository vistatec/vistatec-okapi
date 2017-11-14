/**
 * ClGatewayPortBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.crosslang.clgateway.ws.gateway.impl;

@SuppressWarnings("rawtypes")
public class ClGatewayPortBindingStub extends org.apache.axis.client.Stub implements com.crosslang.gateway.clgateway.CLGateway {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("translateSentence");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "apiKey"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "requestTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "secret"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "sentence"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "UserRequestException"),
                      "com.crosslang.gateway.clgateway.UserRequestExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "PermanentServerException"),
                      "com.crosslang.gateway.clgateway.PermanentServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "TemporaryServerException"),
                      "com.crosslang.gateway.clgateway.TemporaryServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("translateFile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "apiKey"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "requestTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "secret"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "base64content"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "fileType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "base64Response"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "UserRequestException"),
                      "com.crosslang.gateway.clgateway.UserRequestExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "PermanentServerException"),
                      "com.crosslang.gateway.clgateway.PermanentServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "TemporaryServerException"),
                      "com.crosslang.gateway.clgateway.TemporaryServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("translateFileAsync");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "apiKey"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "requestTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "secret"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "base64content"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "fileType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asyncIdentifier"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "estimatedEndTime"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"), java.util.Calendar.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "UserRequestException"),
                      "com.crosslang.gateway.clgateway.UserRequestExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "PermanentServerException"),
                      "com.crosslang.gateway.clgateway.PermanentServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "TemporaryServerException"),
                      "com.crosslang.gateway.clgateway.TemporaryServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkFileAsync");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "apiKey"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "requestTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "secret"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asyncIdentifier"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "estimatedEndTime"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"), java.util.Calendar.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "finished"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), java.lang.Boolean.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "base64Response"), org.apache.axis.description.ParameterDesc.OUT, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "UserRequestException"),
                      "com.crosslang.gateway.clgateway.UserRequestExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "PermanentServerException"),
                      "com.crosslang.gateway.clgateway.PermanentServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "TemporaryServerException"),
                      "com.crosslang.gateway.clgateway.TemporaryServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteAllFileAsync");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "apiKey"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "requestTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "secret"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(java.lang.Boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "succeed"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "UserRequestException"),
                      "com.crosslang.gateway.clgateway.UserRequestExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "PermanentServerException"),
                      "com.crosslang.gateway.clgateway.PermanentServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "TemporaryServerException"),
                      "com.crosslang.gateway.clgateway.TemporaryServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteFileAsync");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "apiKey"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "requestTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "secret"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asyncIdentifier"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(java.lang.Boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "succeed"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "UserRequestException"),
                      "com.crosslang.gateway.clgateway.UserRequestExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "PermanentServerException"),
                      "com.crosslang.gateway.clgateway.PermanentServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "TemporaryServerException"),
                      "com.crosslang.gateway.clgateway.TemporaryServerExceptionBean",
                      new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean"), 
                      true
                     ));
        _operations[5] = oper;

    }

    public ClGatewayPortBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ClGatewayPortBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    @SuppressWarnings("unchecked")
	public ClGatewayPortBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
//            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
//            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
//            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
//            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
//            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
//            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
//            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
//            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
//            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "permanentServerExceptionBean");
            cachedSerQNames.add(qName);
            cls = com.crosslang.gateway.clgateway.PermanentServerExceptionBean.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "temporaryServerExceptionBean");
            cachedSerQNames.add(qName);
            cls = com.crosslang.gateway.clgateway.TemporaryServerExceptionBean.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "userRequestExceptionBean");
            cachedSerQNames.add(qName);
            cls = com.crosslang.gateway.clgateway.UserRequestExceptionBean.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public java.lang.String translateSentence(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String sentence) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "translateSentence"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {apiKey, username, requestTime, secret, sentence});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.UserRequestExceptionBean) {
              throw (com.crosslang.gateway.clgateway.UserRequestExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.PermanentServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.PermanentServerExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public java.lang.String translateFile(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String base64Content, java.lang.String fileType) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "translateFile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {apiKey, username, requestTime, secret, base64Content, fileType});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.UserRequestExceptionBean) {
              throw (com.crosslang.gateway.clgateway.UserRequestExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.PermanentServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.PermanentServerExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void translateFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String base64Content, java.lang.String fileType, javax.xml.rpc.holders.StringHolder asyncIdentifier, javax.xml.rpc.holders.CalendarHolder estimatedEndTime) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "translateFileAsync"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {apiKey, username, requestTime, secret, base64Content, fileType});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                asyncIdentifier.value = (java.lang.String) _output.get(new javax.xml.namespace.QName("", "asyncIdentifier"));
            } catch (java.lang.Exception _exception) {
                asyncIdentifier.value = (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("", "asyncIdentifier")), java.lang.String.class);
            }
            try {
                estimatedEndTime.value = (java.util.Calendar) _output.get(new javax.xml.namespace.QName("", "estimatedEndTime"));
            } catch (java.lang.Exception _exception) {
                estimatedEndTime.value = (java.util.Calendar) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("", "estimatedEndTime")), java.util.Calendar.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.UserRequestExceptionBean) {
              throw (com.crosslang.gateway.clgateway.UserRequestExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.PermanentServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.PermanentServerExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void checkFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String asyncIdentifier, javax.xml.rpc.holders.CalendarHolder estimatedEndTime, javax.xml.rpc.holders.BooleanWrapperHolder finished, javax.xml.rpc.holders.StringHolder base64Response) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "checkFileAsync"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {apiKey, username, requestTime, secret, asyncIdentifier});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                estimatedEndTime.value = (java.util.Calendar) _output.get(new javax.xml.namespace.QName("", "estimatedEndTime"));
            } catch (java.lang.Exception _exception) {
                estimatedEndTime.value = (java.util.Calendar) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("", "estimatedEndTime")), java.util.Calendar.class);
            }
            try {
                finished.value = (java.lang.Boolean) _output.get(new javax.xml.namespace.QName("", "finished"));
            } catch (java.lang.Exception _exception) {
                finished.value = (java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("", "finished")), java.lang.Boolean.class);
            }
            try {
                base64Response.value = (java.lang.String) _output.get(new javax.xml.namespace.QName("", "base64Response"));
            } catch (java.lang.Exception _exception) {
                base64Response.value = (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("", "base64Response")), java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.UserRequestExceptionBean) {
              throw (com.crosslang.gateway.clgateway.UserRequestExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.PermanentServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.PermanentServerExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public java.lang.Boolean deleteAllFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "deleteAllFileAsync"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {apiKey, username, requestTime, secret});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.Boolean) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.Boolean.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.UserRequestExceptionBean) {
              throw (com.crosslang.gateway.clgateway.UserRequestExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.PermanentServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.PermanentServerExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public java.lang.Boolean deleteFileAsync(java.lang.String apiKey, java.lang.String username, java.lang.String requestTime, java.lang.String secret, java.lang.String asyncIdentifier) throws java.rmi.RemoteException, com.crosslang.gateway.clgateway.UserRequestExceptionBean, com.crosslang.gateway.clgateway.PermanentServerExceptionBean, com.crosslang.gateway.clgateway.TemporaryServerExceptionBean {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gateway.crosslang.com/clgateway", "deleteFileAsync"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {apiKey, username, requestTime, secret, asyncIdentifier});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.Boolean) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.Boolean.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.UserRequestExceptionBean) {
              throw (com.crosslang.gateway.clgateway.UserRequestExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.PermanentServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.PermanentServerExceptionBean) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) {
              throw (com.crosslang.gateway.clgateway.TemporaryServerExceptionBean) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
