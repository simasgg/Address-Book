package si.uni_lj.fri.pbd.miniapp1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import si.uni_lj.fri.pbd.miniapp1.MainActivity.Companion.navView


private const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 75
class ContactsFragment : Fragment(R.layout.fragment_contacts) {
    companion object {
        // key values for sharedPreferences
        const val ARRAY_LIST_NAMES = "ARRAY_LIST_NAMES"
        const val ARRAY_LIST_NUMBERS = "ARRAY_LIST_NUMBERS"
        const val ARRAY_LIST_EMAILS = "ARRAY_LIST_EMAILS"
        const val ARRAY_LIST_CHECKED = "ARRAY_LIST_CHECKED"

    }

    private var contacts : ListView? = null
    private val listItems = ArrayList<String>()
    private val listNumbers = ArrayList<String>()
    private val listEmails = ArrayList<String>()
    private val checkedItems = ArrayList<String>()
    private var adapter: ArrayAdapter<String>? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // flag which handles "remove contacts" menu button
        MainActivity.flag = true
        // to change title of activity
        (activity as AppCompatActivity?)!!.supportActionBar?.title = "Contacts"

        // create clickable contact buttons
        contacts = view?.findViewById(R.id.listView_contacts)
        adapter = activity?.let {
            ArrayAdapter(it, android.R.layout.simple_list_item_multiple_choice, listItems)
        }
        contacts?.adapter = adapter


        readData()

        // initialize with contacts from phone if list is empty
        if(listItems.size <= 1) {
            adapter?.clear()
            getContactsPermission()
        }

        // set all items that need to be checked (we got this info from sharedPreferences)
        for (i in 0 until listItems.size) {
            if(checkedItems.contains(contacts!!.getItemAtPosition(i))){
                contacts!!.setItemChecked(i,true)
            }
        }
    }



    // read sharedPreferences data and add it to to ArrayList (string >>> ArrayList)
    private fun readData(){
        val sharedPreferences : SharedPreferences? = activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor? = sharedPreferences?.edit()
        if(sharedPreferences!!.contains(ARRAY_LIST_NAMES) && sharedPreferences.contains(ARRAY_LIST_NUMBERS) && sharedPreferences.contains(ARRAY_LIST_CHECKED)) {
            val savedName = sharedPreferences.getString(ARRAY_LIST_NAMES, null) as String
            val savedNumber = sharedPreferences.getString(ARRAY_LIST_NUMBERS, null) as String
            val savedEmail = sharedPreferences.getString(ARRAY_LIST_EMAILS, null) as String
            val savedCheckItems = sharedPreferences.getString(ARRAY_LIST_CHECKED, null) as String
            listItems.clear()
            listNumbers.clear()
            listEmails.clear()
            listItems.addAll(savedName.split(";").toTypedArray())
            listNumbers.addAll(savedNumber.split(";").toTypedArray())
            listEmails.addAll(savedEmail.split(";").toTypedArray())
            checkedItems.addAll(savedCheckItems.split(";").toTypedArray())
            editor?.apply()
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // create floating action button
        val fab : FloatingActionButton = getView()!!.findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(activity, AddContactActivity::class.java)
            startActivityForResult(intent, 1)
        }

    }


    // Save data on DestroyView
    override fun onDestroyView() {
        super.onDestroyView()

        // If contacts were removed then clear all the data (false == cleared)
        if(!MainActivity.flag){
            listItems.clear()
            listNumbers.clear()
            listEmails.clear()
            checkedItems.clear()
        }
        // set back to normal
        MainActivity.flag = true

        // getting all checked values
        checkedItems.clear()
        for (i in 0 until listItems.size) {
            if(contacts!!.isItemChecked(i)){
                // add checked items to arrayList
                checkedItems.add(contacts!!.getItemAtPosition(i).toString())
            }
        }

        // we will convert arrayList to string so we can save data with sharedPreferences
        val nameHS = listItems.joinToString(separator = ";")
        val numberHS = listNumbers.joinToString(separator = ";")
        val emailHS = listEmails.joinToString(separator = ";")
        val checkedHS = checkedItems.joinToString(separator = ";")


        // start working with sharedPreferences
        val sharedPreferences : SharedPreferences? = activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor? = sharedPreferences?.edit()

        //each time I write checked values and clear it for the next time
        editor?.apply{
            remove(ARRAY_LIST_CHECKED)
            putString(ARRAY_LIST_NAMES, nameHS)
            putString(ARRAY_LIST_NUMBERS, numberHS)
            putString(ARRAY_LIST_EMAILS, emailHS)
            putString(ARRAY_LIST_CHECKED, checkedHS)
        }?.apply()
        adapter?.notifyDataSetChanged()

    }

    // working with data from AddContactActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val resultName = data!!.getStringExtra("name_id")
                val resultNumber = data.getStringExtra("number_id")
                val resultEmail = data.getStringExtra("email_id")
                // check if data inserted is not empty
                if(!resultName.isNullOrBlank() && !resultNumber.isNullOrBlank() && !resultEmail.isNullOrBlank()) {
                    // check if inserted data doesn't match with current list of data
                    when {
                        listItems.contains(resultName) -> {
                            Toast.makeText(activity, "Username cannot repeat, please enter contact again", Toast.LENGTH_SHORT).show()
                        }
                        listNumbers.contains(resultNumber) -> {
                            Toast.makeText(activity, "Phone number cannot repeat, please enter contact again", Toast.LENGTH_SHORT).show()
                        }
                        listEmails.contains(resultEmail) -> {
                            Toast.makeText(activity, "Email cannot repeat, please enter contact again", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            listItems.add(resultName)
                            listNumbers.add(resultNumber)
                            listEmails.add(resultEmail)
                            // notify about insertion, our list of contacts change
                            adapter?.notifyDataSetChanged()

                            Toast.makeText(activity, "Contact added to an app", Toast.LENGTH_SHORT).show()
                            // set home menu
                            fragmentManager!!.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                            navView?.setCheckedItem(R.id.nav_home)
                        }
                    }

                }else{
                    Toast.makeText(activity, "Invalid data inserted, try again", Toast.LENGTH_SHORT).show()
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(activity, "Try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getContactsPermission(){
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(activity!!, arrayOf(Manifest.permission.READ_CONTACTS), MY_PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            // read all the contacts
            readContactsFromPhone()
        }
    }


    @SuppressLint("Recycle")
    private fun readContactsFromPhone(){
        var i = 2
        val resolver: ContentResolver = activity!!.contentResolver
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if(cursor!!.moveToFirst()){
            while (cursor.moveToNext()) {
                var mail = ""
                var phoneNumber = ""
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                val emails : Cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null)!!
                while (emails.moveToNext())
                {
                    mail = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                    break
                }
                emails.close()

                if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt() > 0) {
                    val phoneCursor = activity!!.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?", arrayOf(id), null)
                    while (phoneCursor!!.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        break
                    }
                    phoneCursor.close()
                }

                if(!listItems.contains(name))
                    listItems.add(name)
                else{
                    listItems.add((name + i))
                    i++
                }


                // check if phone number exists
                if(phoneNumber.isBlank()) {
                    listNumbers.add("none")
                }else
                    listNumbers.add(phoneNumber)
                // check if email exists
                if(mail.isBlank()) {
                    listEmails.add("none")
                }else
                    listEmails.add(mail)

            }
        }
        cursor.close()
    }

}