package com.example.buddybeat


import android.content.Context
import androidx.core.net.toUri
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import be.tarsos.dsp.onsets.OnsetHandler


class BeatExtractor(val context: Context) {
    fun beatDetection(path: String, duration: Int): Int {
        val fftsize = 2048
        val BPM = mutableListOf<Double>()

        val onsetHandler = object : OnsetHandler {
            var last_time = 0.0
            override fun handleOnset(time: Double, salience: Double) {
                val bpm = (60 / (time - last_time))
                BPM.add(bpm)
                //Log.d("Beat", "Music bpm : " + bpm + " Salience : " + salience)
                last_time = time
            }
        }

        val begin = Math.min(duration / 1000.0 / 10.0, 2.0)
        val duration1 = Math.min(duration/1000.0, 360.0)
        val dispatcher: AudioDispatcher = AudioDispatcherFactory.fromPipe(
            context, path.toUri(),
            begin, duration1, 44100, fftsize, fftsize / 2)

        val detector = ComplexOnsetDetector(fftsize)
        val handler = BeatRootOnsetEventHandler()
        detector.setHandler(handler)

        dispatcher.addAudioProcessor(detector)
        dispatcher.run()

        handler.trackBeats(onsetHandler)
        dispatcher.stop()

        //val x = mode(BPM)
        val y = BPM.average()

        return Math.round(y).toInt()
    }

    private fun mode(data: MutableList<Double>): Double {
        var maxValue = -1.0
        var maxCount = 0
        for (i in 0 until data.size) {
            val currentValue: Double = data.get(i)
            var currentCount = 1
            for (j in i + 1 until data.size) {
                if (Math.abs(data.get(j) - currentValue) < 0.1) {
                    ++currentCount
                }
            }
            if (currentCount > maxCount) {
                maxCount = currentCount
                maxValue = currentValue
            } else if (currentCount == maxCount) {
                //maxValue = Double.NaN
            }
        }
        return maxValue
    }
}