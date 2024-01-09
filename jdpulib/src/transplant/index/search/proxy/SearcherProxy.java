package transplant.index.search.proxy;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.ProxyHelper;
import framework.pim.dpu.RPCHelper;
import transplant.index.search.IndexTable;
import transplant.index.search.Searcher;
import transplant.index.search.pojo.SearchResult;

import java.util.List;

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
        RPCHelper.invokeMethod(dpuID, address, "package transplant.index.search.Searcher", "getTable():Ltransplant.index.search.IndexTable;\n");
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID);
        return (IndexTable) proxy;
    }

    @Override
    public SearchResult searchDocumentIds(int[] keywordIDs) {
        RPCHelper.invokeMethod(dpuID, address, "package transplant.index.search.Searcher", "getTable():Ltransplant.index.search.IndexTable;\n");
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID);
        return (SearchResult) proxy;
    }
}
