package com.example.audioplayer.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.audioplayer.R
import kotlinx.android.synthetic.main.dialog_one_content.*


class OnContentDialog private constructor(private val title: String, private val okMessage:String, private val cancelMessage:String)
    : DialogFragment(), View.OnClickListener {
    var initValue:String =""   //初始化的值
       set(value) {
           field = value
           if (!value.isNullOrEmpty()){
               if(et_wx_name!=null){
                   et_wx_name.setText(field)
                   et_wx_name.isEnabled = true
                   et_wx_name.setBackgroundResource(R.drawable.bg_edit)
               }
           }
       }

    var showNotification:String = "" //展示弹窗的值
        set(value) {
            field = value
            if (!value.isNullOrEmpty()) {
                if (et_wx_name != null) {
                    et_wx_name.setText(field)
                    et_wx_name.isEnabled = false
                    et_wx_name.setBackgroundColor(Color.WHITE)
                }
            }
        }
    var onDialogClickListener: OnDialogClickListener? = null

    override fun onStart() {
        super.onStart()
    }

    override fun getTheme(): Int =
        R.style.custom_dialog_style

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_one_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
    }

    private fun initViews() {
        tv_ok.setOnClickListener(this)
        tv_cancel.setOnClickListener(this)
        if (!initValue.isNullOrEmpty()){
            et_wx_name.setText(initValue)
            et_wx_name.isEnabled = true
            et_wx_name.setBackgroundResource(R.drawable.bg_edit)
        }else if (!showNotification.isNullOrEmpty()){
            et_wx_name.setText(showNotification)
            et_wx_name.isEnabled = false
            et_wx_name.setBackgroundColor(Color.WHITE)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_ok -> {
                if (et_wx_name.text.isNullOrEmpty()){
                    Toast.makeText(context, "微信名称不能为空", Toast.LENGTH_SHORT).show()
                    return
                }
                if (!et_wx_name.isEnabled){
                    onDialogClickListener?.onOkClick(v, et_wx_name.text.toString())
                    dismiss()
                    return
                }
                onDialogClickListener?.onOkClick(v, et_wx_name.text.toString())
                dismiss()
            }
            R.id.tv_cancel -> {
                onDialogClickListener?.onCancel(v)
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(title:String = "设置对应微信名",okMessage: String = "确定",cancelMessage: String="取消"): OnContentDialog =
            OnContentDialog(
                title,
                okMessage,
                cancelMessage
            )
    }

    interface OnDialogClickListener {
        fun onOkClick(view: View, message: String)

        fun onCancel(view: View)
    }
}