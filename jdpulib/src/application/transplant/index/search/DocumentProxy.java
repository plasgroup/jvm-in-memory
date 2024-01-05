package application.transplant.index.search;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;

import java.util.List;

public class DocumentProxy extends Document implements IDPUProxyObject {
    public Integer dpuID;
    public Integer address;

    public DocumentProxy(int id) {
        super(id);
    }

    public DocumentProxy(int id, List<Integer> context) {
        super(id, context);
    }

    @Override
    public void addWord(int wid) {
        RPCHelper.invokeMethod(dpuID, address,"application/transplant/index/search/Document","addWord:(I)V", wid);
    }

    @Override
    public int getDid() {
        RPCHelper.invokeMethod(dpuID, address,"application/transplant/index/search/Document","getDid:()I" );
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public List<Integer> getContext() {
        RPCHelper.invokeMethod(dpuID, address,"application/transplant/index/search/Document","getContext:()Ljava/util/List;" );
        return (List<Integer>) RPCHelper.getAReturnValue(dpuID, ArrayListProxy.class);
    }

    @Override
    public void setContext(List<Integer> context) {
        RPCHelper.invokeMethod(dpuID, address,"application/transplant/index/search/Document","setContext:(Ljava/util/List;)V", context);
    }

    @Override
    public void setDid(int did) {
        RPCHelper.invokeMethod(dpuID, address,"application/transplant/index/search/Document","setDid:(I)V");
    }

    @Override
    public void pushWord(int wordID) {
        RPCHelper.invokeMethod(dpuID, address,"application/transplant/index/search/Document","pushWord:(I)V", wordID);
    }

    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }
}
