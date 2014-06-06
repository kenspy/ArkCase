package com.armedia.acm.plugins.complaint.dao;

import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.plugins.complaint.model.Complaint;
import com.armedia.acm.plugins.complaint.model.ComplaintListView;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

/**
 * Created by armdev on 4/4/14.
 */
public class ComplaintDao extends AcmAbstractDao<Complaint>
{
    public List<ComplaintListView> listAllComplaints()
    {
        CriteriaBuilder builder = getEm().getCriteriaBuilder();
        CriteriaQuery<ComplaintListView> query = builder.createQuery(ComplaintListView.class);
        Root<ComplaintListView> clv = query.from(ComplaintListView.class);
        query.select(clv);

        // TODO: parameterized order by
        query.orderBy(builder.desc(clv.get("created")));

        TypedQuery<ComplaintListView> dbQuery = getEm().createQuery(query);

        List<ComplaintListView> results = dbQuery.getResultList();

        return results;

    }

    @Transactional
    public int updateComplaintStatus(Long complaintId, String newStatus, String modifier)
    {
        Query updateStatusQuery = getEm().createQuery(
                "UPDATE Complaint " +
                        "SET status = :newStatus, " +
                        "modified = :modified, " +
                        "modifier = :modifier " +
                        "WHERE complaintId = :complaintId");
        updateStatusQuery.setParameter("newStatus", newStatus);
        updateStatusQuery.setParameter("modified", new Date());
        updateStatusQuery.setParameter("modifier", modifier);
        updateStatusQuery.setParameter("complaintId", complaintId);

        return updateStatusQuery.executeUpdate();
    }

}
