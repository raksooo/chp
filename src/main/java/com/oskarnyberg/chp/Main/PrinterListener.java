package com.oskarnyberg.chp.Main;

public interface PrinterListener extends SSHListener {

	public void uploadFailed();

	public void connecting();

	public void noPrinterSpecified();

	public void fileNotFound();

	public void noCidSet();

	public void noPasswordSet();

	public void invalidPrinterIndex();

	public void printSucceeded();

	public void uploading();

	public void sendingPrintCommand();

    public void started(String file);
}
