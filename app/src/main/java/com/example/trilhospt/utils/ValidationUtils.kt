package com.example.trilhospt.utils

import android.widget.EditText

object ValidationUtils {
    
    /**
     * Valida se um campo de texto não está vazio
     */
    fun validateNotEmpty(editText: EditText, fieldName: String): Boolean {
        val text = editText.text.toString().trim()
        return if (text.isBlank()) {
            editText.error = "$fieldName é obrigatório"
            editText.requestFocus()
            false
        } else {
            editText.error = null
            true
        }
    }
    
    /**
     * Valida comprimento mínimo de texto
     */
    fun validateMinLength(editText: EditText, minLength: Int, fieldName: String): Boolean {
        val text = editText.text.toString().trim()
        return if (text.length < minLength) {
            editText.error = "$fieldName deve ter pelo menos $minLength caracteres"
            editText.requestFocus()
            false
        } else {
            editText.error = null
            true
        }
    }
    
    /**
     * Valida número positivo
     */
    fun validatePositiveNumber(editText: EditText, fieldName: String): Double? {
        val text = editText.text.toString().trim()
        
        if (text.isBlank()) {
            editText.error = "$fieldName é obrigatório"
            editText.requestFocus()
            return null
        }
        
        val number = text.toDoubleOrNull()
        return if (number == null || number <= 0) {
            editText.error = "$fieldName deve ser maior que 0"
            editText.requestFocus()
            null
        } else {
            editText.error = null
            number
        }
    }
    
    /**
     * Valida número inteiro positivo
     */
    fun validatePositiveInt(editText: EditText, fieldName: String): Int? {
        val text = editText.text.toString().trim()
        
        if (text.isBlank()) {
            editText.error = "$fieldName é obrigatório"
            editText.requestFocus()
            return null
        }
        
        val number = text.toIntOrNull()
        return if (number == null || number <= 0) {
            editText.error = "$fieldName deve ser maior que 0"
            editText.requestFocus()
            null
        } else {
            editText.error = null
            number
        }
    }
    
    /**
     * Valida range de número
     */
    fun validateNumberRange(editText: EditText, min: Double, max: Double, fieldName: String): Double? {
        val number = validatePositiveNumber(editText, fieldName) ?: return null
        
        return if (number < min || number > max) {
            editText.error = "$fieldName deve estar entre $min e $max"
            editText.requestFocus()
            null
        } else {
            editText.error = null
            number
        }
    }
    
    /**
     * Valida email
     */
    fun validateEmail(editText: EditText): Boolean {
        val email = editText.text.toString().trim()
        
        if (email.isBlank()) {
            editText.error = "Email é obrigatório"
            editText.requestFocus()
            return false
        }
        
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (!email.matches(emailPattern.toRegex())) {
            editText.error = "Email inválido"
            editText.requestFocus()
            false
        } else {
            editText.error = null
            true
        }
    }
    
    /**
     * Valida password
     */
    fun validatePassword(editText: EditText, minLength: Int = 6): Boolean {
        val password = editText.text.toString()
        
        if (password.isBlank()) {
            editText.error = "Password é obrigatória"
            editText.requestFocus()
            return false
        }
        
        return if (password.length < minLength) {
            editText.error = "Password deve ter pelo menos $minLength caracteres"
            editText.requestFocus()
            false
        } else {
            editText.error = null
            true
        }
    }
    
    /**
     * Valida se duas passwords coincidem
     */
    fun validatePasswordMatch(password1: EditText, password2: EditText): Boolean {
        val pass1 = password1.text.toString()
        val pass2 = password2.text.toString()
        
        return if (pass1 != pass2) {
            password2.error = "Passwords não coincidem"
            password2.requestFocus()
            false
        } else {
            password2.error = null
            true
        }
    }
}
