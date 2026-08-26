package com.github.durex.messaging.spring.redis;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

final class TransactionalPublication {
  private TransactionalPublication() {}

  static void publishNowOrAfterCommit(Runnable publication) {
    if (TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              publication.run();
            }
          });
      return;
    }

    publication.run();
  }
}
