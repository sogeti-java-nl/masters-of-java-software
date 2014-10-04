package nl.moj.workspace.server;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.ctrlaltdev.net.server.Log;
import nl.ctrlaltdev.net.server.SimpleSocketServer;
import nl.ctrlaltdev.net.server.SocketHandlerFactory;
import nl.ctrlaltdev.util.SimpleLogFormatter;
import nl.moj.process.ProcessPool;
import nl.moj.security.SandboxSecurityManager;
import nl.moj.workspace.WorkspaceClientServerImpl;

/**
 * Workspace Server : Separate server for MoJ workspaces. Increases scalability of MoJ, reduces
 * influence of buggy/evil code to the game environment. Makes workspace accessible for other
 * processes. Increases latency of Operations.
 *    
 * (C) 2004-2007 E.Hooijmeijer / 42 B.V. -  http://www.2en40.nl and or http://www.ctrl-alt-dev.nl
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

public class WorkspaceServer implements SocketHandlerFactory {

	private SimpleSocketServer mySSS;
	private ProcessPool pool;
	private ApplicationBuilder builder=new ApplicationBuilder();

	public WorkspaceServer(ThreadGroup group,int poolsize) {
		this(group,Integer.parseInt(System.getProperty("MOJ.WORKSPACESERVER.PORT","8081")),poolsize);
	}
	
	public WorkspaceServer(ThreadGroup group,int port,int poolsize) {
		try {
			pool=new ProcessPool(poolsize);
			mySSS=new SimpleSocketServer(port,new Log.JavaUtilLoggerAdapter("WorkspaceServer"),this);
		} catch (IOException ex) {
			Logger.getLogger("").log(Level.SEVERE,"Failed opening server socket."+ex);
			throw new RuntimeException("Failed starting Server.");
		}		
		//
		new Thread(group,mySSS).start();
		//
	}
	
	public Runnable createHandler(Socket s, Log l) {
		//
		return new WorkspaceMessageHandler(s,l,new WorkspaceClientServerImpl(pool,builder));
		//
	}	
	
	public static void main(String[] args) throws IOException {
		//
		int port=(args.length>0?Integer.parseInt(args[0]):8081);
		int poolsize=(args.length>1?Integer.parseInt(args[1]):32);
		//
		SimpleLogFormatter.clearLogConfig();
		SimpleLogFormatter.addConsoleLogging();
		SimpleLogFormatter.addFileLogging("./WorkspaceServer%u.log");
        SimpleLogFormatter.verbose();
        SimpleLogFormatter.info("java");
        SimpleLogFormatter.info("javax");
        SimpleLogFormatter.info("sun");		
		//
		// Force load awt.dll / libawt.so so we'll wont get any
		// security exceptions when some assignment tries to use it..
		//
		new Color(255,255,255);
		//
		ThreadGroup tmp=new ThreadGroup("Tester-ThreadGroup");
		SandboxSecurityManager ssm=new SandboxSecurityManager(tmp);	
		System.setSecurityManager(ssm);				
		//
		Logger.getLogger("").info("Starting Workspace Server.");
		//
		ThreadGroup group=new ThreadGroup("Server");
		new WorkspaceServer(group,port,poolsize);
		//		
	}

}
