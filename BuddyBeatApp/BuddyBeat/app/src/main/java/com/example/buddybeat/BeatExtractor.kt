package com.example.buddybeat


import android.content.Context
import androidx.core.net.toUri
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import be.tarsos.dsp.onsets.OnsetHandler

/*Calculation of bpm*/
class BeatExtractor(val context: Context) {
    fun beatDetection(path: String, duration: Int): Int {
        val fftsize = 2048
        val BPM = mutableListOf<Double>()

        val onsetHandler = object : OnsetHandler {
            var last_time = 0.0
            override fun handleOnset(time: Double, salience: Double) {
                val bpm = (60 / (time - last_time))
                BPM.add(bpm)
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

        val y = BPM.average()

        return Math.round(y).toInt()
    }
}