package com.mikirinkode.pikul.constants

enum class TRANSACTION_STATUS {
    WAITING_FOR_PAYMENT,
    WAITING_FOR_MERCHANT,
    ON_PROCESS_BY_MERCHANT,
    READY_TO_PICK_UP,
    COMPLETED,
    CANCELLED,
}