package pt.fabm.template.models

import java.time.LocalDate

data class Car(
  val model: String,
  val make: CarMake,
  val price: Int,
  val maturityDate: LocalDate
)
