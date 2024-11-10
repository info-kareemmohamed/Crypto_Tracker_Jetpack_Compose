package com.plcoding.cryptotracker.crypto.presentatoin.coin_list

import androidx.compose.runtime.Immutable
import com.plcoding.cryptotracker.crypto.presentatoin.models.CoinUi

@Immutable
data class CoinListState(
    val isLoading: Boolean = false,
    val coins: List<CoinUi> = emptyList(),
    val selectedCoin: CoinUi? = null,
)
