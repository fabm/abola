package pt.fabm.abola.validation

import pt.fabm.abola.ErrorResponse

class RequiredException(
  field: String?
) : Exception("$field is empty"),
  ErrorResponse {
  override val statusCode: Int get() = 400
}
