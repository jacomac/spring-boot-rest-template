package sprest.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import sprest.api.UserPrivilegeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * Central Singleton to access all user privileges available in the system.
 * @author wulf
 *
 */
@Slf4j
public class AllUserRights {
    private static AllUserRights instance;
    private List<String> rights;

    private AllUserRights() {
    }

    public static synchronized AllUserRights getInstance() {
        if (instance == null) {
            instance = new AllUserRights();
        }
        return instance;
    }

    public synchronized List<String> getValues() {
        if (rights == null) {
            rights = new ArrayList<String>();

            Reflections reflections = new Reflections("sprest.user", new TypeAnnotationsScanner(), new SubTypesScanner());
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(UserPrivilegeEnum.class);
            for (Class<?> c : classes) {
                try {
                    Object[] enumValues = c.getEnumConstants();
                    for (Object val : enumValues)
                        rights.add(val.toString());
                } catch (Exception ex) {
                    log.error("could not set up rights system: {}, terminating ...", ex);
                    System.exit(0);
                }
            }
            rights = rights.stream().sorted().collect(Collectors.toList());

            log.debug("available rights in the system: ");
            rights.forEach(log::debug);
        }

        return rights;
    }

}
