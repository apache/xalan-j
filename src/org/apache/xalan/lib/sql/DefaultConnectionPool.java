/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.lib.sql;

import java.util.Properties;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;

import java.lang.String;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

public class DefaultConnectionPool implements ConnectionPool
{
  private final static boolean DEBUG = false;

  /**
   * The basic information to make a JDBC Connection
   */
  private String      m_driver = new String("");
  private String      m_url = new String("");


  /**
   * The mimimum size of the connection pool, if the
   * number of available connections falls below this
   * mark, min connections will be allocated. The Connection
   * Pool will always be somewhere between MinSize and MinSize*2
   *
   */
  private int         m_PoolMinSize = 1;


  /**
   * Always implement the properties mechinism, if the Password
   * or Username is set seperatly then we will add them to the
   * property manually.
   *
   */
  private Properties  m_ConnectionProtocol = new Properties();

  /**
   * Storage for the PooledConnections
   */
  private Vector      m_pool = null;

  /**
   * Are we active ??
   */
  private boolean     m_IsActive = false;

  public DefaultConnectionPool() {}

  /**
   * Are we active, if not then released connections will be
   * closed on release and new connections will be refused.
   *
   * @param <code>boolean flag</code>, Set the active flag.
   */
  public void setActive(boolean flag)
  {
    m_IsActive = flag;
  }

  /**
   * Return our current Active state
   */
  public boolean getActive()
  {
    return m_IsActive;
  }

  /**
   * Set the driver call to be used to create connections
   */
  public void setDriver(String d)
  {
    m_driver = d;
  }

  /**
   * Set the url used to connect to the database
   */
  public void setURL(String url)
  {
    m_url = url;
  }

  /**
   * Go through the connection pool and release any connections
   * that are not InUse;
   *
   */
  public void freeUnused()
  {
    // Iterate over the entire pool closing the
    // JDBC Connections.
    for ( int x = 0; x < m_pool.size(); x++ )
    {


      PooledConnection pcon =
        (PooledConnection) m_pool.elementAt(x);

      // If the PooledConnection is not in use, close it
      if ( pcon.inUse() == false )
      {
        if (DEBUG)
        {
          System.err.println("Closing JDBC Connection " + x);
        }

        pcon.close();
      }
    }

  }

  /**
   * Is our ConnectionPool have any connections that are still in Use ??
   */
  public boolean hasActiveConnections()
  {
    return (m_pool.size() > 0);
  }


  /**
   * Set the password in the property set.
   */
  public void setPassword(String p)
  {
    m_ConnectionProtocol.setProperty("password", p);
  }

  /**
   * Set the user name in the property set
   */
  public void setUser(String u)
  {
    m_ConnectionProtocol.setProperty("user", u);
  }

  /**
   * Copy the properties from the source to our properties
   */
  public void setProtocol(Properties p)
  {
    Set s = p.keySet();
    Iterator i = s.iterator();

    while (i.hasNext())
    {
      String key = (String) i.next();
      m_ConnectionProtocol.setProperty(key, p.getProperty(key));
    }

  }

  /**
   * Override the current number of connections to keep in the pool. This
   * setting will only have effect on a new pool or when a new connection
   * is requested and there is less connections that this setting.
   */
  public void setMinConnections(int n)
  {
    m_PoolMinSize = n;
  }

  /**
   * Try to aquire a new connection, if it succeeds then return
   * true, else return false.
   * Note: This method will cause the connection pool to be built.
   *
   */
  public boolean testConnection()
  {
    try
    {
      Connection conn = getConnection();

      if (DEBUG)
      {
        DatabaseMetaData dma = conn.getMetaData();

        System.out.println("\nConnected to " + dma.getURL());
        System.out.println("Driver   " + dma.getDriverName());
        System.out.println("Version  " + dma.getDriverVersion());
        System.out.println("");
      }

      if (conn == null) return false;

      releaseConnection(conn);

      return true;
    }
    catch(Exception e)
    {
      return false;
    }

  }


