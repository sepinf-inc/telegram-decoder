package org.telegram.ui.Wallet;

/**
 * Stub of upstream WalletEngine2. SendPhase is only the type of a client-side ("custom") field of
 * TL_wallet, never read from serialized data.
 */
public final class WalletEngine2 {
    private WalletEngine2() {
    }

    public enum SendPhase {
        IDLE, VALIDATING, AUTHORIZING, PREPARING, PERSISTING,
        READY_TO_SUBMIT, SUBMITTING, SUBMISSION_UNKNOWN, SUBMITTED,
        HANDED_OFF, CONFIRMED, REPLACED, SEQUENCE_NUMBER_CONSUMED,
        EXPIRED, SUPERSEDED, FAILED, CANCELLED
    }
}
