import { actionsEnums } from '../common/actionsEnums';
import { userAPI } from '../api/userRegister';
import { UserRegister, UserLogin } from '../reducers/user';

export const userRequestCompleted = () => {
  return {
    type: actionsEnums.USER_CREATE_COMPLETED
  }
}
export const userLoginCompleted = () => {
  return {
    type: actionsEnums.USER_LOGIN_COMPLETED
  }
}

export const createUserRequest = (userRegister:UserRegister) => (dispatcher) => {
  const promise = userAPI.createUser(userRegister)

  promise.then(
    (data) => dispatcher(userRequestCompleted())
  );

  return promise;
}
export const loginRequest = (login:UserLogin) => (dispatcher) => {
  const promise = userAPI.doLogin(login)

  promise.then(
    (data) => dispatcher(userLoginCompleted())
  );

  return promise;
}