  // Find an available connection
  public synchronized Connection getConnection()
    throws IllegalArgumentException, SQLException
  {

    PooledConnection pcon = null;

    if (m_pool == null) { initializePool(); }

    // find a connection not in use
    for ( int x = 0; x < m_pool.size(); x++ )
    {

      pcon = (PooledConnection) m_pool.elementAt(x);

      // Check to see if the Connection is in use
      if ( pcon.inUse() == false )
      {
        // Mark it as in use
        pcon.setInUse(true);
        // return the JDBC Connection stored in the
        // PooledConnection object
        return pcon.getConnection();
      }
    }

    // Could not find a free connection,
    // create and add a new one

    // Create a new JDBC Connection
    Connection con = createConnection();

    // Create a new PooledConnection, passing it the JDBC
    // Connection
    pcon = new PooledConnection(con);

    // Mark the connection as in use
    pcon.setInUse(true);

    // Add the new PooledConnection object to the pool
    m_pool.addElement(pcon);

    // return the new Connection
    return pcon.getConnection();
  }

  public synchronized void releaseConnection(Connection con)
    throws SQLException
  {

    // find the PooledConnection Object
    for ( int x = 0; x < m_pool.size(); x++ )
    {

      PooledConnection pcon =
        (PooledConnection) m_pool.elementAt(x);

      // Check for correct Connection
      if ( pcon.getConnection() == con )
      {
        if (DEBUG)
        {
          System.out.println("Releasing Connection " + x);
        }

        if (getActive() == false)
        {
          con.close();
          m_pool.remove(x);
          if (DEBUG)
          {
            System.out.println("-->Inactive Pool, Closing connection");
          }

        }
        else
        {
          // Set it's inuse attribute to false, which
          // releases it for use
          pcon.setInUse(false);
        }

        break;
      }
    }
  }



  private Connection createConnection()
    throws SQLException
  {
    Connection con = null;

    // Create a Connection
    con = DriverManager.getConnection( m_url, m_ConnectionProtocol );

    return con;
  }

  // Initialize the pool
  public synchronized void initializePool()
    throws IllegalArgumentException, SQLException
  {
     // Check our initial values
     if ( m_driver == null )
     {
       throw new IllegalArgumentException("No Driver Name Specified!");
     }

     if ( m_url == null )
     {
       throw new IllegalArgumentException("No URL Specified!");
     }

     if ( m_PoolMinSize < 1 )
     {
       throw new IllegalArgumentException("Pool size is less than 1!");
     }

     // Create the Connections
     // Load the Driver class file

     try
     {
       Class.forName( m_driver );
     }
     catch(ClassNotFoundException e)
     {
       throw new IllegalArgumentException("Invalid Driver Name Specified!");
     }


    // Create Connections based on the size member
    for ( int x = 0; x < m_PoolMinSize; x++ )
    {

      Connection con = createConnection();

      if ( con != null )
      {

        // Create a PooledConnection to encapsulate the
        // real JDBC Connection
        PooledConnection pcon = new PooledConnection(con);

        // Add the Connection the pool.
        addConnection(pcon);

        if (DEBUG) System.out.println("Adding DB Connection to the Pool");
      }
    }
  }

  // Adds the PooledConnection to the pool
  private void addConnection(PooledConnection value)
  {

    // If the pool is null, create a new vector
    // with the initial size of "size"
    if ( m_pool == null )
    {
      m_pool = new Vector( m_PoolMinSize);
    }

    // Add the PooledConnection Object to the vector
    m_pool.addElement(value);
  }


  protected void finalize()
    throws Throwable
  {
    if (DEBUG)
    {
      System.out.println("In Default Connection Pool, Finalize");
    }

    // Iterate over the entire pool closing the
    // JDBC Connections.
    for ( int x = 0; x < m_pool.size(); x++ )
    {

      if (DEBUG)
      {
        System.out.println("Closing JDBC Connection " + x);
      }

      PooledConnection pcon =
        (PooledConnection) m_pool.elementAt(x);

      // If the PooledConnection is not in use, close it
      if ( pcon.inUse() == false ) { pcon.close();  }
      else
      {
        if (DEBUG)
        {
          System.out.println("--> Force close");
        }

        // If it still in use, sleep for 30 seconds and
        // force close.
        try
        {
          java.lang.Thread.sleep(30000);
          pcon.close();
        }
        catch (InterruptedException ie)
        {
          if (DEBUG) System.err.println(ie.getMessage());
        }
      }
    }

    if (DEBUG)
    {
      System.out.println("Exit Default Connection Pool, Finalize");
    }

    super.finalize();
  }
}
