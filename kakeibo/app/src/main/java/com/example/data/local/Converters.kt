package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.KakeiboPillar
import com.example.data.model.PeriodType
import com.example.data.model.TransactionType
import com.example.data.model.WalletType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromKakeiboPillar(value: KakeiboPillar?): String? = value?.name

    @TypeConverter
    fun toKakeiboPillar(value: String?): KakeiboPillar? =
        value?.let { runCatching { KakeiboPillar.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromWalletType(value: WalletType?): String? = value?.name

    @TypeConverter
    fun toWalletType(value: String?): WalletType? =
        value?.let { runCatching { WalletType.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromPeriodType(value: PeriodType?): String? = value?.name

    @TypeConverter
    fun toPeriodType(value: String?): PeriodType? =
        value?.let { runCatching { PeriodType.valueOf(it) }.getOrNull() }
}
