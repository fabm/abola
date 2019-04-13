package pt.fabm.abola.dao

import io.reactivex.Completable
import pt.fabm.abola.models.UserRegisterIn

interface UserDao{
  fun create(userRegisterIn: UserRegisterIn):Completable{
    //implementar o accesso à bd
    return Completable.complete()
  }
}
