package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.SortByField;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdvanceFilterService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<TripRequestAdvancePayment> advancePayments(Filter filter) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TripRequestAdvancePayment> query = criteriaBuilder.createQuery(TripRequestAdvancePayment.class);
        Root<TripRequestAdvancePayment> root = query.from(TripRequestAdvancePayment.class);
        query.select(root);
        query.where(getPredicate(criteriaBuilder, root, filter));
        query.orderBy(getOrders(criteriaBuilder, root, constructSorting(filter)));

        TypedQuery<TripRequestAdvancePayment> typedQuery = entityManager.createQuery(query);
        long l = typedQuery.getResultList().size();

        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPerPage(), constructSorting(filter));
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(typedQuery.getResultList(), pageable, l);
    }

    private Predicate[] getPredicate(
        CriteriaBuilder criteriaBuilder,
        Root<TripRequestAdvancePayment> root,
        Filter filter
    ) {
        List<Predicate> res = new ArrayList<>();
        Boolean isDownloadedAdvanceApplication = filter.getIsDownloadedAdvanceApplication();
        if (isDownloadedAdvanceApplication != null) {
            res.add(criteriaBuilder.equal(root.get("isDownloadedAdvanceApplication"), isDownloadedAdvanceApplication));
        }
        Boolean isDownloadedContractApplication = filter.getIsDownloadedContractApplication();
        if (isDownloadedContractApplication != null) {
            res.add(criteriaBuilder.equal(root.get("isDownloadedContractApplication"), isDownloadedContractApplication));
        }
        if (filter.getIsPushedUnfButton() != null) {
            res.add(criteriaBuilder.equal(root.get("isPushedUnfButton"), filter.getIsPushedUnfButton()));
        }
        if (filter.getIsUnfSend() != null) {
            res.add(criteriaBuilder.equal(root.get("isUnfSend"), filter.getIsUnfSend()));
        }
        if (filter.getIsPaid() != null) {
            res.add(criteriaBuilder.equal(root.get("isPaid"), filter.getIsPaid()));
        }
        if (filter.getIsCancelled() != null) {
            res.add(criteriaBuilder.equal(root.get("isCancelled"), filter.getIsCancelled()));
        }
        if (filter.getHasComment() != null) {
            Predicate comment = filter.getHasComment() ?
                criteriaBuilder.isNotNull(root.get("comment")) :
                criteriaBuilder.isNull(root.get("comment"));
            res.add(comment);
        }
        if (filter.getIsCancelled() != null) {
            res.add(criteriaBuilder.equal(root.get("isCancelled"), filter.getIsCancelled()));
        }
        return res.toArray(new Predicate[]{});
    }

    private Order[] getOrders(CriteriaBuilder criteriaBuilder, Root<TripRequestAdvancePayment> root, Sort orders) {
        List<Order> orderList = new ArrayList<>(2);
        Iterator<Sort.Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Sort.Order order = iterator.next();
            orderList.add(
                Sort.Direction.ASC == order.getDirection() ?
                    criteriaBuilder.asc(root.get(order.getProperty())) :
                    criteriaBuilder.desc(root.get(order.getProperty()))
            );
        }
        return orderList.toArray(new Order[]{});
    }

    private Sort constructSorting(Filter filter) {
        List<Sort.Order> sortingOrders = filter.getSort()
            .stream()
            .map(sort -> new Sort.Order(Sort.Direction.fromString(sort.getDir().name()), getProperty(sort.getKey())))
            .collect(Collectors.toList());
        if (sortingOrders.isEmpty()) {
            sortingOrders.add(new Sort.Order(Sort.Direction.fromString("asc"), "tripId"));
        }
        return Sort.by(sortingOrders);
    }

    private String getProperty(@NotNull @Valid SortByField key) {
        switch (key) {
            case PUSH_BUTTON_AT:
                return "pushButtonAt";
            case IS_AUTOMATION_REQUEST:
                return "isAutomationRequest";
            case LOADING_COMPLETE:
                return "loadingComplete";
            case IS_DOWNLOADED_CONTRACT_APPLICATION:
                return "isDownloadedContractApplication";
            case IS_DOWNLOADED_ADVANCE_APPLICATION:
                return "isDownloadedAdvanceApplication";
            case IS_PUSHED_UNF_BUTTON:
                return "isPushedUnfButton";
            case TRIP_NUM:
            default:
                return "tripId";
        }
    }
}
