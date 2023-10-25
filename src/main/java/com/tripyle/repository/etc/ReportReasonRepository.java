package com.tripyle.repository.etc;

import com.tripyle.model.entity.etc.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
}
