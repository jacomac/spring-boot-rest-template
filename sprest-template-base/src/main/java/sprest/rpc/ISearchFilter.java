package sprest.rpc;

/**
 * Marker Interface for all search filters used in rpc calls
 * Currently all search filters also need to explicily activate the filter using the useFilter-Flag, as Spring always populates the Optional filter (workaround). 
 * @author wulf
 *
 */
public interface ISearchFilter {
    public boolean isUseFilter();
	public void setUseFilter(boolean useFilter);
}
