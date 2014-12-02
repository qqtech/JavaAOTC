package com.sun.corba.se.ActivationIDL;


/**
* com/sun/corba/se/ActivationIDL/ServerAlreadyActiveHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from ../../../../../../src/share/classes/com/sun/corba/se/ActivationIDL/activation.idl
* Wednesday, October 18, 2006 11:10:44 AM PDT
*/

abstract public class ServerAlreadyActiveHelper
{
  private static String  _id = "IDL:ActivationIDL/ServerAlreadyActive:1.0";

  public static void insert (org.omg.CORBA.Any a, com.sun.corba.se.ActivationIDL.ServerAlreadyActive that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static com.sun.corba.se.ActivationIDL.ServerAlreadyActive extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [1];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.corba.se.ActivationIDL.ServerIdHelper.id (), "ServerId", _tcOf_members0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "serverId",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_exception_tc (com.sun.corba.se.ActivationIDL.ServerAlreadyActiveHelper.id (), "ServerAlreadyActive", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static com.sun.corba.se.ActivationIDL.ServerAlreadyActive read (org.omg.CORBA.portable.InputStream istream)
  {
    com.sun.corba.se.ActivationIDL.ServerAlreadyActive value = new com.sun.corba.se.ActivationIDL.ServerAlreadyActive ();
    // read and discard the repository ID
    istream.read_string ();
    value.serverId = istream.read_long ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.corba.se.ActivationIDL.ServerAlreadyActive value)
  {
    // write the repository ID
    ostream.write_string (id ());
    ostream.write_long (value.serverId);
  }

}
