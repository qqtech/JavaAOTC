/*
 * @(#)UnknownUserExceptionHolder.java	1.4 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

/**
* The Holder for <tt>UnknownUserException</tt>.  For more information on 
* Holder files, see <a href="doc-files/generatedfiles.html#holder">
* "Generated Files: Holder Files"</a>.<P>
* org/omg/CORBA/UnknownUserExceptionHolder.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CORBA.idl
* Thursday, August 24, 2000 5:52:22 PM PDT
*/

public final class UnknownUserExceptionHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.UnknownUserException value = null;

  public UnknownUserExceptionHolder ()
  {
  }

  public UnknownUserExceptionHolder (org.omg.CORBA.UnknownUserException initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.UnknownUserExceptionHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.UnknownUserExceptionHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.UnknownUserExceptionHelper.type ();
  }

}
