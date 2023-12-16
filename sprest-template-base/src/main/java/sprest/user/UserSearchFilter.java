package sprest.user;

import sprest.api.ISearchFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchFilter implements ISearchFilter {

    private boolean useFilter = false;

	// consider for every field that is used in this filter using an index in the
	// corresponding column of the table in the database!
	private Boolean active;
    private String userName;
    private String email;
    private String lastName;
    private String firstName;
    private String title;
    private String rights;
}
