package com.sun.corba.se.ActivationIDL.RepositoryPackage;


/**
* com/sun/corba/se/ActivationIDL/RepositoryPackage/StringSeqHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from ../../../../../../src/share/classes/com/sun/corba/se/ActivationIDL/activation.idl
* Wednesday, October 18, 2006 11:10:44 AM PDT
*/

public final class StringSeqHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public StringSeqHolder ()
  {
  }

  public StringSeqHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = com.sun.corba.se.ActivationIDL.RepositoryPackage.StringSeqHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    com.sun.corba.se.ActivationIDL.RepositoryPackage.StringSeqHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return com.sun.corba.se.ActivationIDL.RepositoryPackage.StringSeqHelper.type ();
  }

}
