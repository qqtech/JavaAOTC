package com.sun.corba.se.ActivationIDL.InitialNameServicePackage;

/**
* com/sun/corba/se/ActivationIDL/InitialNameServicePackage/NameAlreadyBoundHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from ../../../../../../src/share/classes/com/sun/corba/se/ActivationIDL/activation.idl
* Wednesday, October 18, 2006 11:10:44 AM PDT
*/

public final class NameAlreadyBoundHolder implements org.omg.CORBA.portable.Streamable
{
  public com.sun.corba.se.ActivationIDL.InitialNameServicePackage.NameAlreadyBound value = null;

  public NameAlreadyBoundHolder ()
  {
  }

  public NameAlreadyBoundHolder (com.sun.corba.se.ActivationIDL.InitialNameServicePackage.NameAlreadyBound initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = com.sun.corba.se.ActivationIDL.InitialNameServicePackage.NameAlreadyBoundHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    com.sun.corba.se.ActivationIDL.InitialNameServicePackage.NameAlreadyBoundHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return com.sun.corba.se.ActivationIDL.InitialNameServicePackage.NameAlreadyBoundHelper.type ();
  }

}
