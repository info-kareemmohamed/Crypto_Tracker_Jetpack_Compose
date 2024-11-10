package com.plcoding.cryptotracker.crypto.presentatoin.coin_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.cryptotracker.core.domain.util.onError
import com.plcoding.cryptotracker.core.domain.util.onSuccess
import com.plcoding.cryptotracker.crypto.domain.CoinDataSource
import com.plcoding.cryptotracker.crypto.presentatoin.coin_detail.DataPoint
import com.plcoding.cryptotracker.crypto.presentatoin.models.CoinUi
import com.plcoding.cryptotracker.crypto.presentatoin.models.toCoinUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CoinListViewModel(
    private val coinDataSource: CoinDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    val state = _state
        .onStart { loadCoins() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoinListState()
        )
    private val _events = Channel<CoinListEvent>()
    val events = _events.receiveAsFlow()
    fun onAction(action: CoinListAction) {
        when (action) {
            is CoinListAction.OnCoinClick -> selectedCoin(action.coin)
        }
    }


    private fun selectedCoin(coin: CoinUi) {
        viewModelScope.launch {
            _state.update { it.copy(selectedCoin = coin) }

            coinDataSource.getCoinHistory(
                coinId = coin.id,
                start = ZonedDateTime.now().minusDays(5),
                end = ZonedDateTime.now()
            ).onSuccess { data ->
                val dataPoints = data.sortedBy { it.dateTime }.map {
                    DataPoint(
                        x = it.dateTime.toEpochSecond().toFloat(),
                        y = it.priceUsd.toFloat(),
                        xLabel = DateTimeFormatter
                            .ofPattern("ha\nM/d")
                            .format(it.dateTime)
                    )
                }
                _state.update { it.copy(selectedCoin = it.selectedCoin?.copy(coinPriceHistory = dataPoints)) }

            }.onError { error ->
                _events.send(CoinListEvent.Error(error))
            }


        }
    }

    private fun loadCoins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            coinDataSource.getCoins()
                .onSuccess { coins ->
                    _state.update {
                        it.copy(coins = coins.map { it.toCoinUi() }, isLoading = false)
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(CoinListEvent.Error(error))
                }
        }
    }

}