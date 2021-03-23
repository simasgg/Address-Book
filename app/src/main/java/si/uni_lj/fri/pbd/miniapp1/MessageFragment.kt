package si.uni_lj.fri.pbd.miniapp1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MessageFragment : Fragment(R.layout.fragment_message) {
    private val listItems = ArrayList<String>()
    private val listNumbers = ArrayList<String>()
    private val listEmails = ArrayList<String>()
    private val listCheckedItems = ArrayList<String>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // to change title of activity
        (activity as AppCompatActivity?)!!.supportActionBar?.title = "Message"
        // read sharedPreferences data and add it to to ArrayList (string >>> ArrayList)
        readDataFromPreferences()
        val dataToSend = ArrayList<String>()

        // check which emails/MMS should be included
        val send = activity?.findViewById(R.id.email_btn) as Button
        val send2 = activity?.findViewById(R.id.mms_btn) as Button
        send.setOnClickListener{
            for (i in 0 until listItems.size) {
                // true if checked item belongs to our main list and email exists
                if(listCheckedItems.contains(listItems[i])){
                    if(listEmails[i] != "none")
                        dataToSend.add(listEmails[i])
                }
            }

            if(dataToSend.isNotEmpty())
                sendMail(dataToSend.toTypedArray())
            else {
                if(listCheckedItems.size>0)
                    Toast.makeText(activity, "None of the contacts selected have email addresses", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(activity, "No contacts were selected", Toast.LENGTH_SHORT).show()
            }
            // clear arrayList
            dataToSend.clear()
        }
        send2.setOnClickListener{
            for (i in 0 until listItems.size) {
                // true if checked item belongs to our main list and phone exists
                if(listCheckedItems.contains(listItems[i])){
                    if(listNumbers[i] != "none")
                        dataToSend.add(listNumbers[i])
                }
            }

            if(dataToSend.isNotEmpty())
                sendMMS(dataToSend.toTypedArray())
            else{
                if(listCheckedItems.size>0)
                    Toast.makeText(activity, "None of the contacts selected have phone numbers", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(activity, "No contacts were selected", Toast.LENGTH_SHORT).show()
            }
            // clear arrayList
            dataToSend.clear()
        }
    }

    // send mail
    private fun sendMail(array: Array<String>){
        val subject = "PBD 2021 Group Email"
        val message  = "Sent from my Android mini app 1"
        val intent  = Intent(Intent.ACTION_SEND)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, array)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, message)

        // for opening only email clients
        intent.type = "message/rfc822"
        startActivity(Intent.createChooser(intent, "Choose Email Client"))
    }

    // send MMS/SMS
    private fun sendMMS(array: Array<String>){
        val message = "Sent from my Android mini app 1"
        val allNumbers = array.joinToString(separator = ";")
        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$allNumbers"))
        smsIntent.putExtra("sms_body", message)
        startActivity(smsIntent)
    }

    // reading data and selected contacts
    private fun readDataFromPreferences(){
        val sharedPreferences : SharedPreferences? = activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPreferences?.getString(ContactsFragment.ARRAY_LIST_NAMES, null) as String
        val savedNumber = sharedPreferences.getString(ContactsFragment.ARRAY_LIST_NUMBERS, null) as String
        val savedEmail = sharedPreferences.getString(ContactsFragment.ARRAY_LIST_EMAILS, null) as String
        val savedCheckedItems = sharedPreferences.getString(ContactsFragment.ARRAY_LIST_CHECKED, null) as String

        // to array list
        listItems.addAll(savedName.split(";").toTypedArray())
        listNumbers.addAll(savedNumber.split(";").toTypedArray())
        listEmails.addAll(savedEmail.split(";").toTypedArray())
        listCheckedItems.addAll(savedCheckedItems.split(";").toTypedArray())
    }

}