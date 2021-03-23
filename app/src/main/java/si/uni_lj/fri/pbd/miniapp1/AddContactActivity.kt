package si.uni_lj.fri.pbd.miniapp1

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AddContactActivity : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var number: EditText
    private lateinit var email: EditText
    private lateinit var myButton: Button

    // email checker
    private fun String.isValidEmail() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    // phone checker
    private fun String.isValidPhone() = !TextUtils.isEmpty(this) && Patterns.PHONE.matcher(this).matches()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        name = findViewById(R.id.c_name)
        number = findViewById(R.id.c_number)
        email = findViewById(R.id.c_email)
        myButton = findViewById(R.id.btn_add)

        // on button click
        myButton.setOnClickListener{
            // returns false if email or phone is not valid
            if(email.text.toString().isValidEmail() && number.text.toString().isValidPhone()) {
                addDataToPhoneContacts()
                // send data back to MessageFragment and add this info to our contact list manually
                val resultIntent = Intent()
                resultIntent.putExtra("name_id", name.text.toString())
                resultIntent.putExtra("number_id", number.text.toString())
                resultIntent.putExtra("email_id", email.text.toString())
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }else{
                Toast.makeText(this, "Entered email address or/and phone number is invalid", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addDataToPhoneContacts(){
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
        }
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name.text.toString())
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number.text.toString())
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.text.toString())
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "Application wasn't found", Toast.LENGTH_LONG).show()
        }
    }


}