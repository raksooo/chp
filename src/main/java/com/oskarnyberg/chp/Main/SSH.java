package com.oskarnyberg.chp.Main;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SSH implements HostKeyVerifier {

	private final SSHClient sshClient;

	private final SSHListener listener;
	private final List<SSHListener> listeners;

	private final String host;
	private final String username;
	private final String password;

	public SSH(String host, String username, String password) {
		Logger.getRootLogger().addAppender(new NullAppender());

		listener = new SSHListenerImpl();
		listeners = new ArrayList<>();

		sshClient = new SSHClient();
		sshClient.addHostKeyVerifier(this);

		this.host = host;
		this.username = username;
		this.password = password;

		connect();
	}

	private void connect() {
		try {
			sshClient.connect(host);
			try {
				sshClient.authPassword(username, password);
			} catch (IOException e) {
				listener.authorizationFailed();
				disconnect();
			} catch (IllegalStateException e) {
				listener.connectionFailed();
				disconnect();
			}
		} catch (IOException e) {
			listener.connectionFailed();
		}
	}

	public SSHClient getSSHClient() {
		return sshClient;
	}

	public void execute(String command) {
		try {
			Session session = sshClient.startSession();
            try {
                final Session.Command cmd = session.exec(command);
                try {
                    cmd.join(1, TimeUnit.SECONDS);
                    try {
                        session.close();
                    } catch (IOException e) {
                        listener.executionFailed("Could not close session (Print should start anyways)");
                    }
                } catch (IOException e) {
                    listener.executionFailed("Could not join execution (Print should start anyways)");
                }
            } catch (IOException e) {
                listener.executionFailed("Could not execute command");
            }
		} catch (IOException e) {
			listener.executionFailed("Could not start ssh session");
		}
	}

	public void disconnect() {
		try {
			sshClient.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addListener(SSHListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SSHListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean verify(String s, int i, PublicKey publicKey) {
		return true;
	}

	private class SSHListenerImpl implements SSHListener {

		@Override
		public void connectionFailed() {
			for (SSHListener listener : listeners) {
				listener.connectionFailed();
			}
		}

		@Override
		public void authorizationFailed() {
			for (SSHListener listener : listeners) {
				listener.authorizationFailed();
			}
		}

		@Override
		public void executionFailed(String error) {
			for (SSHListener listener : listeners) {
				listener.executionFailed(error);
			}
		}
	}
}
