package application.transplant.index.search.proxy;

import framework.pim.struct.IDPUProxyObject;
import framework.pim.dpu.ProxyHelper;
import application.transplant.index.search.IndexTable;
import application.transplant.index.search.Searcher;
import application.transplant.index.search.pojo.SearchResult;

public class SearcherProxy extends Searcher implements IDPUProxyObject {
    int dpuID;
    int address;

    public SearcherProxy(IndexTable table) {
        super(table);
    }

    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public IndexTable getTable() {
        ProxyHelper.invokeMethod(dpuID, address, "package application.transplant.index.search.Searcher", "getTable():Ltransplant.index.search.IndexTable;\n");
        IDPUProxyObject proxy = ProxyHelper.getAReturnValue(dpuID);
        return (IndexTable) proxy;
    }

    @Override
    public SearchResult searchDocumentIds(int[] keywordIDs) {
        ProxyHelper.invokeMethod(dpuID, address, "package application.transplant.index.search.Searcher", "getTable():Ltransplant.index.search.IndexTable;\n");
        IDPUProxyObject proxy = ProxyHelper.getAReturnValue(dpuID);
        return (SearchResult) proxy;
    }
}
