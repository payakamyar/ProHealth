package com.projekt.prohealth.utility

object InputValidation {
    fun validateEmail(email:String):Pair<Boolean,String>{
        if(email.isEmpty()){
            return Pair(false,"The email is empty.")
        }
        if(!email.matches(Regex("^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$")))
            return Pair(false,"The email you provided is not valid. Please enter a valid email.")
        return Pair(true, "")
    }

    fun validatePassword(password:String):Pair<Boolean, String>{
        if(password.isEmpty() || password.isBlank())
            return Pair(false, "The password is empty.")
        if(password.length < 8)
            return Pair(false,"Password is less than 8 characters.")
        return Pair(true,"")
    }

}