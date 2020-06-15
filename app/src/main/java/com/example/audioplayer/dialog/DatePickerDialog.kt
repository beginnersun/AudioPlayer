
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.audioplayer.R
import kotlinx.android.synthetic.main.dialog_pick_time.*
import java.util.*
import kotlin.math.min


class DatePickerDialog private constructor(): DialogFragment() {

    var maxDate:Long = 0
    set(value) {
        field = value
        date_picker?.let {
            it.maxDate = field
        }
    }

    var minDate:Long = 0
    set(value) {
        field = value
        date_picker?.let {
            it.minDate = field
        }
    }

    var onSelectedDateListener: OnSelectedDateListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_pick_time,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        date_picker.maxDate = maxDate
        date_picker.minDate = minDate
        tv_ok.setOnClickListener {
            onSelectedDateListener?.onSelectedDate(date_picker,date_picker.year,date_picker.month+1,date_picker.dayOfMonth)
        }
        tv_cancel.setOnClickListener {
            dismiss()
        }
    }

    fun setCurrentTime(time:Date){
        val calendar = Calendar.getInstance().apply { this.time = time }
        date_picker.updateDate(calendar.get(Calendar.YEAR),Calendar.MONTH,Calendar.DAY_OF_MONTH)
    }

    interface OnSelectedDateListener {
        fun onSelectedDate(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int)
    }

    companion object{
        fun newInstance(onSelectedDateListener: OnSelectedDateListener,minDate: Long = 0,maxDate: Long = System.currentTimeMillis()):DatePickerDialog =
            DatePickerDialog().apply {
                this.onSelectedDateListener = (onSelectedDateListener)
                this.maxDate = maxDate
                this.minDate = minDate
            }
    }
}