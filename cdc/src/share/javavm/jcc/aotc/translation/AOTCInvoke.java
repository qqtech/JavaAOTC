package aotc.translation;

import aotc.*;
import aotc.share.*;
import aotc.ir.*;
import aotc.cfg.*;
import aotc.optimizer.*;
import aotc.profile.*;
import components.*;
import util.*;
import opcodeconsts.*;
import consts.*;
import jcc.*;
import vm.*;
import java.util.*;

public class AOTCInvoke {
    public static final int INVOKE_INLINED = 0x1;
    public static final int INVOKE_NATIVE = 0x2;

    /**
      The constants in the aotc code, that identifies the call types
     */
    public static final int CALLTYPE_JAVA = 0x1;
    public static final int CALLTYPE_JNI = 0x2;
    public static final int CALLTYPE_CNI = 0x4;
    public static final int CALLTYPE_AOTC = 0x8;

    /**
     * The function name for goto label.
     */
    private String name;
    /**
     * The class information.
     */
    private ClassInfo classinfo;
    /**
     * The method information.
     */
    private MethodInfo methodinfo;
    /**
     * The output stream.
     */
    private AOTCPrint out;
    /**
     * Exception handler
     */
    private ExceptionHandler exceptionHandler;
    /**
     * Current IR
     */
    private IR ir;

    private MethodConstant mc;
    private boolean isVirtual = false;
    private boolean isStatic = false;
    private boolean isNonvirtual = false;
    private boolean checkInit = false;
    private int possibleCalltype = 0;
    private JavaVariable obj = null;
    private JavaVariable ret = null;
    private JavaVariable args[];
    private ClassInfo calleeClassinfo;
    private MethodInfo calleeMethodinfo;
    private String MB;
    private CFGInfo cfgInfo;
    private TreeSet liveVariables;
    private Vector calleeProfileList; // for profile
    private boolean useBranchHint = false;
    // by uhehe99 
    // to estimate candidate for preexistence-based inlining
    // count call-site where only single callee is called, but should be guarded.
    public static int singleTarget = 0;
    public static int multipleTarget = 0;

    public AOTCInvoke(IR ir, ClassInfo classinfo, MethodInfo methodinfo,
            String name, AOTCPrint out, ExceptionHandler exceptionHandler,
            CFGInfo cfgInfo, TreeSet liveVariables) {
        this.ir = ir;
        this.classinfo = classinfo;
        this.methodinfo = methodinfo;
        this.name = name;
        this.out = out;
        this.exceptionHandler = exceptionHandler;
        this.MB = "mb";
        this.cfgInfo = cfgInfo;
        this.liveVariables = liveVariables;
        if (AOTCWriter.USE_CALL_COUNT_INFO) { // get profile information from file
            CallCountTable ct = CallCountAnalyzer.getCallCountTable();
            if (ct != null) {
                this.calleeProfileList = ct.findCalleeList(classinfo.className,
                        (methodinfo.name.string + methodinfo.type.string),
                        Integer.toString(ir.getOriginalBpc()));
            }
        } else {
            this.calleeProfileList = null;
        }
    }

    public void doInvoke() {
        fillInfo();

        // prints call info
        out.Print("// [class:" + calleeClassinfo.className + "], [method:"
                + calleeMethodinfo.name + "], [sig:"
                + calleeMethodinfo.type + "]");
        if (calleeProfileList != null) {
            for (int i = 0; i < calleeProfileList.size(); i++) {
                out.Print("// #" + (i+1) + " : " + calleeProfileList.elementAt(i));
            }
        }

        // null check for virtual methods
        if(!isStatic) {
            exceptionHandler.checkNullPointerException(ir, ir.getOperand(0)); 
        }

        if(ir.getShortOpcode()==OpcodeConst.opc_invokeinterface_quick) {
            doInvokeInterface(); // Interface call
            //doGCCheckAfterCall(true);
            //doExceptionCheckAfterCall(true);
            doCheckAfterCall(null, true, true);
        } else if(isVirtual) {
            doAOTCVirtualCall();
            // GC check position is moved to doAOTCVirtualCall for the check removal!
            //doGCCheckAfterCall(true);
            //doExceptionCheckAfterCall(true);
            doCheckAfterCall(null, true, true);
        } else {
            doAOTCStaticCall();
            // Exception & GC check after calls
        }
    }

    private void doCheckAfterCall(MethodInfo calleeMethod, boolean doGCCheck, boolean doExceptionCheck) {
        CFGInfo info;
        boolean generateException;
        boolean generateGC;
        int numOfRefsToRestore;

        if (ir.getNumOfTargets() > 0) {
            numOfRefsToRestore = CCodeGenerator.getRefsToRestore(methodinfo, ir.getTarget(0), liveVariables).size();
        } else {
            numOfRefsToRestore = CCodeGenerator.getRefsToRestore(methodinfo, null, liveVariables).size();
        }
        if(calleeMethod != null && !ClassFilter.inList(calleeMethod.parent.className, calleeMethod.name.string, calleeMethod.type.string)) {
            calleeMethod = null;                                              
        }  

        if(calleeMethod != null) {  // apply callee specific optimization
            generateException = CFGInfo.getCFGInfo(calleeMethod).getExceptionCheck() > 0;
            generateGC = ( (CFGInfo.getCFGInfo(calleeMethod).getGCCheckPoints()
                        + CFGInfo.getCFGInfo(calleeMethod).getGcSafe() ) > 0 );
        } else {
            generateException = true;
            generateGC = true;
        }
        if(checkInit && !ir.hasAttribute(IRAttribute.PRELOADED) &&
                !ir.hasAttribute(IRAttribute.REDUNDANT_CLASSINIT)) {
            generateGC = true;
        }
        doGCCheck = (doGCCheck && generateGC && (numOfRefsToRestore > 0));
        doExceptionCheck = (doExceptionCheck && generateException) && (AOTCWriter.EXCEPTION_IMPLEMENTATION == AOTCWriter.CALL_CHECK_HANDLING);

        out.Print("//num of ref to restore = " + numOfRefsToRestore);
        if (doGCCheck || doExceptionCheck) {
            out.Print("if (unlikely(local_gc_and_exception_count != global_gc_and_exception_count)) {");
            out.Printf("local_gc_and_exception_count = global_gc_and_exception_count;");

            if (doGCCheck) {
                doGCCheckAfterCall(calleeMethod);
            } else {
                out.Print("/* GC check after call is eliminated by interprocedural analysis */");
            }

            if (doExceptionCheck) {
                doExceptionCheckAfterCall();
            } else {
                out.Print("/* Exception check after call is eliminated by interprocedural analysis or inlinig*/");
            }

            out.Print("}");
        } else {
            out.Print("/* GC & Exception checks after call are eliminated by interprocedural analysis or inlinig*/");
        }
    }

