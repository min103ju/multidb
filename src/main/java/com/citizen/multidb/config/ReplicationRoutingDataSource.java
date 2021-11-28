package com.citizen.multidb.config;

import com.citizen.multidb.constants.DbDestination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {

        if (log.isDebugEnabled()) {
            log.debug("current getCurrentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
            log.debug("current isCurrentTransactionReadOnly : {}", TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            log.debug("current isActualTransactionActive : {}", TransactionSynchronizationManager.isActualTransactionActive());
            log.debug("current getCurrentTransactionIsolationLevel : {}", TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
            log.debug("current getResourceMap : {}", TransactionSynchronizationManager.getResourceMap());
        }

        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? DbDestination.MULTI_DB_SLAVE
            : DbDestination.MULTI_DB_MASTER;
    }
}
