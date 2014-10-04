package nl.moj.security;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import nl.moj.model.Assignment;
import nl.moj.model.Tester;
import nl.moj.model.Tester.SecurityDelegate;

/**
 * MOJSecurityManager reroutes all calls to a security manager to the check method which checks if the current calling Thread is part of the ThreadGroup specified at startup. If so, a
 * SecurityException is thrown.
 */

public class SandboxSecurityManager extends SecurityManager
{

   private static Logger log = Logger.getLogger("Sandbox");

   private ThreadGroup myUntrusted;
   //private ThreadLocal isChecking=new ThreadLocal();
   private Map<String, Tester.SecurityDelegate> securityDelegates;
   private Map<String, ThreadGroup> threadGroups;

   public SandboxSecurityManager(ThreadGroup untrusted)
   {
      super();
      myUntrusted = untrusted;
      securityDelegates = new HashMap<String, SecurityDelegate>();
      threadGroups = new HashMap<String, ThreadGroup>();
   }

   public ThreadGroup getEvilThreadGroupRoot()
   {
      return myUntrusted;
   }

   /**
    * registers an assignment with the SecurityManager. The name is used as a key to store the 'evil' ThreadGroup and the delegate. Multiple assignments registered reuse the same threadgroup and
    * delegate.
    * 
    * @param assignment the assignment to register.
    */
   public synchronized void registerAssignment(Assignment assignment)
   {
      String tgName = assignment.getName() + "-securityCtx";
      if (securityDelegates.containsKey(tgName))
         return;
      //
      ThreadGroup assignmentThreadGroup = new ThreadGroup(myUntrusted, tgName);
      securityDelegates.put(tgName, assignment.getSecurityDelegate());
      threadGroups.put(tgName, assignmentThreadGroup);
   }

   public synchronized ThreadGroup getEvilThreadGroup(Assignment assignment)
   {
      String tgName = assignment.getName() + "-securityCtx";
      ThreadGroup tg = threadGroups.get(tgName);
      if (tg == null)
      {
         log.warning("No ThreadGroup set for '" + tgName + "'. Using root.");
         tg = myUntrusted;
      }
      return tg;
   }

   protected synchronized Tester.SecurityDelegate getSecurityDelegate(ThreadGroup tg)
   {
      String name = tg.getName();
      Tester.SecurityDelegate tsd = securityDelegates.get(name);
      if (tsd == null)
      {
         log.warning("No SecurityDelegate set for '" + name + "'. Using default.");
         tsd = new DefaultSecurityDelegate();
      }
      return tsd;
   }

   /**
    * Standard the Security Manager only checks modification for root groups. Obviously we dont want any untrusted threads to access other groups, because the security mechanism is based on untrusted
    * threads in a specific group.
    */
   public void checkAccess(ThreadGroup g)
   {
      //
      if (g == null)
         throw new NullPointerException("NULL ThreadGroup.");
      // Allow access of one's self.
      if (g.equals(myUntrusted))
         return;
      // Dont allow access to other groups.
      if (Thread.currentThread().getThreadGroup() == myUntrusted)
         throw new SecurityException("Not allowed to modify ThreadGroups");
      //		 
   }

   public void checkPermission(Permission perm)
   {
      check(perm, null);
   }

   public void checkPermission(Permission perm, Object context)
   {
      check(perm, context);
   }

   private void check(Permission perm, Object context)
   {
      boolean evil = false;
      //
      // Get the threads in the untrusted group.
      //
      Thread[] tr = new Thread[myUntrusted.activeCount()];
      myUntrusted.enumerate(tr, true);
      //
      // Get the name of the current thread.
      //
      String current = Thread.currentThread().getName();
      //
      // Check if it in there, and if so, declare this thread as untrustworthy.
      //
      for (int t = 0; t < tr.length; t++)
      {
         if (tr[t] == null)
            continue;
         if (tr[t].getName().equals(current))
            evil = true;
      }
      //
      if (evil)
      {
         // 
         // delegate to the assignment to evaluate the permission.
         // 
         getSecurityDelegate(Thread.currentThread().getThreadGroup()).checkPermission(perm, context);
         //
      }
      //
   }

}
