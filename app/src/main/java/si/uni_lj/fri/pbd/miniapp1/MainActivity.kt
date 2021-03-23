package si.uni_lj.fri.pbd.miniapp1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.ByteArrayOutputStream

/*
    NOTE:
        This project was mostly tested on real device: Samsung Galaxy S7.
        Although emulators should also work fine. (Pixel_3a_API_30_x86 seems fine)

        If your contacts have no phone number or email added then
        they won't be attached as a recipients once you press 'send' button

        If you can't see your contacts in the app, press "re-upload contacts"
        in the option menu

        make sure you have at least several contacts in your phone contact list
 */
private const val REQUEST_CODE = 50
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    private var drawer : DrawerLayout? = null
    private var toggle : ActionBarDrawerToggle? = null
    companion object {
        var navView: NavigationView? = null
        // Notify Contact fragment that we just cleared contact list
        var flag = true
    }
    private lateinit var img : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // drawer menu settings
        drawer = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // setting our nav header image
        uploadAndSetPhoto()

        // handling drawer
        navView?.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer?.addDrawerListener(toggle!!)
        toggle?.syncState()


        // set initial fragment, if device rotates, fragment doesn't change
        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navView?.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val takenImage = data?.extras?.get("data") as Bitmap
            img.setImageBitmap(takenImage)

            // save image as string
            val stream = ByteArrayOutputStream()
            takenImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val b: ByteArray = stream.toByteArray()

            val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)

            val sharedPreferences : SharedPreferences? = getSharedPreferences("sharedPrefsImage", Context.MODE_PRIVATE)
            val editor : SharedPreferences.Editor? = sharedPreferences?.edit()

            //each time I write checked values and clear it for the next time
            editor?.apply{
                putString("image_data",encodedImage)
            }?.apply()

        }else
            super.onActivityResult(requestCode, resultCode, data)
    }

    // inflate options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    // for menu options (exit program and delete contacts)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // get item for quit
        val getId = item.itemId

        if(getId == R.id.quit) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialogTitleExit)
            builder.setPositiveButton("Yes"){ _, _ ->
                this.finishAffinity()
            }
            // when cancel ir pressed
            builder.setNegativeButton("Cancel", null)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
        else if(getId == R.id.remove){
            flag = false
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialogTitle)
            // when yes is pressed
            builder.setPositiveButton("Yes"){ _, _ ->
                val sharedPreferences : SharedPreferences = this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                val editor : SharedPreferences.Editor? = sharedPreferences.edit()

                // remove everything but not profile image
                editor?.apply{
                    clear()
                }?.apply()


                // switch to home screen
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                navView?.setCheckedItem(R.id.nav_home)
                Toast.makeText(this, "Contacts successfully updated", Toast.LENGTH_SHORT).show()
            }
            // when cancel ir pressed
            builder.setNegativeButton("Cancel", null)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    // check for the item we click and set the fragment
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_contacts -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ContactsFragment()).commit()
            R.id.nav_message -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MessageFragment()).commit()
            else -> { // Note the block
                Toast.makeText(this, "Error choosing a button", Toast.LENGTH_SHORT).show()
            }
        }

        // close the drawer
        drawer?.closeDrawer(GravityCompat.START)

        return true
    }

    // Close the drawer if it's open and back button is pressed
    override fun onBackPressed() {
        if(drawer!!.isDrawerOpen(GravityCompat.START)){
            // close the drawer
            drawer?.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun uploadAndSetPhoto(){
        val hView = navView!!.getHeaderView(0)
        img = hView.findViewById(R.id.imageView_click)

        val sharedPreferences : SharedPreferences? = getSharedPreferences("sharedPrefsImage", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor? = sharedPreferences?.edit()
        if(sharedPreferences!!.contains("image_data")) {
            val previouslyEncodedImage = sharedPreferences.getString("image_data", null) as String

            if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                img.setImageBitmap(bitmap)
            }
            editor?.remove("image_data")
            editor?.apply()
        }

        // clicking an image
        img.setOnClickListener{
            requestPermission()
            // taking photo
            if(checkForPermissions() == 0) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_CODE)
                } else {
                    Toast.makeText(this, "Not able to open camera", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    // BELOW: Working with permissions

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED


    private fun requestPermission(){
        val reqPerm = mutableListOf<String>()
        if( !hasReadExternalStoragePermission() ){
            reqPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if(reqPerm.isNotEmpty()){
            ActivityCompat.requestPermissions(this, reqPerm.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.isNotEmpty())
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkForPermissions(): Int {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    }


}