    private void doExceptionCheckAfterCall() {
        /* if(AOTCWriter.EXCEPTION_IMPLEMENTATION == AOTCWriter.CALL_CHECK_HANDLING) */ exceptionHandler.checkExceptionInvoke(ir);
    }
    private void doGCCheckAfterCall(MethodInfo calleeMethod) {
        if (ir.getNumOfTargets() > 0) {
            CCodeGenerator.doGCRestorePreserve(methodinfo, ir.getBpc(), ir.getTarget(0), liveVariables, out, false);
        } else {
            CCodeGenerator.doGCRestore(methodinfo, ir.getBpc(), liveVariables, out, false);
        }
    }

    private void fillInfo() {
        switch(ir.getShortOpcode()) {
            case OpcodeConst.opc_invokestatic_checkinit_quick:
                //mc = (MethodConstant)classinfo.constants[ir.getBytecode().getOperand(0)];
                mc = (MethodConstant)classinfo.getConstantPool().getConstants()[ir.getBytecode().getOperand(0)];
                isStatic = true;
                checkInit = true;
                break;
            case OpcodeConst.opc_invokestatic_quick:
                //mc = (MethodConstant)classinfo.constants[ir.getBytecode().getOperand(0)];
                mc = (MethodConstant)classinfo.getConstantPool().getConstants()[ir.getBytecode().getOperand(0)];
                isStatic = true;
                break;
            case OpcodeConst.opc_invokevirtual_quick_w:
                //mc = (MethodConstant)classinfo.constants[ir.getBytecode().getOperand(0)];
                //mc = (MethodConstant)classinfo.getConstantPool().getConstants()[ir.getBytecode().getOperand(0)];
                mc = (MethodConstant)methodinfo.typeInfo.get(Integer.toString(ir.getOriginalBpc()));
                isVirtual = true;
                break;
            case OpcodeConst.opc_invokenonvirtual_quick:
                //mc = (MethodConstant)classinfo.constants[ir.getBytecode().getOperand(0)];
                mc = (MethodConstant)classinfo.getConstantPool().getConstants()[ir.getBytecode().getOperand(0)];
                isNonvirtual = true;
                break;
            case OpcodeConst.opc_invokevirtual_quick:
            case OpcodeConst.opc_ainvokevirtual_quick:
            case OpcodeConst.opc_dinvokevirtual_quick:
            case OpcodeConst.opc_vinvokevirtual_quick:
            case OpcodeConst.opc_invokevirtualobject_quick: // TODO ...
                //mc = (MethodConstant)methodinfo.typeInfo.get(Integer.toString(ir.getBpc()));
                mc = (MethodConstant)methodinfo.typeInfo.get(Integer.toString(ir.getOriginalBpc()));
                isVirtual = true;
                if (ir.hasAttribute(IRAttribute.PRELOADED)) {
                    MB = "mbAt" + ir.getBpc();
                }
                break;
            case OpcodeConst.opc_invokeinterface_quick: 
                //mc = (MethodConstant)classinfo.constants[ir.getBytecode().getOperand(0)];
                mc = (MethodConstant)classinfo.getConstantPool().getConstants()[ir.getBytecode().getOperand(0)];
                isVirtual = true;
                break;
        }
        calleeMethodinfo = mc.find();
        calleeClassinfo = calleeMethodinfo.parent;


        possibleCalltype = checkStaticCalltype(calleeMethodinfo, calleeClassinfo);
        // handle special cases

        // following class has sync native methods.
        if(calleeClassinfo.className.equals("java/net/PlainDatagramSocketImpl")) {
            possibleCalltype = CALLTYPE_JAVA;
        }

        if(isVirtual) {
            adjustPossibleType();
        }

        makeArgsRet();
    }

    public static int checkStaticCalltype(MethodInfo calleeMethodinfo, ClassInfo calleeClassinfo) {
        int type;
        if((calleeMethodinfo.access & Const.ACC_AOTC)!=0) {
            type = CALLTYPE_AOTC;
        } else if((calleeMethodinfo.access & Const.ACC_NATIVE)!=0) {
            if(MethodTranslator.nativeTypes.isType(calleeClassinfo.className, "CNI")) {
                type = CALLTYPE_CNI;
            } else {
                type = CALLTYPE_JNI;
            }
        } else {
            type = CALLTYPE_JAVA;
        }
        return type;
    }

    private void adjustPossibleType() { // Manually change or add possible type
        if (classinfo.className.equals("spec/io/FileInputStream")
                && calleeClassinfo.className.equals("java/io/InputStream")
                && calleeMethodinfo.name.string.equals("read")) {
            possibleCalltype |= CALLTYPE_JNI;
        }

        if (classinfo.className.equals("java/util/Hashtable") 
                && calleeMethodinfo.name.string.equals("hashCode")) {
            possibleCalltype |= CALLTYPE_AOTC;
        }

        if (calleeClassinfo.className.equals("java/lang/Number")) {
            possibleCalltype |= CALLTYPE_AOTC;
            possibleCalltype |= CALLTYPE_JNI;
        }
    }

    private void makeArgsRet() {
        if(ir.getNumOfTargets()!=0 && ir.getNumOfUses(0)!=0) {
            ret = ir.getTarget(0);
        }

        Vector operands = ir.getOperandList();
        if(isStatic) {
            args = new JavaVariable[operands.size()];
            for(int i=0; i<operands.size(); i++) {
                args[i] = (JavaVariable)operands.elementAt(i);
            }
        } else { // isVirtual || isNonvirtual
            obj = (JavaVariable)operands.elementAt(0);
            args = new JavaVariable[operands.size()-1];
            for(int i=1; i<operands.size(); i++) {
                args[i-1] = (JavaVariable)operands.elementAt(i);
            }
        }
    }

    private boolean doInline(int count, int number) {
        MethodInliner mi = new MethodInliner();
        String newname;
        if(number>0) newname = number + "_" + name;
        else newname = name;
        if(mi.doInline(calleeClassinfo, calleeMethodinfo, isVirtual, ir, ret, out, newname, count, exceptionHandler)) {
            return true;
        }
        return false;
    }

    private boolean tryNativeCall(String classname, String methodname, String signature, boolean write) {
        String functionName = null;

        if(classname.equals("java/lang/String")) {
            if(methodname.equals("indexOf") && !signature.equals("([CII[CIII)I"))
                functionName = ("AOTC_" + Util.convertToJNIName(classname, methodname, signature).substring(5));
            else if(methodname.equals("charAt") ||
                    methodname.equals("equals") ||
                    methodname.equals("compareTo") ||
                    methodname.equals("getChars") ||
                    methodname.equals("hashCode") ||
                    methodname.equals("intern"))
                functionName = "AOTC_java_lang_String_" + methodname;

        } else if(classname.equals("java/lang/StringBuffer")) {
            if(methodname.equals("append") &&
                    (signature.equals("([C)Ljava/lang/StringBuffer;") || signature.equals("([CII)Ljava/lang/StringBuffer;")))
                functionName = ("AOTC_" + Util.convertToJNIName(classname, methodname, signature).substring(5));
            else if(methodname.equals("expandCapacity"))
                functionName = "AOTC_java_lang_StringBuffer_" + methodname;

        } else if(classname.equals("java/util/Vector")) {
            if(methodname.equals("ensureCapacityHelper") ||
                    methodname.equals("addElement"))
                functionName = "AOTC_java_util_Vector_" + methodname;
        } else if(classname.equals("sun/io/ByteToCharISO8859_1")) {
            if(methodname.equals("convert")) {
                functionName = "AOTCsun_io_ByteToCharISO8859_1_" + methodname;
            }
        } else if(classname.equals("sun/io/CharToByteISO8859_1")) {
            if(methodname.equals("convert")) {
                functionName = "AOTC_sun_io_CharToByteISO8859_1_convert";
            }
        }

        if(functionName==null)
            return false;
        if(!write)
            return true;

        String argument = "ee, " + obj + "_ICell";
        for(int i=0; i<args.length; i++) {
            if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                argument += (", " + args[i] + "_ICell");
            } else {
                argument += (", " + args[i]);
            }
        }

