package application.transplant.index.search;

import application.transplant.index.search.pojo.SearchResultProxy;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;
import application.transplant.index.search.IndexTable;
import application.transplant.index.search.Searcher;
import application.transplant.index.search.pojo.SearchResult;

import java.util.List;

public class SearcherProxy extends Searcher implements IDPUProxyObject {
    public Integer dpuID;
    public Integer address;

    public SearcherProxy(IndexTable table, List<Document> documents) {
        super(table, documents);
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
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "getTable:()Lapplication/transplant/index/search/IndexTable;\n");
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, IndexTableProxy.class);
        return (IndexTable) proxy;
    }

    @Override
    public SearchResult searchDocumentIds(int... keywordIDs) {

        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "searchDocumentIds:([I)Lapplication/transplant/index/search/pojo/SearchResult;");
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, SearcherProxy.class);
        return (SearchResult) proxy;
    }

    @Override
    public SearchResult search(int w0) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "search:(I)Lapplication/transplant/index/search/pojo/SearchResult;", w0);
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, SearcherProxy.class);
        return (SearchResult) proxy;
    }

    @Override
    public SearchResult search(int w0, int w1) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "search:(II)Lapplication/transplant/index/search/pojo/SearchResult;", w0, w1);
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, SearchResultProxy.class);
        return (SearchResultProxy) proxy;
    }

    @Override
    public SearchResult search(int w0, int w1, int w2) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "search:(III)Lapplication/transplant/index/search/pojo/SearchResult;", w0, w1, w2);
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, SearcherProxy.class);
        return (SearchResult) proxy;
    }

    @Override
    public SearchResult search(int w0, int w1, int w2, int w3) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "search:(IIII)Lapplication/transplant/index/search/pojo/SearchResult;", w0, w1, w2, w3);
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, SearcherProxy.class);
        return (SearchResult) proxy;
    }

    @Override
    public SearchResult search(int w0, int w1, int w2, int w3, int w4) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/Searcher", "search:(IIIII)Lapplication/transplant/index/search/pojo/SearchResult;", w0, w1, w2, w3, w4);
        IDPUProxyObject proxy = RPCHelper.getAReturnValue(dpuID, SearcherProxy.class);
        return (SearchResult) proxy;
    }
}
