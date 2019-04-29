package pt.fabm.abola.validation

import pt.fabm.abola.ErrorResponse

class AuthException : Exception("Autentication Fails"), ErrorResponse{
  override val statusCode: Int get() = 403
}
