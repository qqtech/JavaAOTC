package com.sun.corba.se.internal.orbutil.resources;

import java.util.ListResourceBundle;

public final class sunorb_sv extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "bootstrap.exception", "p\u00E5tr\u00E4ffade undantag medan egenskaper sparades i filen {0}: Undantag {1}" },
            { "bootstrap.filenotfound", "filen {0} g\u00E5r inte att hitta" },
            { "bootstrap.filenotreadable", "filen {0} g\u00E5r inte att l\u00E4sa" },
            { "bootstrap.success", "s\u00E4tter porten till {0} och l\u00E4ser tj\u00E4nster fr\u00E5n {1}" },
            { "bootstrap.usage", "G\u00F6r s\u00E5 h\u00E4r: {0} <alternativ> \n\nd\u00E4r <alternativ> omfattar:\n  -ORBInitialPort        Initialport (obligatoriskt)\n  -InitialServicesFile   En fil som inneh\u00E5ller en lista \u00F6ver initiala tj\u00E4nster (obligatoriskt)\n" },
            { "orbd.commfailure", "\nMisslyckades starta ORBD f\u00F6r att ORBinitialport anv\u00E4ndas redan" },
            { "orbd.internalexception", "\nMisslyckades starta ORBD p\u00E5 grund av internt undantag. \nM\u00F6jliga Orsakar: \n1. Anget ORBInitialPort or ORBAktiveringsPort anv\u00E4ndas redan \n2. No Write Tillst\u00E5nd att skriva orb.db " },
            { "orbd.usage", "G\u00F6r s\u00E5 h\u00E4r: {0} <alternativ> \n\nd\u00E4r <alternativ> omfattar:\n  -port                  Aktiveringsport d\u00E4r ORBD ska startas, standard 1049 (valfritt)\n  -defaultdb             Katalog f\u00F6r ORBD-filer, standard \"./orb.db\" (valfritt)\n  -serverid              Server-ID f\u00F6r ORBD, standard 1 (valfritt)\n  -ORBInitialPort        Initialport (obligatoriskt)\n  -ORBInitialHost        Initialt v\u00E4rdnamn (obligatoriskt)\n" },
            { "pnameserv.success", "Namnservern har startats utan problem" },
            { "servertool.appname", "\tapplicationName     - {0}" },
            { "servertool.args", "\targs      - {0}" },
            { "servertool.baddef", "D\u00E5lig serverdefinition: {0}" },
            { "servertool.banner", "\n\nV\u00E4lkommen till Java IDL Server Tool \nskriv kommandona vid prompten \n" },
            { "servertool.classpath", "\tclasspath - {0}" },
            { "servertool.getserverid", "\n\tgetserverid [ -applicationName <namn> ] \n" },
            { "servertool.getserverid1", "returnerar server-id f\u00F6r ett applicationName" },
            { "servertool.getserverid2", "\tServer-ID f\u00F6r applicationName {0} \u00E4r {1}" },
            { "servertool.helddown", "\tserver h\u00E5lls nere." },
            { "servertool.help", "\thelp\n\tOR\n\thelp <kommandonamn>\n" },
            { "servertool.help1", "get help" },
            { "servertool.list", "\n\tlist_\n" },
            { "servertool.list1", "listar alla registrerade servrar" },
            { "servertool.list2", "\n\tServer Id\tServer Class Name\t\tServer Application\n\t---------\t-----------------\t\t------------------\n" },
            { "servertool.listactive", "\n\tlistactive" },
            { "servertool.listactive1", "listar alla f\u00F6r tillf\u00E4llet aktiva servrar" },
            { "servertool.listappnames", "\tlistappnames\n" },
            { "servertool.listappnames1", "listar de applicationNames som f\u00F6r tillf\u00E4llet \u00E4r definierade" },
            { "servertool.listappnames2", "Aktuella definierade server-applicationNames:" },
            { "servertool.locate", "\n\tlocate [ -serverid <server-id> | -applicationName <namn> ] [ <-endpointType <endpointtyp> ] \n" },
            { "servertool.locate1", "lokaliserar portar av en viss typ f\u00F6r en registrerad server" },
            { "servertool.locate2", "\n\n\tV\u00E4rdnamn {0} \n\n\t\tPort\t\tPorttyp\t\tORB-ID\n\t\t----\t\t---------\t\t------\n" },
            { "servertool.locateorb", "\n\tlocateperorb [ -serverid <server-id> | -applicationName <namn> ] [ -orbid <ORB-namn> ]\n" },
            { "servertool.locateorb1", "lokaliserar portar f\u00F6r en viss ORB f\u00F6r en registrerad server" },
            { "servertool.locateorb2", "\n\n\tV\u00E4rdnamn {0} \n\n\t\tPort\t\tPorttyp\t\tORB-ID\n\t\t----\t\t--------\t\t------\n" },
            { "servertool.name", "\tnamn      - {0}" },
            { "servertool.nosuchorb", "\togiltig ORB." },
            { "servertool.nosuchserver", "\tn\u00E5gon s\u00E5dan server kan inte hittas." },
            { "servertool.orbidmap", "\tG\u00F6r s\u00E5 h\u00E4r: orblist [ -serverid <server-id> | -applicationName <namn> ]\n" },
            { "servertool.orbidmap1", "lista \u00F6ver ORB-namn och deras mappning" },
            { "servertool.orbidmap2", "\n\tORB-ID\t\tORB-namn\n\t------\t\t--------\n" },
            { "servertool.quit", "\n\tquit\n" },
            { "servertool.quit1", "avsluta det h\u00E4r verktyget" },
            { "servertool.register", "\n\n\tregister -server <serverklassnamn> \n\t         -applicationName <alternativt servernamn> \n\t         -classpath <klass\u00F6kv\u00E4g till server> \n\t         -args <argument till server> \n\t         -vmargs <argument till server-JVM>\n" },
            { "servertool.register1", "registrera en aktiverbar server" },
            { "servertool.register2", "\tserver registrerad (serverid = {0})." },
            { "servertool.register3", "\tserver registrerad men h\u00E5lls nere (serverid = {0})." },
            { "servertool.register4", "\tserver har redan registrerats (serverid = {0})." },
            { "servertool.serverid", "\tserver id - {0}" },
            { "servertool.servernotrunning", "\tserver k\u00F6rs inte." },
            { "servertool.serverup", "\tserver \u00E4r redan uppe." },
            { "servertool.shorthelp", "\n\n\tTillg\u00E4ngliga kommandon: \n\t------------------- \n" },
            { "servertool.shutdown", "\n\tshutdown [ -serverid <server-id> | -applicationName <namn> ]\n" },
            { "servertool.shutdown1", "st\u00E4nger en registrerad server" },
            { "servertool.shutdown2", "\tserver har st\u00E4ngts av." },
            { "servertool.startserver", "\n\tstartup [ -serverid <server-id> | -applicationName <namn> ]\n" },
            { "servertool.startserver1", "startar en registrerad server" },
            { "servertool.startserver2", "\tserver har startats." },
            { "servertool.unregister", "\n\tunregister [ -serverid <server-id> | -applicationName <namn> ] \n" },
            { "servertool.unregister1", "avregistrera en registrerad server" },
            { "servertool.unregister2", "\tserver oregistrerad." },
            { "servertool.usage", "G\u00F6r s\u00E5 h\u00E4r: {0} <alternativ> \n\nd\u00E4r <alternativ> omfattar:\n  -ORBInitialPort        Initialport (obligatoriskt)\n  -ORBInitialHost        Initialt v\u00E4rdnamn (obligatoriskt)\n" },
            { "servertool.vmargs", "\tvmargs    - {0}" },
            { "tnameserv.exception", "p\u00E5tr\u00E4ffade ett undantagsfel n\u00E4r starttj\u00E4nsten startades vid porten {0}" },
            { "tnameserv.hs1", "Initial namngivningskontext:\n{0}" },
            { "tnameserv.hs2", "TransientNameServer: anger port f\u00F6r initiala objektreferenser till: {0}" },
            { "tnameserv.hs3", "Klar." },
            { "tnameserv.invalidhostoption", "ORBInitialHost \u00E4r inte ett giltigt alternativ f\u00F6r NameService" },
            { "tnameserv.orbinitialport0", "ORBInitialPort 0 \u00E4r inte ett giltigt alternativ f\u00F6r NameService" },
            { "tnameserv.usage", "f\u00F6rs\u00F6k att anv\u00E4nda en annan port med kommandoradsargument -ORBInitialPort <portnr>" },
        };
    }
}