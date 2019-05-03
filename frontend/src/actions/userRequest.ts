import { actionsEnums } from '../common/actionsEnums';

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


