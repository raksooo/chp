package com.oskarnyberg.chp.Main;

public interface SSHListener {

	public void connectionFailed();

	public void authorizationFailed();

	public void executionFailed(String error);
}
