/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F1\u06F1
 */
package localhost.idroid.salamun

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.LinearLayout
import android.widget.TextView

class AudioEnvironmentChallengeActivity : Activity() {
	private lateinit var indexMonitor: TextView
	private var volumeProgress: Int = 0
	private var bassBoostModifier: Int = 0
	private var spatialMultiplier: Double = 1.0
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setPadding(64, 96, 64, 96)
		}
		val titleView = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
			text = "Audio Environment Matrix"
			textSize = 24f
			typeface = Typeface.DEFAULT_BOLD
			gravity = Gravity.CENTER
			setPadding(0, 0, 0, 48)
		}
		val volumeControl = SeekBar(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { setMargins(0, 0, 0, 48) }
			max = 100
			progress = 0
		}
		val bassBoost = CheckBox(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { setMargins(0, 0, 0, 48) }
			text = "Enable Bass Boost (+25 Gain)"
			textSize = 16f
		}
		val spatialMode = RadioGroup(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { setMargins(0, 0, 0, 64) }
			orientation = RadioGroup.VERTICAL
		}
		val monoRadio = RadioButton(this).apply {
			id = View.generateViewId()
			text = "Mono"
			isChecked = true
		}
		val stereoRadio = RadioButton(this).apply {
			id = View.generateViewId()
			text = "Stereo"
		}
		val atmosRadio = RadioButton(this).apply {
			id = View.generateViewId()
			text = "Dolby Atmos"
		}
		with(spatialMode) {
			addView(monoRadio)
			addView(stereoRadio)
			addView(atmosRadio)
		}
		indexMonitor = TextView(this).apply {
			text = "Environment Index Score: 0.0"
			textSize = 20f
			typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
			gravity = Gravity.CENTER
			setPadding(0, 32, 0, 32)
		}
		val volumeChangeListener = object: SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				volumeProgress = progress
				recalculateEnvironmentIndex()
			}
			override fun onStartTrackingTouch(seekBar: SeekBar?) {
				// Mandatory stub method fulfilled under interface contract parameters
			}
			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				// Mandatory stub method fulfilled under interface contract parameters
			}
		}
		volumeControl.setOnSeekBarChangeListener(volumeChangeListener)
		bassBoost.setOnCheckedChangeListener { _, isChecked ->
			bassBoostModifier = if (isChecked) 25 else 0
			recalculateEnvironmentIndex()
		}
		spatialMode.setOnCheckedChangeListener { _, checkedId ->
			spatialMultiplier = when(checkedId) {
				monoRadio.id -> 1.0
				stereoRadio.id -> 1.5
				atmosRadio.id -> 2.0
				else -> 1.0
			}
			recalculateEnvironmentIndex()
		}
		with(rootLayout) {
			addView(titleView)
			addView(volumeControl)
			addView(bassBoost)
			addView(spatialMode)
			addView(indexMonitor)
		}
		setContentView(rootLayout)
	}
	private fun recalculateEnvironmentIndex() {
		val finalScore = (volumeProgress + bassBoostModifier) * spatialMultiplier
		indexMonitor.text = "Environment Index Score: $finalScore"
	}
}
