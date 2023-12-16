package application.transplant.index.search.pojo;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.ProxyHelper;
import framework.pim.dpu.RPCHelper;

public class SearchResultProxy extends SearchResult implements IDPUProxyObject {
    public Integer address = -1;
    public Integer dpuID = -1;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public int getTotalMatchedDocuments() {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/pojo/SearchResult","getTotalMatchedDocuments:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public int getFirstMatchedLocations() {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/pojo/SearchResult","getFirstMatchedLocations:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public int getFirstMatchedDocumentId() {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/pojo/SearchResult","getFirstMatchedDocumentId:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public int getTotalMatch() {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/pojo/SearchResult","getTotalMatch:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }
}
