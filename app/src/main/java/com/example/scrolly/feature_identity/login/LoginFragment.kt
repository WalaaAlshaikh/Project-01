package com.example.scrolly.feature_identity.login

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.scrolly.R
import com.example.scrolly.databinding.FragmentLoginBinding
import com.example.scrolly.main.SHARED_PREF_FILE
import com.example.scrolly.main.STATE
import com.example.scrolly.main.USER_ID
import com.example.scrolly.util.RegisterValidation
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewMode by activityViewModels()
    //private lateinit var progressDialog: ProgressDialog
    //private lateinit var bottomNav: BottomNavigationView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sharedPref = requireActivity().getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)
        loadingDialog = setProgressDialog(requireContext(), "Loading..")

//        if (sharedPref.getBoolean(STATE, false)){
//            findNavController().navigate(R.id.action_loginFragment_to_timelineFragment)
//        }else{
//            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
//        }
    }

//


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        firebaseAuth = FirebaseAuth.getInstance()
        observers()

        //  bottomNav=activity!!.findViewById(R.id.bottomNavView)


//      if(firebaseAuth.currentUser?.uid.isNullOrBlank()){
//          findNavController().navigate(R.id.acti)
//
//      }

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        //  activity!!.findViewById<BottomAppBar>(R.id.bottomNavView)?.visibility=View.GONE
        //bottomNav.visibility=View.GONE


        binding.goToRegisTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginButton.setOnClickListener {
            val emailAddress = binding.emailTextfield.editText?.text.toString()
            val password = binding.passwordTextfield.editText?.text.toString()

            if (emailAddress.isNotBlank() && password.isNotBlank()) {
                loadingDialog.show()
                loginViewModel.login(emailAddress, password)
            } else {
                checkFields(emailAddress, password)

            }
        }

        binding.goToHomeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_timelineFragment)
        }





        binding.goToRegisTextView2.setOnClickListener {

            showdialog()
        }


    }

    fun observers() {
        loginViewModel.loginLiveData.observe(viewLifecycleOwner, {
            it?.let {
                val sharedPrefEdit = sharedPref.edit()
                Toast.makeText(requireActivity(), "login successfully", Toast.LENGTH_SHORT).show()
                sharedPrefEdit.putBoolean(STATE, true)
                sharedPrefEdit.putString(USER_ID, FirebaseAuth.getInstance().currentUser!!.uid)
                sharedPrefEdit.commit()
                loginViewModel.loginLiveData.postValue(null)
                //checkLoggedInState()
                loadingDialog.dismiss()
                findNavController().navigate(R.id.action_loginFragment_to_timelineFragment)

            }
        })

        loginViewModel.loginErrorLiveData.observe(viewLifecycleOwner, {

            it?.let {
                loadingDialog.dismiss()
                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
                loginViewModel.loginErrorLiveData.postValue(null)
            }
        })

    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    private fun checkFields(
        email: String,
        password: String,
    ): Boolean {
        var state = true
        val emailLayout = binding.emailTextfield
        val passwordLayout = binding.passwordTextfield

        emailLayout.error = null

        passwordLayout.error = null

        // Get needed string messages from strings.xml resource
        val require = "required!"

        if (email.isBlank()) {
            emailLayout.error = require
            state = false
        }

        if (password.isBlank()) {
            passwordLayout.error = require
            state = false
        }


        return state
    }

    fun showdialog() {
        val builder: android.app.AlertDialog.Builder =
            android.app.AlertDialog.Builder(requireActivity())
        builder.setTitle("Reset Password")

// Set up the input
        val input = EditText(requireContext())
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setHint("Enter you email")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

// Set up the buttons
        builder.setPositiveButton(
            "Send Password",
            DialogInterface.OnClickListener { dialog, which ->
                // Here you get get input text from the Edittext
                val email = input.text.toString()
                if (email.isNotEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(input.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    requireActivity(),
                                    "Password Sent Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    it.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                } else {
                    Toast.makeText(
                        requireActivity(),
                        "You need to put your email first",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }
}