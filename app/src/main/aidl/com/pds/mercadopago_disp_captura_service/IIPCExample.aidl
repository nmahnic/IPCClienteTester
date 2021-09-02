// IIPCExample.aidl
package com.pds.mercadopago_disp_captura_service;

// Declare any non-default types here with import statements

interface IIPCExample {
    /** Request the process ID of this service */
    int getPid();

    /** Count of received connection requests from clients */
    int getConnectionCount();

    /** Set displayed value of screen */
    void setDisplayedValue(String packageName, int pid, String data);

    int getPaymentStatus(String extRef);

    int createSaleIntent(double amount, String orderId, String androidID);

    int cancelSaleIntent(String extRef);
}