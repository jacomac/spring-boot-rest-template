package sprest.data;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class QueryManager<T> {

	/**
	 * perform a multi criteria query against a given Entity T that is pageable, sortable and case insensitive, with the following rules:
	 *
	 * @param filter a class whose only fields are the ones that represent a column in the table of the entity, so with the same name
	 * @param page a regular Spring Pageable, so with page, size and sort[] as Input
	 * @return regular Spring Page with multi-search results
	 */
	public Page<T> findByMultiSearch(Object filter, Pageable page, Class<T> entityClass) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery<T> cq = cb.createQuery(entityClass);
	    Root<T> iRoot = cq.from(entityClass);
		try {
			Predicate[] predArray = getPredicates(filter, cb, iRoot);
		    cq.where(predArray);
		    cq.orderBy(QueryUtils.toOrders(page.getSort(), iRoot, cb));

		    TypedQuery<T> query = em.createQuery(cq);

		    // TODO a pure native count(*)-query may be more efficient
		    int totalRows = query.getResultList().size();

		    query.setFirstResult(page.getPageNumber() * page.getPageSize());
		    query.setMaxResults(page.getPageSize());

		    Page<T> result = new PageImpl<T>(query.getResultList(), page, totalRows);

		    return result;

		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("could not create predicates of query", e);
		}
		return null;
	}

	private Predicate[] getPredicates(Object filter, CriteriaBuilder cb, Root<T> iRoot) throws IllegalArgumentException, IllegalAccessException {
		List<Predicate> predicates = new ArrayList<Predicate>();

		Field[] fields = filter.getClass().getDeclaredFields();

		for (Field f : fields) {
			if ("useFilter".equals(f.getName())) // TODO ugly - find a better solution!
				continue;
			f.setAccessible(true);
			if (String.class.equals(f.getType())) {
				String value = (String) f.get(filter);
				if (StringUtils.isNotEmpty(value)) {
                    String prop = f.getName();
                    if (prop.startsWith("tenant")) {
                        prop = prop.replaceAll("tenant", "");
                        predicates.add(cb.like(cb.lower(iRoot.<String>get("client").get(prop.toLowerCase())), "%" + value.toLowerCase() + "%"));
                    } else {
                        predicates.add(cb.like(cb.lower(iRoot.get(f.getName())),
                                "%" + value.toLowerCase() + "%"));
                    }
                }
			} else if (Boolean.class.equals(f.getType())) {
				Boolean value = (Boolean) f.get(filter);
				if (value != null)
			    	predicates.add(cb.equal(iRoot.<Boolean>get(f.getName()), value));
			} else if (Date.class.equals(f.getType())) {
				Date value = (Date) f.get(filter);
				if (value != null) {
					if (f.getName().endsWith("From"))
						predicates.add(cb.greaterThanOrEqualTo(iRoot.<Date>get(f.getName()), value));
					else if (f.getName().endsWith("Until"))
						predicates.add(cb.lessThanOrEqualTo(iRoot.<Date>get(f.getName()), value));
					else
						predicates.add(cb.equal(iRoot.<Date>get(f.getName()), value));
				}
			} else if (Integer.class.equals(f.getType()) || Integer.TYPE.equals(f.getType()) ) {
				Integer value = (Integer) f.get(filter);
				if (value != null && value > 0) {
					String prop = f.getName();
					if ("clientId".equals(prop)) // TODO ugly - find a better solution!
						prop = "client";
			    	predicates.add(cb.equal(iRoot.<Integer>get(prop), value));
                }
			}
		}

	    Predicate[] predArray = new Predicate[predicates.size()];
	    predicates.toArray(predArray);
		return predArray;
	}

	@Autowired
	private EntityManager em;

}
