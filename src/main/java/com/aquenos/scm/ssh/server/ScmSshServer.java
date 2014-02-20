/*
 * Copyright 2012 aquenos GmbH.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package com.aquenos.scm.ssh.server;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.shiro.subject.Subject;
//import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.Session.AttributeKey;
import org.apache.sshd.server.Command;
//import org.apache.sshd.server.CommandFactory;
//import org.apache.sshd.server.PasswordAuthenticator;
//import org.apache.sshd.server.PublickeyAuthenticator;
//import org.apache.sshd.server.session.ServerSession;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.*;
/**
 * SSH server for SCM.
 * 
 * @author Sebastian Marsching
 */
@Singleton
public class ScmSshServer {

	/**
	 * Key used for storing a {@link Subject} in a {@link ServerSession}.
	 */
	public final static AttributeKey<Subject> SUBJECT_SESSION_ATTRIBUTE_KEY = new AttributeKey<Subject>();

	//private SshServer sshServer;

	/**
	 * Constructor. Meant to be called by Guice.
	 * 
	 * @param passwordAuthenticator
	 *            authenticator used for password authentication.
	 * @param publickeyAuthenticator
	 *            authenticator used for public-key authentication.
	 * @param commandFactory
	 *            factory used to create SSH commands.
	 * @param configStore
	 *            store the the SSH server configuration.
	 * @param keyPairProvider
	 *            provider for SSH host keys.
	 */
	
	@Inject
	public ScmSshServer(ScmSshServerConfigurationStore configStore,
			ScmKeyPairProvider keyPairProvider
			) {
		ScmSshServerConfiguration config = configStore.load();
		System.out.println(config.toString());
		if (config == null) {
			config = new ScmSshServerConfiguration();
		}
		//sshServer = SshServer.setUpDefaultServer();
		String listenAddress = config.getListenAddress();
		//if (listenAddress != null && !listenAddress.trim().isEmpty()) {
	//		sshServer.setHost(config.getListenAddress());
	//	}
		
		//sshServer.setPort(config.getListenPort());
		//sshServer.setKeyPairProvider(keyPairProvider);
		//sshServer.setPasswordAuthenticator(passwordAuthenticator);
		//sshServer.setPublickeyAuthenticator(publickeyAuthenticator);
		//sshServer.setCommandFactory(commandFactory);
		//sshServer.setShellFactory(new NoShellCommandFactory());
	}

	/**
	 * Starts the SSH server in a thread of its own.
	 */
	public void start() {
		try {
			//sshServer.start();
			//USocketFactory udsf = new JUnixDomainSocketFactory();
		      //USocketFactory udsf = new NCUSocketFactory();
		      USocketFactory udsf = new JNAUSocketFactory();
		      AgentProxy ap = new AgentProxy(new SSHAgentConnector(udsf));


		      Identity[] identities = ap.getIdentities();
		      System.out.println("+++++++++++++++++++++++++++++");
		      System.out.println(identities);
		System.out.println("+++++++++++++++++++++++++++++");
		      System.out.println("count: "+identities.length);
		     System.out.println("+++++++++++++++++++++++++++++");
		      for(int i=0; i<identities.length; i++){
		        System.out.println("  comment: "+
		                           new String(identities[i].getComment()));
		         
		        byte[] blob = identities[i].getBlob();
		System.out.println("+++++++++++++++++++++++++++++");
		        System.out.print("  blob: ");
		        for(int j=0; j<blob.length; j++){
		          System.out.print(Integer.toHexString(blob[j]&0xff)+":");
		        }
		        System.out.println("");
		         System.out.println("+++++++++++++++++++++++++++++");
		        String data = "foo";
		        byte[] signed = ap.sign(blob, data.getBytes());
		        System.out.print("  sign: "+data+" -> ");
		        for(int j=0; j<signed.length; j++){
		          System.out.print(Integer.toHexString(signed[j]&0xff)+":");
		        }
		        System.out.println("");
		      }
		} 
		catch(AgentProxyException e){
		      System.out.println(e);
		    }
	}

	/**
	 * Stops the SSH server. This method will block until the SSH server has
	 * been stopped.
	 */
	public void stop() {
		//try {
		    //sshServer.stop();
		//} catch (InterruptedException e) {
		//	Thread.currentThread().interrupt();
		//}
	}

	/**
	 * Simple command factory that creates a command which signals the client
	 * that no shell is available.
	 */
	private class NoShellCommandFactory implements Factory<Command> {

		@Override
		public Command create() {
			return new AbstractCommand() {
				@Override
				protected int run() {
					try {
						PrintStream printStream = null;
						if (getErrorStream() != null) {
							printStream = new PrintStream(getErrorStream(),
									true, "UTF-8");
						} else if (getOutputStream() != null) {
							printStream = new PrintStream(getOutputStream(),
									true, "UTF-8");
						}
						if (printStream != null) {
							printStream.println("This is not a shell.");
						}
					} catch (UnsupportedEncodingException e) {
						// Ignore an unsupported-encoding exception. We just do
						// not write anything.
					}
					return 1;
				}
			};
		}

	}

}
