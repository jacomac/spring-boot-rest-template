package sprest.user;

import sprest.rpc.ISearchFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchFilter implements ISearchFilter {

    private boolean useFilter = false;

	// make sure for every field that is used in this filter there's an index in the
	// corresponding column of the table in the database!
	private Boolean active;

    private int clientId;
    private String tenantShortcut;
    private String tenantName;

    private String userName;
    private String email;
    private String lastName;
    private String firstName;
    private String title;
    private String rights;
}
