package sprest.user;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AllUserRightsTest {

    @Test
    void testGetValues() {
        var singleton = AllUserRights.getInstance();
        List<String> allRights = singleton.getValues();
        assertTrue(allRights.size() > 0);
        assertTrue(allRights.contains(UserRight.MANAGE_ALL.toString()));
        // should be ordered alphabetically
        assertTrue(allRights.get(0).startsWith("ACCESS_"));
        assertTrue(allRights.get(allRights.size()-2).startsWith("MANAGE_"));
    }

}
