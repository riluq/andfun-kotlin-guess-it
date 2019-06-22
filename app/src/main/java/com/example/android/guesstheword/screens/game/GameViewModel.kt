package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import android.util.Patterns
import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel


//    Vibration is controlled by passing in an array representing the number of milliseconds each interval of buzzing and non-buzzing takes.
//    So the array [0, 200, 100, 300] will wait 0 milliseconds,
//    then buzz for 200ms, then wait 100ms, then buzz fo 300ms. Here are some example buzz patterns you can copy over:

//    Getaran dikendalikan oleh melewati dalam array yang mewakili jumlah milidetik setiap interval berdengung dan tidak berdengung mengambil.
//    Jadi array [0, 200, 100, 300] akan menunggu 0 milidetik,
//    kemudian buzz untuk 200ms, kemudian menunggu 100ms, kemudian buzz untuk 300ms.
//    Berikut adalah beberapa contoh pola Buzz Anda dapat menyalin atas:

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel: ViewModel() {

    enum class BuzzType(val patterns: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    companion object {
        // These represent different important times in the game, such as game length
        // Ini mewakili waktu penting yang berbeda dalam permainan, seperti panjang permainan -> setelah di translate

        // This is when the game is over
        // Ini adalah ketika permainan berakhir -> setelah di translate
        private const val DONE = 0L

        // This is the time when the phone will start buzzing each second
        // Ini adalah waktu ketika telepon akan mulai berdengung setiap detik -> setelah di translate
        private const val COUNTDOWN_PANIC_SECONDS = 10L

        // This is the number of millisecond in a second
        // Ini adalah jumlah milidetik dalam kedua -> setelah di translate
        private const val ONE_SECOND = 1000L

        // This is the total time of the game
        // Ini adalah total waktu permainan -> setelah di translate
        private const val COUNTTDOWN_TIME = 60000L
    }

    private val timer: CountDownTimer

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    private val _eventGameFinished = MutableLiveData<Boolean>()
    val eventGameFinished: LiveData<Boolean>
        get() = _eventGameFinished

    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime

    val currentTimeString = Transformations.map(currentTime, { time ->
        DateUtils.formatElapsedTime(time)
    })

    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz


    init {
        Log.i("GameViewModel", "GameViewModel created!")
        resetList()
        nextWord()
        _score.value = 0
//        _eventGameFinished.value = false

        timer = object : CountDownTimer(COUNTTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (millisUntilFinished / ONE_SECOND)
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _eventBuzz.value = BuzzType.GAME_OVER
                _eventGameFinished.value = true
            }

        }
        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel", "GameViewModel destroyed!")
        timer.cancel()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)

    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = (score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        nextWord()
    }

    fun onGameFinishComplete() {
        _eventGameFinished.value = false

    }

    fun onBuzzComplate() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }


}