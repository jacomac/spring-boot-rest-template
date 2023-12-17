package sprest.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import sprest.api.AccessRightEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * Central Singleton to access all user privileges available in the system.
 * @author wulf
 *
 */
@Slf4j
public class AllAccessRights {
    private static AllAccessRights instance;
    private List<String> rights;

    private AllAccessRights() {
    }

    public static synchronized AllAccessRights getInstance() {
        if (instance == null) {
            instance = new AllAccessRights();
        }
        return instance;
    }

    /**
     * Get all access rights available in ths system, across all modules
     * @return all access rights as stings that have been aggregated from enums annotated with {@link AccessRightEnum } and are located in a sprest.user package
     */
    public synchronized List<String> getValues() {
        if (rights == null) {
            rights = new ArrayList<String>();

            Reflections reflections = new Reflections("sprest.user", new TypeAnnotationsScanner(), new SubTypesScanner());
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(AccessRightEnum.class);
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
