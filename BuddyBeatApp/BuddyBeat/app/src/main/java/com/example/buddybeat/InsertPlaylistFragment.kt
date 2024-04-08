package com.example.buddybeat

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment


class InsertPlaylistFragment(val listen : InsertPlaylistListener) : DialogFragment()
{
    private lateinit var listener: InsertPlaylistListener

    // The activity that creates an instance of this dialog fragment must
    // implement this interface to receive event callbacks. Each method passes
    // the DialogFragment in case the host needs to query it.
    interface InsertPlaylistListener {
        fun onDialogPositiveClick(dialog: DialogFragment, name : String, description : String)
    }

    // Override the Fragment.onAttach() method to instantiate the
    // NoticeDialogListener.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface.
        try {
            // Instantiate the NoticeDialogListener so you can send events to
            // the host.
            listener = listen as InsertPlaylistListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface. Throw exception.
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                // Get the layout inflater.
                val inflater = requireActivity().layoutInflater;

                // Inflate and set the layout for the dialog.
                // Pass null as the parent view because it's going in the dialog
                // layout.
                val view = inflater.inflate(R.layout.insert_playlist, null)
                builder.setView(view)
                val name : EditText? = view.findViewById<EditText>(R.id.name_playlist)
                val description : EditText? = view.findViewById<EditText>(R.id.description)
                    // Add action buttons.
                builder.setPositiveButton("Insert",
                        DialogInterface.OnClickListener { dialog, id ->
                            val title = name?.text.toString()
                            val descr = description?.text.toString()
                            listener.onDialogPositiveClick(this, title, descr)
                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            getDialog()?.cancel()
                        })
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
}