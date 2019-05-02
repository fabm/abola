import {connect} from 'react-redux';
import {State} from '../../reducers';

import {HelloWorldComponent} from './helloWorld';
import { createUserRequest, loginRequest } from '../../actions/userRequest';
import { UserRegister } from '../../reducers/user';

const mapStateToProps = (state : State) => {
  return {
    userName: state.userProfileReducer.firstname
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    createUser: (userRegister:UserRegister) => dispatch(createUserRequest(userRegister)),
    loginUser:(login)=> dispatch(loginRequest(login))
  };
}

export const HelloWorldContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(HelloWorldComponent);
