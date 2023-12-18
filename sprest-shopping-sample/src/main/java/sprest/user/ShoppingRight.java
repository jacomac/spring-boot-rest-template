package sprest.user;

import sprest.api.AccessRightEnum;

/**
 * An enumeration of access rights in this shopping module
 */
@AccessRightEnum
public enum ShoppingRight {

	/**
	 * Can use the personal shopping list functionality
	 */
	ACCESS_SHOPPING;

	public class values {
		public static final String ACCESS_SHOPPING = "ACCESS_SHOPPING";
	}
}