        if(ret == null) {
            out.Print(functionName + "(" + argument + ");");
        } else {
            out.Print(ret + " = " + functionName + "(" + argument + ");");
        }
        if(ret!=null && ret.getType()==TypeInfo.TYPE_REFERENCE) {
            if(ir.hasAttribute(IRAttribute.NEED_ICELL))
                out.Print(ret + "_ICell->ref_DONT_ACCESS_DIRECTLY = " + ret + ";");
        }
        return true;
    }
    private boolean tryNativeVirtualCall() {
        // { className of callsite, calleeClassName, calleeMethodName, 
        //   expectedClassblock, function name} 
        // and null means DON'T CARES.
        // NOTE: the upper array elements is tested earlier.
        String[][] nativeList = {
            { "java/util/Stack", "java/util/Vector", "addElement",
                "java_util_Stack_Classblock",
                "AOTC_java_util_Vector_addElement"}, // Stack is inherited from Vector, but I think this is somewhat risky approach.
            { null, "sun/io/ByteToCharConverter", "convert",
                "sun_io_ByteToCharISO8859_1_Classblock",
                "AOTCsun_io_ByteToCharISO8859_1_convert" },
            { "java/util/Hashtable", "java/lang/Object", "equals",
                "java_lang_String_Classblock",
                "AOTC_java_lang_String_equals" },
            { null, "sun/io/CharToByteConverter", "convert",
                "sun_io_CharToByteISO8859_1_Classblock",
                "AOTC_sun_io_CharToByteISO8859_1_convert" },
            { "java/util/Hashtable", null, "hashCode",
                "java_lang_String_Classblock",
                "AOTC_java_lang_String_hashCode" },
            { null, "java/util/Vector", "addElement",
                "java_util_Vector_Classblock",
                "AOTC_java_util_Vector_addElement"},
            { null, "java/util/Vector", "ensureCapacityHelper",
                "java_util_Vector_Classblock",
                "AOTC_java_util_Vector_ensureCapacityHelper"}
        };

        String functionName = null;
        String expectedClassBlock = null;

        for (int i = 0; i < nativeList.length; i++) {
            if ( (nativeList[i][0] == null || classinfo.className.equals(nativeList[i][0]))
                    && (nativeList[i][1] == null || calleeClassinfo.className.equals(nativeList[i][1]))
                    && (nativeList[i][2] == null || calleeMethodinfo.name.string.equals(nativeList[i][2]))) { 
                expectedClassBlock = nativeList[i][3];
                functionName = nativeList[i][4];
                break;
            }
        }
        if (functionName != null) { // the same part as tryNativeStaticCall: it would be better to merge below code
            out.Print("extern const CVMClassBlock " + expectedClassBlock + ";");
            out.Print("if (cb == (CVMClassBlock*) &" + expectedClassBlock + ") {");
            out.Print("// AOTC type virtual method derived from CNI method");
            // Call Arg - BY Clamp *****
            String argument = "ee, ";
            /*
               if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.CNI_TYPE) {
               argument += "callArg";
               }else if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.JNI_TYPE) {
               argument += obj + "_ICell";
               }else if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.MIX_TYPE) {
               argument += "NULL, NULL, " + obj + "_ICell";
               }
             */
            argument += obj + "_ICell";

            //if(AOTCWriter.METHOD_ARGUMENT_PASSING != AOTCWriter.CNI_TYPE) {
            for(int i=0; i<args.length; i++) {
                if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                    argument += (", " + args[i] + "_ICell");
                } else {
                    argument += (", " + args[i]);
                }
            }

            /*} else {
              int j = 0;
              out.Print("CVMStackVal32 callArg["+ args.length + args.length +"];");
              out.Printf("callArg["+j+"].j.r.ref_DONT_ACCESS_DIRECTLY = "+obj+";");//added
              j++;

              for(int i=0 ; i<args.length; i++) {
              if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
              out.Printf("callArg["+j+"].j.r.ref_DONT_ACCESS_DIRECTLY = "+args[i]+";");//added
              } else {
              if(args[i].getType()==TypeInfo.TYPE_FLOAT)
              out.Printf("callArg["+j+"].j.f = "+args[i]+";"); //added
              else if(args[i].getType()==TypeInfo.TYPE_LONG){
              out.Printf("callArg["+j+"].j.i = "+args[i]+"& 0xffffffff;");
              out.Printf("callArg["+(j+1)+"].j.i = ("+args[i]+"& 0xffffffff00000000)>>32;");
              j++;
              }
              else if(args[i].getType()==TypeInfo.TYPE_DOUBLE){
              out.Printf("CNIDouble2jvm(callArg+"+j+", "+args[i]+");");
              j++;
              }
              else
              out.Printf("callArg["+j+"].j.i = "+args[i]+";"); //added

              }
              j++;
              }
              }
             */

            if(ret == null) {
                out.Print(functionName + "(" + argument + ");");
            } else {
                //if(AOTCWriter.METHOD_ARGUMENT_PASSING != AOTCWriter.CNI_TYPE) {
                out.Print(ret + " = " + functionName + "(" + argument + ");");
                /*} else {
                  out.Printf(functionName + "(ee, callArg);");
                  if(ret.getType()==TypeInfo.TYPE_REFERENCE) {
                  out.Print(ret +"= callArg[0].j.r.ref_DONT_ACCESS_DIRECTLY;");
                  }
                  else{
                  if(ret.getType()==TypeInfo.TYPE_FLOAT)
                  out.Printf(ret +"= callArg[0].j.f;");
                  else if(ret.getType()==TypeInfo.TYPE_LONG){
                  out.Printf("CNIjvm2Long(callArg, "+ret+");");
                  }
                  else if(ret.getType()==TypeInfo.TYPE_DOUBLE){
                  out.Printf("CNIjvm2Double(callArg, "+ret+");");
                  }
                  else
                  out.Print(ret +"= callArg[0].j.i;");
                  }
                  }
                 */
            }
            // ************************

            if(ret!=null && ret.getType()==TypeInfo.TYPE_REFERENCE) {
                if(ir.hasAttribute(IRAttribute.NEED_ICELL))
                    out.Print(ret + "_ICell->ref_DONT_ACCESS_DIRECTLY = " + ret + ";");
            }
            return true;
        } else {
            return false;
        }
    }

    private void doInvokeInterface() {
        out.Print("{");
        out.Print("static int guess;");
        out.Print("CVMUint16 methodTableIndex;");
        out.Print("CVMMethodBlock *mb;");

        String cb = ((CVMClass)(calleeClassinfo.vmClass)).getNativeName() + "_Classblock";
        out.Print("extern const CVMClassBlock " + cb + ";");
        out.Print("const CVMClassBlock *icb = &" + cb + ";");
        //out.Print("CVMMethodBlock *imb = CVMcpGetMb(cp, " + ir.getBytecode().getOperand(0) + ");");
        //out.Print("CVMClassBlock *icb = CVMmbClassBlock(imb);");

        out.Print("CVMClassBlock *cb = (CVMClassBlock*)CVMobjectGetClass(" + obj + ");");

        out.Print("if(CVMcbInterfacecb(cb, guess) != icb) {");
        out.Print("int i;");
        out.Print("for(i = CVMcbInterfaceCount(cb) - 1; ; i--) {");

        exceptionHandler.checkIncompatibleClassChangeError(ir, "i");

        out.Print("if(CVMcbInterfacecb(cb, i) == icb) {");
        out.Print("guess = i;");
        out.Print("break;");
        out.Print("}");
        out.Print("}");
        out.Print("}");

        out.Print("methodTableIndex = CVMcbInterfaceMethodTableIndex(cb, guess, " + calleeMethodinfo.methodTableIndex + ");");
        //out.Print("methodTableIndex = CVMcbInterfaceMethodTableIndex(ocb, guess, CVMmbMethodSlotIndex(imb));");
        out.Print("mb = CVMcbMethodTableSlot(cb, methodTableIndex);");
        /*
        if (AOTCWriter.PROFILE_CALL_COUNT) {
            out.Print("{");
            out.Print("//INSTRUMENTATION TO PROFILE INTERFACE CALL");
            out.Print("increaseVirtualCallCounter(\"" + classinfo.className + "\", \"" 
                    + methodinfo.name + methodinfo.type.string + "\", \"" + ir.getBpc() + "\", cb, mb);");
            out.Print("}");
        }
        */

        out.Print("// UNKNOWN(AOTC) calls");
        out.Print("if(CVMmbInvokerIdx(mb)==CVM_INVOKE_AOTC_METHOD) {");
        doAOTCInvoke(true);
        out.Print("} else {");
        doJAVAInvoke(false);
        out.Print("}");
        out.Print("}");
    }

    private void doAOTCStaticCall() {
        boolean inlined = false;
        // run static class initializer, if required
        if(checkInit && !ir.hasAttribute(IRAttribute.PRELOADED) &&
                !ir.hasAttribute(IRAttribute.REDUNDANT_CLASSINIT)) {
            doClassInitCheck();
            //out.Print("{");
            //out.Print("extern const CVMClassBlock " + cb + ";");
            //out.Print("if(CVMcbInitializationNeeded(&" + cb + ", ee)) {");
            //out.Print("AOTCclassInit(ee, (CVMClassBlock*)(&" + cb + "));");
            //out.Print("}");
            //out.Print("}");
        }

        //if (!AOTCWriter.PROFILE_CALL_COUNT && AOTCWriter.INLINE_STATICCALL) { // turn off inlining when counting call count
        if (AOTCWriter.INLINE_STATICCALL) { // turn off inlining when counting call count
            if (AOTCWriter.USE_CALL_COUNT_INFO) {
                int profileSize = calleeProfileList.size();
                if(profileSize==0) {
                    inlined = doInline(0, 0);
                } else if(profileSize==1) {
                    CalleeInfo info = (CalleeInfo)calleeProfileList.elementAt(0);
                    inlined = doInline(info.getCallCount(), 0);
                } else {
                    new aotc.share.Assert(false, "Static method but it has two profile info!!");
                }
            } else {
                inlined = doInline(-1, 0);
            }
        }
        if (inlined) {
            out.Print("// static call inline *************************************");
        } else { 
            int staticType = possibleCalltype; // static call only only 1 type of call
            /*
            if (AOTCWriter.PROFILE_CALL_COUNT) {
                out.Print("{");
                out.Print("//INSTRUMENTATION TO PROFILE STATIC CALL");
                String cbName = classinfo.className.replace('/', '_');
                cbName = cbName.replace('$', '_') + "_Classblock";
                out.Print("extern const CVMClassBlock " + cbName + ";");
                out.Printf("CVMConstantPool *cp = CVMcbConstantPool(&" + cbName + ");");
                out.Print("CVMMethodBlock *mb = CVMcpGetMb(cp, " + ir.getBytecode().getOperand(0) + ");");
                out.Print("increaseStaticCallCounter(\"" + classinfo.className + "\", \"" 
                        + methodinfo.name + methodinfo.type + "\", \"" + ir.getBpc() 
                        + "\", \"" + calleeClassinfo.className + "\", mb);");
                out.Print("}");
            }
            */
            // AOTC type static methods which was CNI methods.
            if (tryNativeCall(calleeClassinfo.className, calleeMethodinfo.name.string, calleeMethodinfo.type.string, true)) {
                //doGCCheckAfterCall(false);
                //doExceptionCheckAfterCall(false);
                doCheckAfterCall(null, true, true);
                return;
            }

            if(staticType == CALLTYPE_JAVA || staticType == CALLTYPE_CNI) {
                out.Print("// Interpreter or CNI type static call");
                out.Print("{");
                doJAVAInvoke(true);
                out.Print("}");
            } else if(staticType == CALLTYPE_AOTC) {
                out.Print("// AOTC type static call");
                out.Print("{");
                doAOTCInvoke(false);
                out.Print("}");
            } else if (staticType == CALLTYPE_JNI) {
                out.Print("// JNI type static call");
                out.Print("{");
                doJNIInvoke(false);
                out.Print("}");
            } else {
                System.err.println("staticType = " + staticType);
                new aotc.share.Assert(false, "more than one type or none");
            }
        }
        // Exception & GC check after calls
        //doGCCheckAfterCall(false);
        //if(!inlined) {
        //   doExceptionCheckAfterCall(false);
        //}
        doCheckAfterCall(calleeMethodinfo, true, (!inlined));
    }

    private String callOrder = null;

    private void doAOTCVirtualCall() {
        int cbCompares = 0;
        int count = 0;

        out.Print("{");
        out.Print("CVMClassBlock *cb = (CVMClassBlock*)CVMobjectGetClass(" + obj + ");");
        /*
        if (AOTCWriter.PROFILE_CALL_COUNT) {
            out.Print("{");
            out.Print("//INSTRUMENTATION TO PROFILE VIRTUAL CALL");
            out.Print("CVMClassBlock *cbForProfile = (CVMClassBlock*)CVMobjectGetClass(" + obj + ");");
            out.Print("CVMMethodBlock *mbForProfile = CVMcbMethodTableSlot(cb, " + ir.getBytecode().getOperand(0) + ");");
            out.Print("increaseVirtualCallCounter(\"" + classinfo.className + "\", \"" 
                    + methodinfo.name + methodinfo.type + "\", \"" + ir.getBpc() + "\", cbForProfile, mbForProfile);");
            out.Print("}");
        }
        */
        
        if(AOTCWriter.USE_CALL_COUNT_INFO) {
            cbCompares = getNumberOfClassBlockCompares();
            int size = calleeProfileList.size();

            CalleeInfo info[] = new CalleeInfo [size];
            ClassInfo cls[] = new ClassInfo [size];
            MethodInfo method[] = new MethodInfo [size];
            String cbname[] = new String [size];
            String className[] = new String [size];
            String methodName[] = new String [size];
            String typeName[] = new String [size];



            for(int i=0; i<size; i++) {
                info[i] = (CalleeInfo)calleeProfileList.elementAt(i);
                cls[i] = getClassInfoFromProfile(info[i]);
                if(cls[i]==null) {
                    continue;
                }
                method[i] = getMethodInfoFromProfile(info[i]);
                new aotc.share.Assert(method[i]!=null, "Can't find the method block for current calls!!");

                className[i] = method[i].parent.className;
                methodName[i] = info[i].getTargetMethod();
                typeName[i] = info[i].getTargetType();
                //if(!ClassFilter.inList(className[i], methodName[i], typeName[i]) && info[i].getCallType().equals("AOTC")) {
                if(((method[i].access & Const.ACC_NATIVE) == 0 && (method[i].access & Const.ACC_AOTC) == 0) || MethodTranslator.nativeTypes.isType(calleeClassinfo.className, "CNI") ) {
                    cls[i] = null;
                    continue;
                }
                cbname[i] = ((CVMClass)(cls[i].vmClass)).getNativeName() + "_Classblock";
                if(i<cbCompares ||
                        tryNativeCall(className[i], methodName[i], typeName[i], false)) {
                    out.Print("extern const CVMClassBlock " + cbname[i] + ";");
                } else {
                    cls[i] = null;
                }
            }
            // by uhehe99 
            // to estimate candidate for preexistence-based inlining
            // count call-site where only single callee is called, but should be guarded.
            if (size == 1) {
                singleTarget++;
            } else {
                multipleTarget++;
            }

            for(int i=0; i<size; i++) {
                if(cls[i]==null) continue;
                if(count==0) {
                    if(useBranchHint) {
                        out.Print("if (likely(cb == (CVMClassBlock*) &" + cbname[i] + ")) {");
                    } else {
                        out.Print("if (cb == (CVMClassBlock*) &" + cbname[i] + ") {");
                    }
                } else {
                    out.Print("} else if (cb == (CVMClassBlock*) &" + cbname[i] + ") {");
                }

                boolean ret;
                calleeClassinfo = method[i].parent;
                calleeMethodinfo = method[i];
                if (AOTCWriter.INLINE_VIRTUALCALL) {
                    ret = doInline(info[i].getCallCount(), i+1);
                } else {
                    ret = false;
                }
                if(!ret)
                    ret = tryNativeCall(className[i], methodName[i], typeName[i], true);
                else
                    out.Print("// virtual call inline ************************************");
                if(i<cbCompares && !ret) {
                    //if(info[i].getCallType().equals("AOTC")) {
                    MethodInfo mInfo = getMethodInfoFromProfile(info[i]);
                    if((mInfo.access & Const.ACC_AOTC) != 0) {
                        doAOTCInvoke(false);
                    //} else if(info[i].getCallType().equals("JNI")) {
                    } else if(isJNI(mInfo)) {
                        doJNIInvoke(false);
                    } else {
                        doJAVAInvoke(true);
                    }
                }
                count++;
            }
        } else {
            if(tryNativeVirtualCall()) {
                count++;
            }
        }
        if(count>0) out.Print("} else {");
        if (!ir.hasAttribute(IRAttribute.PRELOADED)) {
            out.Print("CVMMethodBlock *mb = CVMcbMethodTableSlot(cb, " + ir.getBytecode().getOperand(0) + ");");
        }
        /*
           if (AOTCWriter.PROFILE_CALL_COUNT) {
           out.Print("{");
           out.Print("//INSTRUMENTATION TO PROFILE VIRTUAL CALL");
           out.Print("CVMClassBlock *cbForProfile = (CVMClassBlock*)CVMobjectGetClass(" + obj + ");");
           out.Print("CVMMethodBlock *mbForProfile = CVMcbMethodTableSlot(cb, " + ir.getBytecode().getOperand(0) + ");");
           out.Print("increaseVirtualCallCounter(\"" + classinfo.className + "\", \"" 
           + methodinfo.name + methodinfo.type + "\", \"" + ir.getBpc() + "\", cbForProfile, mbForProfile);");
           out.Print("}");
           }
         */
    
        //if(!AOTCWriter.USE_CALL_COUNT_INFO || classinfo.className.equals("Test")) {
            //callOrder = "";
            ///*if ((possibleCalltype & CALLTYPE_AOTC) != 0)*/ callOrder += "a";
            //if ((possibleCalltype & CALLTYPE_JNI) != 0) callOrder = "j" + callOrder;
            //callOrder += "c";
        //} 
        if(AOTCWriter.METHOD_ARGUMENT_PASSING != AOTCWriter.WRAPPER_TYPE) {
            boolean useElse = false;
            for(int i=0; i<callOrder.length(); i++) {
                if(callOrder.charAt(i)=='a') {
                    out.Print( (useElse ? "} else " : "") + "if(CVMmbInvokerIdx(" + MB + ")==CVM_INVOKE_AOTC_METHOD) {");
                    doAOTCInvoke(true);
                } else if(callOrder.charAt(i)=='j') {
                    out.Print((useElse ? "} else " : "") + "if(CVMmbInvokerIdx(" + MB + ")==CVM_INVOKE_JNI_METHOD) {");
                    doJNIInvoke(true);
                } else if(callOrder.charAt(i)=='c') {
                    out.Print((useElse ? "} else " : "") + "{");
                    doJAVAInvoke(false);
                    out.Print("}");
                }
                useElse = true;
            }
        }else {
            // new optimized invoke virtual
            String functionName = "(*func)";
            doFuncDefine(functionName, false);
            // Call Arg - BY Clamp *****
            String argument = "ee, ";
            argument += (obj + "_ICell");
            for(int i=0; i<args.length; i++) {
                if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                    argument += (", " + args[i] + "_ICell");
                } else {
                    argument += (", " + args[i]);
                }
            }
            //out.Print("func = (void*)CVMmbNativeCode(" + MB + ");");
            out.Print("ee->callee = " + MB + ";");
            //out.Print("fprintf(stderr, \"INVOKE AOTC TEST" + (staticIdx++) + "\\n\");");
            out.Print("func = (void*)" + MB + "->immutX.invokerAOT;");
            if(ret == null) {
                out.Print(functionName + "(" + argument + ");");
            } else {
                // Call Arg - BY Clamp *****
                out.Print(ret + " = " + functionName + "(" + argument + ");");
                // ***************************
            }
            if(ret!=null && ret.getType()==TypeInfo.TYPE_REFERENCE) {
                if(ir.hasAttribute(IRAttribute.NEED_ICELL))
                    out.Print(ret + "_ICell->ref_DONT_ACCESS_DIRECTLY = " + ret + ";");
            }
        }
        //doGCCheckAfterCall(true);
        if(count>0) out.Print("}");
        out.Print("}");
    }

    // AOTC method call
    private void doAOTCInvoke(boolean useMb) {
        String functionName;
        if(useMb) {
            functionName = "(*func)";
        } else if(MethodTranslator.nativeTypes.isType(calleeClassinfo.className, "CNI")) {
            // Call Arg - BY Clamp *****
            if(AOTCWriter.METHOD_ARGUMENT_PASSING != AOTCWriter.CNI_TYPE) {
                functionName = "CNI" + calleeMethodinfo.getNativeName(true).substring(5);
            }else{
                functionName = "Java_" + calleeMethodinfo.getNativeName(true).substring(5);
            }
            // *************************
        } else {
            functionName = calleeMethodinfo.getNativeName(true);
        }
        doFuncDefine(functionName, false);
        // Call Arg - BY Clamp *****
        String argument = "ee, ";
        if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.MIX_TYPE) {
            argument += "NULL, NULL,";
        }else if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.CNI_TYPE) {
            argument += "callArg";
        }
        if(AOTCWriter.METHOD_ARGUMENT_PASSING != AOTCWriter.CNI_TYPE) {
            if(isStatic) {
                String cb = ((CVMClass)(calleeClassinfo.vmClass)).getNativeName() + "_Classblock";
                out.Print("extern const CVMClassBlock " + cb + ";");
                argument += (cb + ".javaInstanceX");
            } else { // isVirtual || isNonvirtual
                argument += (obj + "_ICell");
            }
            for(int i=0; i<args.length; i++) {
                if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                    argument += (", " + args[i] + "_ICell");
                } else {
                    argument += (", " + args[i]);
                }
            }
        }else {
            int j = 0;
            if(isStatic){
                out.Print("CVMStackVal32 callArg["+ (args.length + 2) +"];");
            }
            else{
                out.Print("CVMStackVal32 callArg["+ (args.length+2) +"];//c8c8");
                out.Printf("callArg["+j+"].j.r.ref_DONT_ACCESS_DIRECTLY = "+obj+";");//added
                j++;
            }

            for(int i=0 ; i<args.length; i++) {
                if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                    out.Printf("callArg["+j+"].j.r.ref_DONT_ACCESS_DIRECTLY = "+args[i]+";");//added
                } else {
                    if(args[i].getType()==TypeInfo.TYPE_FLOAT)
                        out.Printf("callArg["+j+"].j.f = "+args[i]+";"); //added
                    else if(args[i].getType()==TypeInfo.TYPE_LONG){
                        out.Printf("callArg["+j+"].j.i = "+args[i]+"& 0xffffffff;");
                        out.Printf("callArg["+(j+1)+"].j.i = ("+args[i]+"& 0xffffffff00000000)>>32;");
                        j++;
                    }
                    else if(args[i].getType()==TypeInfo.TYPE_DOUBLE){
                        out.Printf("CNIDouble2jvm(callArg+"+j+", "+args[i]+");");
                        j++;
                    }
                    else
                        out.Printf("callArg["+j+"].j.i = "+args[i]+";"); //added
                }
                j++;
            }

        }
        // **************************

        if(useMb) {
            out.Print("func = (void*)CVMmbNativeCode(" + MB + ");");
        }
        if(ret == null) {
            out.Print(functionName + "(" + argument + ");");
        } else {
            // Call Arg - BY Clamp *****
            if(AOTCWriter.METHOD_ARGUMENT_PASSING != AOTCWriter.CNI_TYPE) {
                if(ret.getType()==TypeInfo.TYPE_REFERENCE) {
                    out.Print(ret + " = (CVMObject*)" + functionName + "(" + argument + ");");
                } else {
                    out.Print(ret + " = " + functionName + "(" + argument + ");");
                }
            }else{
                out.Printf(functionName + "(ee, callArg);//c7c7");
                if(ret.getType()==TypeInfo.TYPE_REFERENCE) {
                    out.Print(ret +"= callArg[0].j.r.ref_DONT_ACCESS_DIRECTLY;");
                }
                else{
                    if(ret.getType()==TypeInfo.TYPE_FLOAT)
                        out.Printf(ret +"= callArg[0].j.f;");
                    else if(ret.getType()==TypeInfo.TYPE_LONG){
                        out.Printf("CNIjvm2Long(callArg,"+ret+");");
                    }
                    else if(ret.getType()==TypeInfo.TYPE_DOUBLE){
                        out.Printf("CNIjvm2Double(callArg, "+ret+");");
                    }
                    else
                        out.Print(ret +"= callArg[0].j.i;");
                }
            }
            // ***************************
        }
        if(ret!=null && ret.getType()==TypeInfo.TYPE_REFERENCE) {
            if(ir.hasAttribute(IRAttribute.NEED_ICELL))
                out.Print(ret + "_ICell->ref_DONT_ACCESS_DIRECTLY = " + ret + ";");
        }
    }

    // JNI method call
    private void doJNIInvoke(boolean useMb) {
        String functionName;
        if(useMb) {
            functionName = "(*func)";
        } else {
            functionName = calleeMethodinfo.getNativeName(true);
        }
        doFuncDefine(functionName, true);

        if(ret!=null && ret.getType()==TypeInfo.TYPE_REFERENCE) {
            out.Print("jobject ret;");
        }

        String argument = "CVMexecEnv2JniEnv(ee), ";
        String cb = ((CVMClass)(calleeClassinfo.vmClass)).getNativeName() + "_Classblock";
        if(isStatic) {
            out.Print("extern const CVMClassBlock " + cb + ";");
            argument += (cb + ".javaInstanceX");
        } else { // isVirtual || isNonvirtual
            argument += (obj + "_ICell");
        }
        for(int i=0; i<args.length; i++) {
            if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                argument += (", " + args[i] + " ? " + args[i] + "_ICell : NULL");
            } else {
                argument += (", " + args[i]);
            }
        }

        if(useMb) {
            out.Print("func = (void*)CVMmbNativeCode(" + MB + ");");
        }
        if(ret == null) {
            out.Print("CVMD_gcSafeExec(ee, {");
            out.Print(functionName + "(" + argument + ");");
            out.Print("} );");
        } else if(ret.getType()==TypeInfo.TYPE_REFERENCE) {
            out.Print("CVMD_gcSafeExec(ee, {");
            out.Print("ret = " + functionName + "(" + argument + ");");
            out.Print("} );");
            out.Print(ret + " = ret ? ret->ref_DONT_ACCESS_DIRECTLY : NULL;");
            if(ir.hasAttribute(IRAttribute.NEED_ICELL))
                out.Print(ret + "_ICell->ref_DONT_ACCESS_DIRECTLY = " + ret + ";");
        } else {
            out.Print("CVMD_gcSafeExec(ee, {");
            out.Print(ret + " = " + functionName + "(" + argument + ");");
            out.Print("} );");
        }
    }

    // Interpreted method call & some CNI mehtod call
    // Most of CNI calls are processed by doNativeInterface
    private void doJAVAInvoke(boolean loadBlock) {
        String objName;

        if(loadBlock) {
            if(isVirtual) {
                out.Print("CVMMethodBlock *mb = CVMcbMethodTableSlot(cb, " + ir.getBytecode().getOperand(0) + ");");
            } else {
                //cfgInfo.setConstantPool();
                out.Print("CVMMethodBlock *mb = CVMcpGetMb(cp, " + ir.getBytecode().getOperand(0) + ");");
                /*	out.Print("CVMMethodBlock *mb = CVMcpGetMb(cp, " + ir.getBytecode().getOperand(0) + ");");
                    MethodConstant mc = ((MethodConstant)classinfo.constants[ir.getBytecode().getOperand(0)]);
                    new aotc.share.Assert(mc.isResolved(), "");
                    MethodInfo mi = mc.find();
                    CVMMethodInfo em = (CVMMethodInfo)(mi.vmMethodInfo);
                    CVMClass      ec = (CVMClass)(mi.parent.vmClass);
                    int n = mi.index;
                    String mbName = ec.getNativeName()+"_methods";
                    out.Print("extern const struct CVMMethodRange " + mbName + ";");
                //out.Print("CVMMethodBlock* " + MB + " = &" + mbName + ".ranges.mb" + (n/256+1) + "["+n%256+"];");
                out.Print("CVMMethodBlock* " + MB + " = &" + mbName + ".mb["+n+"];");*/
            }
        }

        if(ret!=null)
            out.Print("AOTCreturn ret;");

        if(isStatic) {
            String cb = ((CVMClass)(calleeClassinfo.vmClass)).getNativeName() + "_Classblock";
            out.Print("extern const CVMClassBlock " + cb + ";");
            objName = (cb + ".javaInstanceX");
        } else { // isVirtual || isNonvirtual
            objName = (obj + "_ICell");
        }

        String argument = "           ";
        for(int i=0; i<args.length; i++) {
            if(args[i].getType()==TypeInfo.TYPE_REFERENCE) {
                argument += (", " + args[i] + "_ICell");
            } else {
                argument += (", " + args[i]);
            }
        }

        String type = MethodDeclarationInfo.methodTypeList(mc.sig.type.string);
        out.Print("AOTCInvoke(ee, " + objName + ", " + MB + ", " + TypeInfo.getCVMRetTypeName(type.charAt(0))
                + ", " + (ret!=null ? "&ret" : "NULL") + ", "
                + (isStatic ? "1" : "0") + ", " + (isVirtual ? "1" : "0"));
        out.Print(argument + ");");

        if(ret != null) {
            switch(ret.getType()) {
                case TypeInfo.TYPE_BOOLEAN:
                case TypeInfo.TYPE_BYTE:
                case TypeInfo.TYPE_CHAR:
                case TypeInfo.TYPE_SHORT:
                case TypeInfo.TYPE_INT:
                    out.Print(ret + " = ret.i;");
                    break;
                case TypeInfo.TYPE_LONG:
                    out.Print(ret + " = ret.j;");
                    break;
                case TypeInfo.TYPE_FLOAT:
                    out.Print(ret + " = ret.f;");
                    break;
                case TypeInfo.TYPE_DOUBLE:
                    out.Print(ret + " = ret.d;");
                    break;
                case TypeInfo.TYPE_REFERENCE:
                    out.Print(ret + " = ret.l;");
                    if(ir.hasAttribute(IRAttribute.NEED_ICELL))
                        out.Print(ret + "_ICell->ref_DONT_ACCESS_DIRECTLY = ret.l;");
                    break;
            }
        }
        if(AOTCWriter.EXCEPTION_IMPLEMENTATION == AOTCWriter.SETJMP_PROLOG_HANDLING || AOTCWriter.EXCEPTION_IMPLEMENTATION == AOTCWriter.SETJMP_TRY_HANDLING) {
            doExceptionCheckAfterCall();
        }

    }

    private void doFuncDefine(String funcName, boolean isJNI) {
        String type = MethodDeclarationInfo.methodTypeList(mc.sig.type.string);
        String typelist = "";
        for(int i=1; i<type.length(); i++) {
            typelist += (", " + TypeInfo.getArgTypeName(type.charAt(i)));
        }
        if(isJNI) {
            out.Print(TypeInfo.getRetTypeName(type.charAt(0)) + " " + funcName + "(JNIEnv*, jclass" + typelist + ");");
        } else {
            // Call Arg - BY Clamp *****
            if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.JNI_TYPE 
                    || AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.WRAPPER_TYPE) {
                out.Print(TypeInfo.getRetTypeName(type.charAt(0)) + " " + funcName + "(CVMExecEnv*, jclass" + typelist + ");");
            }else if(AOTCWriter.METHOD_ARGUMENT_PASSING == AOTCWriter.CNI_TYPE) {
                out.Print("CNIResultCode "+ funcName + "(CVMExecEnv* ee, CVMStackVal32 *arguments);");
            }else {
                out.Print(TypeInfo.getRetTypeName(type.charAt(0)) + " " + funcName + "(CVMExecEnv*, CVMStackVal32*, int*, jclass" + typelist + ");");
            }
            // *************************
        }
    }

    private int getNumberOfClassBlockCompares() {
        
        int size = calleeProfileList.size();
        int totalCount = 0;
        int aotcCalls = 0, jniCalls = 0, javaCalls = 0;
        int count[] = new int [size];
        int type[] = new int[size];
        int i;
        
        for(i = 0 ; i < size ; i++) {
            CalleeInfo info = (CalleeInfo)calleeProfileList.elementAt(i);
            int callCount = info.getCallCount();
            MethodInfo mInfo = getMethodInfoFromProfile(info);
            if(mInfo != null) {
                if((mInfo.access & Const.ACC_AOTC) != 0) {
                    aotcCalls += callCount;
                    type[i] = CALLTYPE_AOTC;
                }else if(isJNI(mInfo)) {
                    jniCalls += callCount;
                    type[i] = CALLTYPE_JNI;
                }else {
                    javaCalls += callCount;
                    type[i] = CALLTYPE_JAVA;
                }
            }else {
                javaCalls += callCount;
                type[i] = CALLTYPE_JAVA;
            }
            count[i] = callCount;
            totalCount += callCount;
        }


        double threshold = 0.4;
        int bestCompare = 0;
        //useBranchHint = true;
        
        for(i = 0 ; i < size ; i++) {
            if(((double)count[i] / totalCount) < threshold /*|| type[i] == CALLTYPE_JAVA*/) {
                bestCompare = i;
                break;
            }
            /*
            if(type[i] == CALLTYPE_AOTC) {
                aotcCalls -= count[i];
            }else if(type[i] == CALLTYPE_JNI) {
                jniCalls -= count[i];
            }else {
                javaCalls -= count[i];
            }
            */
            //threshold -= 0.1;
        }
        
        /*
        String order = "ajc";
        if(aotcCalls < jniCalls) {
            order = "jac";
            if(aotcCalls < javaCalls) {
                order = "jca";
                if(jniCalls < javaCalls) {
                    order = "cja";
                }
            }
        } else {
            if(jniCalls < javaCalls) {
                order = "acj";
                if(aotcCalls < javaCalls) {
                    order = "caj";
                }
            }
        }
        */
        String order = "aj";
        if(aotcCalls < jniCalls) {
            order = "ja";
        }

        if(aotcCalls==0) order = order.replace('a', ' ');
        if(jniCalls==0) order = order.replace('j', ' ');
        //if(javaCalls==0) order = order.replace('c', ' ');
        callOrder = order.trim();
        callOrder += "c";
        return bestCompare;

        
        /*
        int size = calleeProfileList.size();
        int totalCount = 0;
        int aotcCalls[] = new int [size+1];
        int jniCalls[] = new int [size+1];
        int javaCalls[] = new int [size+1];
        int count[] = new int [size];
        for(int i=size-1; i>=0; i--) {
            CalleeInfo info = (CalleeInfo)calleeProfileList.elementAt(i);
            //String callType = info.getCallType();
            int callCount = info.getCallCount();
            aotcCalls[i] = aotcCalls[i+1];
            jniCalls[i] = jniCalls[i+1];
            javaCalls[i] = javaCalls[i+1];
            MethodInfo mInfo = getMethodInfoFromProfile(info);
            if(mInfo != null) { 
                if((mInfo.access & Const.ACC_AOTC) != 0) {
                    aotcCalls[i] += callCount;
                }else if(isJNI(mInfo)) {
                    jniCalls[i] += callCount;
                } else {
                    javaCalls[i] += callCount;
                }
            }else {
                System.out.println("no mInfo: " + info.getTargetClass() + " " + info.getTargetMethod() + " " + info.getTargetType());
                javaCalls[i] += callCount;
            }
            totalCount += callCount;
            count[i] = callCount;
        }
        

        if(size>0 && ((double)count[0])/totalCount > 0.9) {
            useBranchHint = true;
        }

        // Assumption : there is rare CNI, JAVA methods
        int bestCompare = 0;
        double bestCost = Double.MAX_VALUE;
        double cost[] = new double [count.length+1];
        String order = null;
        for(int i=0; i<=count.length; i++) {
            double costInc = 0.0;
            int j;
            for(j=0; j<i; j++) {
                costInc += 3.0;
                cost[i] += costInc*count[j]/totalCount;
            }
            costInc += 2.0;
            order = "ajc";
            if(aotcCalls[j]<jniCalls[j]) {
                order = "jac";
                if(aotcCalls[j]<javaCalls[j]) {
                    order = "jca";
                    if(jniCalls[j]<javaCalls[i]) {
                        order = "cja";
                    }
                }
            } else {
                if(jniCalls[j]<javaCalls[j]) {
                    order = "acj";
                    if(aotcCalls[j]<javaCalls[j]) {
                        order = "caj";
                    }
                }
            }
            for(int k=0; k<3; k++) {
                costInc += 3.0;
                if(order.charAt(k)=='a') cost[i] += costInc*aotcCalls[j]/totalCount;
                else if(order.charAt(k)=='j') cost[i] += costInc*jniCalls[j]/totalCount;
                else cost[i] += costInc*javaCalls[j]/totalCount;
            }

            // code size factor : 0.1 cycle per cb comprare
            if(bestCost-0.1>cost[i]) {
                bestCost = cost[i];
                bestCompare = i;
            }
            if(totalCount<1000)
                break;
        }
        if(aotcCalls[bestCompare]==0) order = order.replace('a', ' ');
        if(jniCalls[bestCompare]==0) order = order.replace('j', ' ');
        if(javaCalls[bestCompare]==0) order = order.replace('c', ' ');
        callOrder = order.trim();
        if(callOrder.indexOf('c')==-1) callOrder += "c";
        if(callOrder.equals("c")) {
            while(bestCompare>0) {
                //String callType = ((CalleeInfo)calleeProfileList.elementAt(bestCompare-1)).getCallType();
                //if(!callType.equals("AOTC") && !callType.equals("JNI")) {
                MethodInfo mInfo = getMethodInfoFromProfile((CalleeInfo)calleeProfileList.elementAt(bestCompare - 1));
                if(mInfo == null || (mInfo.access & Const.ACC_AOTC) == 0 && !isJNI(mInfo)) {
                    bestCompare--;
                } else {
                    break;
                }
            }
        }
        new aotc.share.Assert(callOrder.length()==1 || callOrder.endsWith("c"), "I can't handle this situation!!");

        out.Print("// ==> cb compare : " +  bestCompare + ", call order : " + callOrder + ", cost : " + bestCost);
        return bestCompare;
        */
    }

    private void doClassInitCheck() {
        if (calleeClassinfo.equals(this.classinfo)) {
            return;
        }
        String cb = ((CVMClass)(calleeClassinfo.vmClass)).getNativeName() + "_Classblock";
        for (int i = 0 ; i < Preloader.PRELOADED_AT_VM_INITIALIZATION.length; i++) {
            if (cb.equals(Preloader.PRELOADED_AT_VM_INITIALIZATION[i])) {
                return;
            }
        }
        out.Print("{");
        out.Print("extern const CVMClassBlock " + cb + ";");
        out.Print("if(CVMcbInitializationNeeded(&" + cb + ", ee)) {");
        out.Print("AOTCclassInit(ee, (CVMClassBlock*)(&" + cb + "));");
        out.Print("}");
        out.Print("}");
    }

    public static ClassInfo getClassInfoFromProfile(CalleeInfo ci) {
        //return ClassInfo.lookupClass(ci.getTargetClass());
        return ClassTable.lookupClass(ci.getTargetClass());
    }
    public static MethodInfo getMethodInfoFromProfile(CalleeInfo ci) {
        //ClassInfo cls = lookupClass(ci.getTargetClass());
        ClassInfo cls = ClassTable.lookupClass(ci.getTargetClass());
        if(cls==null) return null;

        String methodName = ci.getTargetMethod();
        String typeName = ci.getTargetType();

        MethodInfo method = null;
        while(cls!=null) {
            for(int j=0; j<cls.methods.length; j++) {
                if(methodName.equals(cls.methods[j].name.string) &&
                        typeName.equals(cls.methods[j].type.string)) {
                    method = cls.methods[j];
                    break;
                }
            }
            if(method==null) {
                cls = cls.superClassInfo;
            } else {
                break;
            }
        }
        return method;
    }
    public static boolean isJNI(MethodInfo mInfo) {
        ClassInfo cInfo = mInfo.parent;
        if((mInfo.access & Const.ACC_NATIVE) == 0 || (mInfo.access & Const.ACC_AOTC) != 0) {
            return false;
        }
        if(cInfo.className.startsWith("CNI")) {
            return false;
        }
        return true;
    }
}