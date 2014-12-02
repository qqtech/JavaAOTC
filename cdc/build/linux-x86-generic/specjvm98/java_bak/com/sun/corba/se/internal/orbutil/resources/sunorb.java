package com.sun.corba.se.internal.orbutil.resources;

import java.util.ListResourceBundle;

public final class sunorb extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "bootstrap.exception", "caught exception while saving Properties to file {0}: Exception {1}" },
            { "bootstrap.filenotfound", "the file {0} not found" },
            { "bootstrap.filenotreadable", "the file {0} is not readable" },
            { "bootstrap.success", "setting port to {0} and reading services from {1}" },
            { "bootstrap.usage", "Usage: {0} <options> \n\nwhere <options> includes:\n  -ORBInitialPort        Initial Port (required)\n  -InitialServicesFile   File containing list of initial services (required)\n" },
            { "orbd.commfailure", "\nFailed to start ORBD because ORBinitialPort is already in use" },
            { "orbd.internalexception", "\nFailed to start ORBD because of an Internal Exception. \nPossible Causes: \n1. Specified ORBInitialPort or ORBActivationPort is already in use \n2. No Write Permission to write orb.db " },
            { "orbd.usage", "Usage: {0} <options> \n\nwhere <options> includes:\n  -port                  Activation Port where the ORBD should be started, default 1049 (optional)\n  -defaultdb             Directory for ORBD files, default \"./orb.db\" (optional)\n  -serverid              Server Id for ORBD, default 1 (optional)\n  -ORBInitialPort        Initial Port (required)\n  -ORBInitialHost        Initial HostName (required)\n" },
            { "pnameserv.success", "Persistent NameServer Started Successfully" },
            { "servertool.appname", "\tapplicationName     - {0}" },
            { "servertool.args", "\targs      - {0}" },
            { "servertool.baddef", "Bad server definition: {0}" },
            { "servertool.banner", "\n\nWelcome to the Java IDL Server Tool \nplease enter commands at the prompt \n" },
            { "servertool.classpath", "\tclasspath - {0}" },
            { "servertool.getserverid", "\n\tgetserverid [ -applicationName <name> ] \n" },
            { "servertool.getserverid1", "return the server id for an applicationName" },
            { "servertool.getserverid2", "\tServer ID for applicationName {0} is {1}" },
            { "servertool.helddown", "\tserver is held down." },
            { "servertool.help", "\thelp\n\tOR\n\thelp <command name>\n" },
            { "servertool.help1", "get help" },
            { "servertool.list", "\n\tlist\n" },
            { "servertool.list1", "list all registered servers" },
            { "servertool.list2", "\n\tServer Id\tServer Class Name\t\tServer Application\n\t---------\t-----------------\t\t------------------\n" },
            { "servertool.listactive", "\n\tlistactive" },
            { "servertool.listactive1", "list currently active servers" },
            { "servertool.listappnames", "\tlistappnames\n" },
            { "servertool.listappnames1", "list applicationNames currently defined" },
            { "servertool.listappnames2", "Currently defined server applicationNames:" },
            { "servertool.locate", "\n\tlocate [ -serverid <server id> | -applicationName <name> ] [ <-endpointType <endpointType> ] \n" },
            { "servertool.locate1", "locate ports of specific type for a registered server" },
            { "servertool.locate2", "\n\n\tHost Name {0} \n\n\t\tPort\t\tPort Type\t\tORB Id\n\t\t----\t\t---------\t\t------\n" },
            { "servertool.locateorb", "\n\tlocateperorb [ -serverid <server id> | -applicationName <name> ] [ -orbid <ORB name> ]\n" },
            { "servertool.locateorb1", "locate ports for a specific orb of registered server" },
            { "servertool.locateorb2", "\n\n\tHost Name {0} \n\n\t\tPort\t\tPortType\t\tORB Id\n\t\t----\t\t--------\t\t------\n" },
            { "servertool.name", "\tname      - {0}" },
            { "servertool.nosuchorb", "\tinvalid ORB." },
            { "servertool.nosuchserver", "\tno such server found." },
            { "servertool.orbidmap", "\tUsage: orblist [ -serverid <server id> | -applicationName <name> ]\n" },
            { "servertool.orbidmap1", "list of orb names and their mapping" },
            { "servertool.orbidmap2", "\n\tORB Id\t\tORB Name\n\t------\t\t--------\n" },
            { "servertool.quit", "\n\tquit\n" },
            { "servertool.quit1", "quit this tool" },
            { "servertool.register", "\n\n\tregister -server <server class name> \n\t         -applicationName <alternate server name> \n\t         -classpath <classpath to server> \n\t         -args <args to server> \n\t         -vmargs <args to server Java VM>\n" },
            { "servertool.register1", "register an activatable server" },
            { "servertool.register2", "\tserver registered (serverid = {0})." },
            { "servertool.register3", "\tserver registerd but held down (serverid = {0})." },
            { "servertool.register4", "\tserver already registered (serverid = {0})." },
            { "servertool.serverid", "\tserver id - {0}" },
            { "servertool.servernotrunning", "\tserver is not running." },
            { "servertool.serverup", "\tserver is already up." },
            { "servertool.shorthelp", "\n\n\tAvailable Commands: \n\t------------------- \n" },
            { "servertool.shutdown", "\n\tshutdown [ -serverid <server id> | -applicationName <name> ]\n" },
            { "servertool.shutdown1", "shutdown a registered server" },
            { "servertool.shutdown2", "\tserver sucessfully shutdown." },
            { "servertool.startserver", "\n\tstartup [ -serverid <server id> | -applicationName <name> ]\n" },
            { "servertool.startserver1", "start a registered server" },
            { "servertool.startserver2", "\tserver sucessfully started up." },
            { "servertool.unregister", "\n\tunregister [ -serverid <server id> | -applicationName <name> ] \n" },
            { "servertool.unregister1", "unregister a registered server" },
            { "servertool.unregister2", "\tserver unregistered." },
            { "servertool.usage", "Usage: {0} <options> \n\nwhere <options> includes:\n  -ORBInitialPort        Initial Port (required)\n  -ORBInitialHost        Initial HostName (required)\n" },
            { "servertool.vmargs", "\tvmargs    - {0}" },
            { "tnameserv.exception", "caught an exception while starting the bootstrap service on port {0}" },
            { "tnameserv.hs1", "Initial Naming Context:\n{0}" },
            { "tnameserv.hs2", "TransientNameServer: setting port for initial object references to: {0}" },
            { "tnameserv.hs3", "Ready." },
            { "tnameserv.invalidhostoption", "ORBInitialHost is not a valid option for NameService" },
            { "tnameserv.orbinitialport0", "ORBInitialPort 0 is not valid option for NameService" },
            { "tnameserv.usage", "try using a different port with commandline arguments -ORBInitialPort <portno>" },
        };
    }
}
