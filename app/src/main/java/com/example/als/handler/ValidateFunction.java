package com.example.als.handler;

import android.util.Patterns;

import com.example.als.object.Variable;
import com.google.android.material.textfield.TextInputLayout;

public class ValidateFunction {

    //validate field
    public static boolean validateTILField(TextInputLayout textInputLayout){
        //get user input
        String userInput = textInputLayout.getEditText().getText().toString().trim();

        //if user input == null
        if(userInput.isEmpty())
        {
            textInputLayout.getEditText().setError("Field can't be empty");
            return false;
        }
        else{
            return true;
        }
    }

    //validate email
    public static boolean validateEmail(TextInputLayout emailTIL){
        //get email
        String emailInput = emailTIL.getEditText().getText().toString().trim();

        //if email == null
        if(emailInput.isEmpty())
        {
            emailTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        //if email address not an valid email address
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            emailTIL.getEditText().setError("Please enter a valid email address");
            return false;
        }
        else{
            return true;
        }
    }

    //validate password
    public static boolean validatePassword(TextInputLayout passwordTIL) {
        //get password
        String passwordInput = passwordTIL.getEditText().getText().toString().trim();

        //if password == null
        if(passwordInput.isEmpty()){
            passwordTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        //if password less than 6 character or have white space
        else if(!Variable.PASSWORD_PATTERN.matcher(passwordInput).matches()){
            passwordTIL.getEditText().setError("The length of password must have at least 6 character");
            return false;
        }
        else{
            return true;
        }
    }

    //validate confirm password
    public static boolean validateConfirmPassword(TextInputLayout passwordTIL, TextInputLayout confirmPasswordTIL) {
        //get password
        String passwordInput = passwordTIL.getEditText().getText().toString().trim();
        //get confirm password
        String confirmPasswordInput = confirmPasswordTIL.getEditText().getText().toString().trim();

        //if confirm password == null
        if(confirmPasswordInput.isEmpty()){
            confirmPasswordTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        // if confirm password != password
        else if(!confirmPasswordInput.equals(passwordInput)){
            confirmPasswordTIL.getEditText().setError("Passwords are not same");
            return false;
        }
        else{
            return true;
        }
    }
}
