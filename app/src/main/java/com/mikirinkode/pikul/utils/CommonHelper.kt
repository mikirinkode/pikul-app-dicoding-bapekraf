package com.mikirinkode.pikul.utils

import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS

object CommonHelper {
    fun getReadAblePaymentStatus(paymentStatus: String): String = when (paymentStatus) {
        PAYMENT_STATUS.WAITING_FOR_PAYMENT.toString() -> {
            "Menunggu Pembayaran"
        }
        PAYMENT_STATUS.ALREADY_PAID.toString() -> {
            "Sudah Dibayar"
        }
        PAYMENT_STATUS.FAILED.toString() -> {
            "Pembayaran Gagal"
        }
        else -> {
            ""
        }
    }

    fun getReadAbleTransactionStatus(transactionStatus: String): String = when (transactionStatus) {
        PAYMENT_STATUS.WAITING_FOR_PAYMENT.toString() -> {
            "Menunggu Pembayaran"
        }
        TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString() -> {
            "Menunggu Konfirmasi Pedagang"
        }
        TRANSACTION_STATUS.ON_PROCESS_BY_MERCHANT.toString() -> {
            "Sedang Diproses Pedagang"
        }
        TRANSACTION_STATUS.READY_TO_PICK_UP.toString() -> {
            "Pesanan Siap Diambil"
        }
        TRANSACTION_STATUS.COMPLETED.toString() -> {
            "Pesanan Selesai"
        }
        TRANSACTION_STATUS.CANCELLED.toString() -> {
            "Pesanan Dibatalkan"
        }
        else -> {
            ""
        }
    }

}