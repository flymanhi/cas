package org.apereo.cas.trusted.authentication.storage;

import com.google.common.collect.Sets;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * This is {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerMfaAuthnTrust")
public class JpaMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final String TABLE_NAME = "MultifactorAuthenticationTrustRecord";
    
    @PersistenceContext(unitName = "mfaTrustedAuthnEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void expire(final LocalDate onOrBefore) {
        try {
            final int count = this.entityManager.createQuery("DELETE FROM " + TABLE_NAME + " r where r.date < :date",
                    MultifactorAuthenticationTrustRecord.class)
                    .setParameter("date", onOrBefore)
                    .executeUpdate();
            logger.info("Found and removed {} records", count);
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found");
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        try {
            final List<MultifactorAuthenticationTrustRecord> results =
                    this.entityManager.createQuery("SELECT r FROM " + TABLE_NAME + " r where r.principal = :principal",
                            MultifactorAuthenticationTrustRecord.class).setParameter("principal", principal).getResultList();
            return Sets.newHashSet(results);
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found for {}", principal);
        }
        return Sets.newHashSet();
    }

    @Override
    public MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        return this.entityManager.merge(record);
    }
}
