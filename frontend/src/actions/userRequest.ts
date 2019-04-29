import { actionsEnums } from '../common/actionsEnums';
import { userAPI } from '../api/user';

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

export const createUserRequest = () => (dispatcher) => {
  const promise = userAPI.createUser()

  promise.then(
    (data) => dispatcher(userRequestCompleted())
  );

  return promise;
}
export const loginRequest = () => (dispatcher) => {
  const promise = userAPI.doLogin()

  promise.then(
    (data) => dispatcher(userLoginCompleted())
  );

  return promise;
}

