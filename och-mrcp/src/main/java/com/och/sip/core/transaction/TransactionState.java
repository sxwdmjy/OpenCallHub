package com.och.sip.core.transaction;

public enum TransactionState {
    INIT {
        @Override
        public boolean canTransitionTo(TransactionState newState) {
            return newState == PROCEEDING || newState == TERMINATED;
        }
    },
    PROCEEDING {
        @Override
        public boolean canTransitionTo(TransactionState newState) {
            return newState == COMPLETED || newState == TERMINATED;
        }
    },
    COMPLETED {
        @Override
        public boolean canTransitionTo(TransactionState newState) {
            return newState == CONFIRMED || newState == TERMINATED;
        }
    },
    CONFIRMED {
        @Override
        public boolean canTransitionTo(TransactionState newState) {
            return newState == TERMINATED;
        }
    },
    TERMINATED {
        @Override
        public boolean canTransitionTo(TransactionState newState) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(TransactionState newState);
}