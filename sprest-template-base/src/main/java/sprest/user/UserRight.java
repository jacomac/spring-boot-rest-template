package sprest.user;

import sprest.api.UserPrivilegeEnum;

/**
 * <h1>An enumeration of all general access rights a user of the system can have</h1>
 * <br/>
 * Every right follows the uniforms syntax &lt;verb&gt;_&lt;object&gt;, so e.g.
 * MANAGE_USERS. Rights starting with the same verb can also be grouped together
 * using the suffix _ALL, so e.g. MANAGE_ALL
 *
 */
@UserPrivilegeEnum
public enum UserRight {

	// REGION-START: Groupings
	/**
	 * Genereller administrativer Zugriff via Web-UI
	 */
	MANAGE_ALL,

	/**
	 * Alles außer Administration via Web-UI
	 */
	ACCESS_ALL,

	/**
	 * Genereller programmatischer Zugriff auf external API
	 */
	INVOKE_ALL,
	// REGION-END

	// REGION-START: Rights for Administrative Users
	/**
	 * can CRUD users of the app
	 */
	MANAGE_USERS,

    /**
     * can adjust logging at runtime, check system health etc.
     */
    MANAGE_SYSTEM_SETTINGS,

	/**
	 * Can define time-boxed announcements to usually be shown to the user at the login screen
	 */
    MANAGE_ANNOUNCEMENTS;
	// REGION-END

	public class values {
		public static final String MANAGE_ALL = "MANAGE_ALL";
		public static final String ACCESS_ALL = "ACCESS_ALL";
        public static final String INVOKE_ALL = "INVOKE_ALL";
		public static final String MANAGE_ANNOUNCEMENTS = "MANAGE_ANNOUNCEMENTS";
        public static final String MANAGE_SYSTEM_SETTINGS = "MANAGE_SYSTEM_SETTINGS";
		public static final String MANAGE_USERS = "MANAGE_USERS";
	}
}
