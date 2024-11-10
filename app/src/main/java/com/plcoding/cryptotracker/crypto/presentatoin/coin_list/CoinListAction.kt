package com.plcoding.cryptotracker.crypto.presentatoin.coin_list

import com.plcoding.cryptotracker.crypto.presentatoin.models.CoinUi

sealed interface CoinListAction {
    data class OnCoinClick(val coin: CoinUi): CoinListAction
}