package pt.fabm.abola.models

data class UserRegisterIn(
  val name: String,
  val email: String,
  val pass: ByteArray
)
